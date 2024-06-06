package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.CREATED;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.notice.NoticePostRequest;
import org.websoso.WSSServer.service.NoticeService;
import org.websoso.WSSServer.service.UserService;

@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Void> createNotice(Principal principal,
                                             @RequestBody NoticePostRequest noticePostRequest) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        noticeService.createNotice(user, noticePostRequest);
        return ResponseEntity
                .status(CREATED)
                .build();
    }
}
