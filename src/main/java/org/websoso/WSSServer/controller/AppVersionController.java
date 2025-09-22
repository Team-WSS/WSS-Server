package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.application.AppVersionApplication;
import org.websoso.support.version.dto.MinimumVersionGetResponse;
import org.websoso.support.version.dto.MinimumVersionUpdateRequest;

@RestController
@RequiredArgsConstructor
public class AppVersionController {

    private final AppVersionApplication appVersionApplication;

    @GetMapping("/minimum-version")
    public ResponseEntity<MinimumVersionGetResponse> getMinimumVersion(@RequestParam String os) {
        return ResponseEntity
                .status(OK)
                .body(appVersionApplication.getMinimumVersion(os));
    }

    @PostMapping("/minimum-version")
    public ResponseEntity<Void> updateMinimumVersion(@Valid @RequestBody MinimumVersionUpdateRequest request) {
        appVersionApplication.updateMinimumVersion(request);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }
}
