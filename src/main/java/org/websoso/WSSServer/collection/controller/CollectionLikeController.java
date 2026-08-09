package org.websoso.WSSServer.collection.controller;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.collection.application.CollectionLikeApplication;
import org.websoso.WSSServer.collection.application.CollectionLikeFindApplication;
import org.websoso.WSSServer.collection.controller.dto.LikedCollectionsGetResponse;
import org.websoso.WSSServer.user.domain.User;

@RequestMapping
@RestController
@RequiredArgsConstructor
public class CollectionLikeController {

    private static final String DEFAULT_PAGE_SIZE = "10";

    private final CollectionLikeApplication collectionLikeApplication;
    private final CollectionLikeFindApplication collectionLikeFindApplication;

    /**
     * 컬렉션 좋아요 등록. 같은 요청을 반복해도 좋아요는 하나만 남으므로 멱등하다.
     * 이미 좋아요한 컬렉션이어도 성공 응답을 그대로 돌려준다.
     */
    @PutMapping("/collections/{collectionId}/likes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> likeCollection(@AuthenticationPrincipal User user,
                                               @PathVariable("collectionId") Long collectionId) {
        collectionLikeApplication.create(user, collectionId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    /**
     * 컬렉션 좋아요 취소. 좋아요하지 않은 컬렉션의 취소 요청도 오류가 아니라 성공이다.
     */
    @DeleteMapping("/collections/{collectionId}/likes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unlikeCollection(@AuthenticationPrincipal User user,
                                                 @PathVariable("collectionId") Long collectionId) {
        collectionLikeApplication.delete(user, collectionId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    /**
     * 좋아요한 컬렉션 목록. 본인이 좋아요한 목록만 조회하므로 경로에 사용자 ID를 받지 않고 토큰의 사용자를 쓴다.
     */
    @GetMapping("/users/me/liked-collections")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LikedCollectionsGetResponse> getLikedCollections(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity
                .status(OK)
                .body(collectionLikeFindApplication.getLikedCollections(user, cursor, size));
    }
}
