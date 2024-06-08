package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.feed.FeedCreateRequest;
import org.websoso.WSSServer.dto.feed.FeedGetResponse;
import org.websoso.WSSServer.dto.feed.FeedUpdateRequest;
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

    @PutMapping("/{feedId}")
    public ResponseEntity<Void> updateFeed(Principal principal,
                                           @PathVariable("feedId") Long feedId,
                                           @Valid @RequestBody FeedUpdateRequest request) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        feedService.updateFeed(user, feedId, request);

        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @DeleteMapping("/{feedId}")
    public ResponseEntity<Void> deleteFeed(Principal principal,
                                           @PathVariable("feedId") Long feedId) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        feedService.deleteFeed(user, feedId);

        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @PostMapping("/{feedId}/likes")
    public ResponseEntity<Void> likeFeed(Principal principal,
                                         @PathVariable("feedId") Long feedId) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        feedService.likeFeed(user, feedId);

        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @DeleteMapping("/{feedId}/likes")
    public ResponseEntity<Void> unLikeFeed(Principal principal,
                                           @PathVariable("feedId") Long feedId) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        feedService.unLikeFeed(user, feedId);

        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @GetMapping("/{feedId}")
    public ResponseEntity<FeedGetResponse> getFeed(Principal principal,
                                                   @PathVariable("feedId") Long feedId) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        FeedGetResponse response = feedService.getFeedById(user, feedId);

        return ResponseEntity
                .status(OK)
                .body(response);
    }

}
