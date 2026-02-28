package com.fun.ai.agent.api.service;

import com.fun.ai.agent.api.config.ImageCatalogProperties;
import com.fun.ai.agent.api.model.AcceptedActionResponse;
import com.fun.ai.agent.api.model.ClawInstanceDto;
import com.fun.ai.agent.api.model.CreateInstanceRequest;
import com.fun.ai.agent.api.model.ImagePresetDto;
import com.fun.ai.agent.api.model.InstanceActionRequest;
import com.fun.ai.agent.api.model.InstanceActionType;
import com.fun.ai.agent.api.model.InstanceDesiredState;
import com.fun.ai.agent.api.model.InstanceRuntime;
import com.fun.ai.agent.api.model.InstanceStatus;
import com.fun.ai.agent.api.repository.InstanceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ControlService {

    private final InstanceRepository instanceRepository;
    private final ImageCatalogProperties imageCatalogProperties;
    private final PlaneClient planeClient;

    public ControlService(InstanceRepository instanceRepository,
                          ImageCatalogProperties imageCatalogProperties,
                          PlaneClient planeClient) {
        this.instanceRepository = instanceRepository;
        this.imageCatalogProperties = imageCatalogProperties;
        this.planeClient = planeClient;
    }

    public List<ClawInstanceDto> listInstances() {
        return instanceRepository.findAll();
    }

    public List<ImagePresetDto> listImagePresets() {
        return imageCatalogProperties.getPresets().stream()
                .filter(this::isValidPreset)
                .map(preset -> new ImagePresetDto(
                        preset.getId().trim(),
                        preset.getName().trim(),
                        preset.getImage().trim(),
                        InstanceRuntime.ZEROCLAW,
                        preset.getDescription(),
                        preset.isRecommended()
                ))
                .toList();
    }

    @Transactional
    public ClawInstanceDto createInstance(CreateInstanceRequest request) {
        String name = request.name().trim();
        validateInstanceName(name);

        String image = request.image().trim();
        validateRequestedImage(image);

        UUID hostId;
        try {
            hostId = UUID.fromString(request.hostId().trim());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "hostId must be a valid UUID");
        }

        UUID instanceId = UUID.randomUUID();
        Instant now = Instant.now();
        InstanceDesiredState desiredState = Objects.requireNonNullElse(request.desiredState(), InstanceDesiredState.RUNNING);
        InstanceStatus status = desiredState == InstanceDesiredState.RUNNING ? InstanceStatus.CREATING : InstanceStatus.STOPPED;

        ClawInstanceDto instance = new ClawInstanceDto(
                instanceId,
                name,
                hostId,
                image,
                InstanceRuntime.ZEROCLAW,
                status,
                desiredState,
                now,
                now
        );
        instanceRepository.insert(instance);

        if (desiredState == InstanceDesiredState.RUNNING) {
            PlaneClient.PlaneTaskExecutionRecord execution = planeClient.reconcileInstanceAction(
                    instance.id(),
                    InstanceActionType.START,
                    instance.image()
            );
            if (!execution.succeeded()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "plane execution failed: " + execution.message());
            }
            Instant updatedAt = Instant.now();
            InstanceStatus finalStatus = InstanceStatus.RUNNING;
            instanceRepository.updateState(instance.id(), finalStatus, desiredState, updatedAt);
            return new ClawInstanceDto(
                    instance.id(),
                    instance.name(),
                    instance.hostId(),
                    instance.image(),
                    instance.runtime(),
                    finalStatus,
                    desiredState,
                    instance.createdAt(),
                    updatedAt
            );
        }

        return instance;
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public AcceptedActionResponse submitInstanceAction(UUID instanceId, InstanceActionRequest request) {
        ClawInstanceDto instance = getInstance(instanceId);
        Instant now = Instant.now();

        InstanceDesiredState desiredState = desiredStateForAction(request.action());
        PlaneClient.PlaneTaskExecutionRecord execution;
        try {
            execution = planeClient.reconcileInstanceAction(
                    instance.id(),
                    request.action(),
                    instance.image()
            );
        } catch (ResponseStatusException ex) {
            instanceRepository.updateState(instance.id(), InstanceStatus.ERROR, desiredState, now);
            instanceRepository.insertAction(
                    instance.id(),
                    request.action(),
                    failureReason(request.reason(), ex.getReason()),
                    now
            );
            throw ex;
        }
        InstanceStatus status = execution.succeeded()
                ? statusForSuccessfulAction(request.action())
                : InstanceStatus.ERROR;

        instanceRepository.updateState(instance.id(), status, desiredState, now);
        UUID actionTaskId = instanceRepository.insertAction(
                instance.id(),
                request.action(),
                execution.succeeded() ? request.reason() : failureReason(request.reason(), execution.message()),
                now
        );
        if (!execution.succeeded()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "plane execution failed: " + execution.message());
        }
        return new AcceptedActionResponse(actionTaskId, now);
    }

    @Transactional
    public void deleteInstance(UUID instanceId) {
        getInstance(instanceId);
        planeClient.deleteInstance(instanceId);
        int deletedRows = instanceRepository.deleteById(instanceId);
        if (deletedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "instance not found");
        }
    }

    private ClawInstanceDto getInstance(UUID instanceId) {
        return instanceRepository.findById(instanceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "instance not found"));
    }

    private void validateInstanceName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank");
        }

        if (instanceRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "instance name already exists");
        }
    }

    private void validateRequestedImage(String image) {
        if (imageCatalogProperties.isAllowCustomImage()) {
            return;
        }

        List<ImageCatalogProperties.Preset> validPresets = imageCatalogProperties.getPresets().stream()
                .filter(this::isValidPreset)
                .toList();
        if (validPresets.isEmpty()) {
            return;
        }

        boolean matched = validPresets.stream()
                .anyMatch(preset -> image.equals(preset.getImage().trim()));
        if (!matched) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "image must come from configured presets");
        }
    }

    private boolean isValidPreset(ImageCatalogProperties.Preset preset) {
        return StringUtils.hasText(preset.getId())
                && StringUtils.hasText(preset.getName())
                && StringUtils.hasText(preset.getImage());
    }

    private InstanceDesiredState desiredStateForAction(InstanceActionType action) {
        return switch (action) {
            case STOP -> InstanceDesiredState.STOPPED;
            case START, RESTART, ROLLBACK -> InstanceDesiredState.RUNNING;
        };
    }

    private InstanceStatus statusForSuccessfulAction(InstanceActionType action) {
        return switch (action) {
            case STOP -> InstanceStatus.STOPPED;
            case START, RESTART, ROLLBACK -> InstanceStatus.RUNNING;
        };
    }

    private String failureReason(String reason, String executionMessage) {
        String fallback = StringUtils.hasText(executionMessage) ? executionMessage : "plane execution failed";
        if (!StringUtils.hasText(reason)) {
            return "[FAILED] " + fallback;
        }
        return reason + " | [FAILED] " + fallback;
    }
}
