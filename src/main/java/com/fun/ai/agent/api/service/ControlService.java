package com.fun.ai.agent.api.service;

import com.fun.ai.agent.api.model.AcceptedActionResponse;
import com.fun.ai.agent.api.model.CreateInstanceRequest;
import com.fun.ai.agent.api.model.InstanceActionRequest;
import com.fun.ai.agent.api.model.InstanceActionType;
import com.fun.ai.agent.api.model.InstanceDesiredState;
import com.fun.ai.agent.api.model.InstanceRuntime;
import com.fun.ai.agent.api.model.InstanceStatus;
import com.fun.ai.agent.api.model.LobsterInstanceDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ControlService {

    private final Map<UUID, LobsterInstanceDto> instances = new ConcurrentHashMap<>();

    public List<LobsterInstanceDto> listInstances() {
        return instances.values().stream()
                .sorted(Comparator.comparing(LobsterInstanceDto::createdAt))
                .toList();
    }

    public LobsterInstanceDto createInstance(CreateInstanceRequest request) {
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

        LobsterInstanceDto instance = new LobsterInstanceDto(
                instanceId,
                request.name(),
                hostId,
                InstanceRuntime.ZEROCLAW,
                status,
                desiredState,
                now,
                now
        );
        instances.put(instanceId, instance);
        return instance;
    }

    public AcceptedActionResponse submitInstanceAction(UUID instanceId, InstanceActionRequest request) {
        LobsterInstanceDto instance = getInstance(instanceId);
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

        LobsterInstanceDto updated = new LobsterInstanceDto(
                instance.id(),
                instance.name(),
                instance.hostId(),
                instance.runtime(),
                status,
                desiredState,
                instance.createdAt(),
                now
        );
        instances.put(instanceId, updated);
        return new AcceptedActionResponse(UUID.randomUUID(), now);
    }

    private LobsterInstanceDto getInstance(UUID instanceId) {
        LobsterInstanceDto instance = instances.get(instanceId);
        if (instance == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "instance not found");
        }
        return instance;
    }
}
