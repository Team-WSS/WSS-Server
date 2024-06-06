package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.CREATED;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.dto.notice.NoticePostRequest;
import org.websoso.WSSServer.service.NoticeService;

@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @PostMapping
    public ResponseEntity<Void> createNotice(@RequestBody NoticePostRequest noticePostRequest) {
        noticeService.createNotice(noticePostRequest);
        return ResponseEntity
                .status(CREATED)
                .build();
    }
}
