package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.feed.FeedCreateRequest;
import org.websoso.WSSServer.service.FeedService;
import org.websoso.WSSServer.service.UserService;

@RequestMapping("/feeds")
@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Void> createFeed(Principal principal,
                                           @Valid @RequestBody FeedCreateRequest request) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        feedService.createFeed(user, request);

        return ResponseEntity
                .status(CREATED)
                .build();
    }
}
