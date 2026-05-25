package com.kedada.backend.media.controller;

import com.kedada.backend.auth.security.AuthenticatedUser;
import com.kedada.backend.media.dto.MediaAssetResponse;
import com.kedada.backend.media.service.MediaAssetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
public class MediaAssetController {

    private final MediaAssetService service;

    public MediaAssetController(MediaAssetService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    MediaAssetResponse upload(@AuthenticationPrincipal AuthenticatedUser user, @RequestPart("file") MultipartFile file) {
        return service.upload(user.id(), file);
    }

    @GetMapping("/{id}")
    MediaAssetResponse get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
        return service.get(id, user == null ? null : user.id());
    }
}
