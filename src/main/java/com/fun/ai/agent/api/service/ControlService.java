package com.fun.ai.agent.api.service;

import com.fun.ai.agent.api.model.AcceptedActionResponse;
import com.fun.ai.agent.api.model.CreateInstanceRequest;
import com.fun.ai.agent.api.model.InstanceActionRequest;
import com.fun.ai.agent.api.model.InstanceActionType;
import com.fun.ai.agent.api.model.InstanceDesiredState;
import com.fun.ai.agent.api.model.InstanceRuntime;
import com.fun.ai.agent.api.model.InstanceStatus;
import com.fun.ai.agent.api.model.ClawInstanceDto;
import com.fun.ai.agent.api.repository.InstanceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ControlService {

    private final InstanceRepository instanceRepository;

    public ControlService(InstanceRepository instanceRepository) {
        this.instanceRepository = instanceRepository;
    }

    public List<ClawInstanceDto> listInstances() {
        return instanceRepository.findAll();
    }

    @Transactional
    public ClawInstanceDto createInstance(CreateInstanceRequest request) {
        UUID hostId;
        try {
            hostId = UUID.fromString(request.hostId());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "hostId must be a valid UUID");
        }

        UUID instanceId = UUID.randomUUID();
        Instant now = Instant.now();
        InstanceDesiredState desiredState = Objects.requireNonNullElse(request.desiredState(), InstanceDesiredState.RUNNING);
        InstanceStatus status = desiredState == InstanceDesiredState.RUNNING ? InstanceStatus.RUNNING : InstanceStatus.STOPPED;

        ClawInstanceDto instance = new ClawInstanceDto(
                instanceId,
                request.name(),
                hostId,
                InstanceRuntime.ZEROCLAW,
                status,
                desiredState,
                now,
                now
        );
        instanceRepository.insert(instance, request.image());
        return instance;
    }

    @Transactional
    public AcceptedActionResponse submitInstanceAction(UUID instanceId, InstanceActionRequest request) {
        ClawInstanceDto instance = getInstance(instanceId);
        Instant now = Instant.now();

        InstanceDesiredState desiredState = instance.desiredState();
        InstanceStatus status = instance.status();

        if (request.action() == InstanceActionType.START) {
            desiredState = InstanceDesiredState.RUNNING;
            status = InstanceStatus.RUNNING;
        } else if (request.action() == InstanceActionType.STOP) {
            desiredState = InstanceDesiredState.STOPPED;
            status = InstanceStatus.STOPPED;
        } else if (request.action() == InstanceActionType.RESTART) {
            desiredState = InstanceDesiredState.RUNNING;
            status = InstanceStatus.RUNNING;
        } else if (request.action() == InstanceActionType.ROLLBACK) {
            status = InstanceStatus.CREATING;
        }

        instanceRepository.updateState(instance.id(), status, desiredState, now);
        UUID actionTaskId = instanceRepository.insertAction(instance.id(), request.action(), request.reason(), now);
        return new AcceptedActionResponse(actionTaskId, now);
    }

    private ClawInstanceDto getInstance(UUID instanceId) {
        return instanceRepository.findById(instanceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "instance not found"));
    }
}
