package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.OK;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.dto.sosoPick.SosoPickGetResponse;
import org.websoso.WSSServer.service.SosoPickService;

@RestController
@RequestMapping("/soso-picks")
@RequiredArgsConstructor
public class SosoPickController {

    private final SosoPickService sosoPickService;

    @GetMapping
    public ResponseEntity<SosoPickGetResponse> getSosoPick() {
        return ResponseEntity
                .status(OK)
                .body(sosoPickService.getSosoPick());
    }
}
