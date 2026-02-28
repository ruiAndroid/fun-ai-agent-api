package com.fun.ai.agent.api.controller;

import com.fun.ai.agent.api.model.ImagePresetDto;
import com.fun.ai.agent.api.model.ListResponse;
import com.fun.ai.agent.api.service.ControlService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/images")
public class ImageController {

    private final ControlService controlService;

    public ImageController(ControlService controlService) {
        this.controlService = controlService;
    }

    @GetMapping
    public ListResponse<ImagePresetDto> listImages() {
        return new ListResponse<>(controlService.listImagePresets());
    }
}
