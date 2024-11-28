package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.OK;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.dto.AppVersion.MinimumVersionGetResponse;
import org.websoso.WSSServer.service.AppVersionService;

@RestController
@RequiredArgsConstructor
public class AppVersionController {

    private final AppVersionService appVersionService;

    @GetMapping("/minimum-version")
    public ResponseEntity<MinimumVersionGetResponse> getMinimumVersion(@RequestParam String os) {
        return ResponseEntity
                .status(OK)
                .body(appVersionService.getMinimumVersion(os));
    }
}
