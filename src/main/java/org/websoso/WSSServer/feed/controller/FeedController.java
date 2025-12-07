package org.websoso.WSSServer.feed.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.application.FeedFindApplication;
import org.websoso.WSSServer.domain.common.FeedGetOption;
import org.websoso.WSSServer.dto.feed.FeedCreateRequest;
import org.websoso.WSSServer.dto.feed.FeedCreateResponse;
import org.websoso.WSSServer.dto.feed.FeedGetResponse;
import org.websoso.WSSServer.dto.feed.FeedImageCreateRequest;
import org.websoso.WSSServer.dto.feed.FeedImageUpdateRequest;
import org.websoso.WSSServer.dto.feed.FeedUpdateRequest;
import org.websoso.WSSServer.dto.feed.FeedsGetResponse;
import org.websoso.WSSServer.dto.feed.InterestFeedsGetResponse;
import org.websoso.WSSServer.dto.popularFeed.PopularFeedsGetResponse;
import org.websoso.WSSServer.feed.service.FeedService;
import org.websoso.WSSServer.user.domain.User;

@RequestMapping("/feeds")
@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final FeedFindApplication feedFindApplication;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FeedCreateResponse> createFeed(@AuthenticationPrincipal User user,
                                                         @Valid @RequestPart("feed") FeedCreateRequest request,
                                                         @Valid @ModelAttribute FeedImageCreateRequest requestImage) {
        return ResponseEntity
                .status(CREATED)
                .body(feedService.createFeed(user, request, requestImage));
    }

    @GetMapping("/{feedId}")
    @PreAuthorize("isAuthenticated() and @feedAccessValidator.canAccess(#feedId, #user)")
    public ResponseEntity<FeedGetResponse> getFeed(@AuthenticationPrincipal User user,
                                                   @PathVariable("feedId") Long feedId) {
        return ResponseEntity
                .status(OK)
                .body(feedFindApplication.getFeedById(user, feedId));
    }

    @GetMapping
    public ResponseEntity<FeedsGetResponse> getFeeds(@AuthenticationPrincipal User user,
                                                     @RequestParam(value = "category", required = false) String category,
                                                     @RequestParam("lastFeedId") Long lastFeedId,
                                                     @RequestParam("size") int size,
                                                     @RequestParam(value = "feedsOption", required = false) FeedGetOption feedGetOption) {
        return ResponseEntity
                .status(OK)
                .body(feedFindApplication.getFeeds(user, category, lastFeedId, size, feedGetOption));
    }

    @PutMapping("/{feedId}")
    @PreAuthorize("isAuthenticated() and @authorizationService.validate(#feedId, #user, T(org.websoso.WSSServer.feed.domain.Feed))")
    public ResponseEntity<FeedCreateResponse> updateFeed(@AuthenticationPrincipal User user,
                                                         @PathVariable("feedId") Long feedId,
                                                         @Valid @RequestPart("feed") FeedUpdateRequest request,
                                                         @Valid @ModelAttribute FeedImageUpdateRequest requestImage) {
        return ResponseEntity
                .status(OK)
                .body(feedService.updateFeed(feedId, request, requestImage));
    }

    @DeleteMapping("/{feedId}")
    @PreAuthorize("isAuthenticated() and @authorizationService.validate(#feedId, #user, T(org.websoso.WSSServer.feed.domain.Feed))")
    public ResponseEntity<Void> deleteFeed(@AuthenticationPrincipal User user,
                                           @PathVariable("feedId") Long feedId) {
        feedService.deleteFeed(feedId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @PostMapping("/{feedId}/likes")
    @PreAuthorize("isAuthenticated() and @feedAccessValidator.canAccess(#feedId, #user)")
    public ResponseEntity<Void> likeFeed(@AuthenticationPrincipal User user,
                                         @PathVariable("feedId") Long feedId) {
        feedService.likeFeed(user, feedId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @DeleteMapping("/{feedId}/likes")
    @PreAuthorize("isAuthenticated() and @feedAccessValidator.canAccess(#feedId, #user)")
    public ResponseEntity<Void> unLikeFeed(@AuthenticationPrincipal User user,
                                           @PathVariable("feedId") Long feedId) {
        feedService.unLikeFeed(user, feedId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @GetMapping("/popular")
    public ResponseEntity<PopularFeedsGetResponse> getPopularFeeds(@AuthenticationPrincipal User user) {
        return ResponseEntity
                .status(OK)
                .body(feedFindApplication.getPopularFeeds(user));
    }

    @GetMapping("/interest")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InterestFeedsGetResponse> getInterestFeeds(@AuthenticationPrincipal User user) {
        return ResponseEntity
                .status(OK)
                .body(feedFindApplication.getInterestFeeds(user));
    }

}
