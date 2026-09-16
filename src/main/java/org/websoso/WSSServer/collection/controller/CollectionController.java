package org.websoso.WSSServer.collection.controller;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.collection.application.CollectionFindApplication;
import org.websoso.WSSServer.collection.application.CollectionManagementApplication;
import org.websoso.WSSServer.collection.controller.dto.CollectionCreateRequest;
import org.websoso.WSSServer.collection.controller.dto.CollectionCreateResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionGetResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionUpdateRequest;
import org.websoso.WSSServer.collection.controller.dto.CollectionsGetResponse;
import org.websoso.WSSServer.domain.common.SortCriteria;
import org.websoso.WSSServer.user.domain.User;

@RequestMapping
@RestController
@RequiredArgsConstructor
public class CollectionController {

    private static final String DEFAULT_PAGE_SIZE = "10";

    private final CollectionManagementApplication collectionManagementApplication;
    private final CollectionFindApplication collectionFindApplication;

    @PostMapping("/collections")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CollectionCreateResponse> createCollection(@AuthenticationPrincipal User user,
                                                                     @Valid @RequestBody CollectionCreateRequest request) {
        return ResponseEntity
                .status(CREATED)
                .body(collectionManagementApplication.create(user, request));
    }

    @PutMapping("/collections/{collectionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateCollection(@AuthenticationPrincipal User user,
                                                 @PathVariable("collectionId") Long collectionId,
                                                 @Valid @RequestBody CollectionUpdateRequest request) {
        collectionManagementApplication.update(user, collectionId, request);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @DeleteMapping("/collections/{collectionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteCollection(@AuthenticationPrincipal User user,
                                                 @PathVariable("collectionId") Long collectionId) {
        collectionManagementApplication.delete(user, collectionId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    /**
     * 사용자별 컬렉션 목록. 마이페이지 미리보기도 전용 API 없이 이 API를 {@code size=3}으로 호출한다.
     * 본인 목록에만 비공개 컬렉션이 포함되므로 로그인한 사용자만 요청할 수 있다.
     */
    @GetMapping("/users/{userId}/collections")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CollectionsGetResponse> getUserCollections(
            @AuthenticationPrincipal User user,
            @PathVariable("userId") Long userId,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity
                .status(OK)
                .body(collectionFindApplication.getUserCollections(user, userId, cursor, size));
    }

    /**
     * 컬렉션 상세. 공유 링크로 들어온 비로그인 사용자도 공개 컬렉션은 볼 수 있어야 하므로 인증을 선택으로 둔다.
     * 로그인 여부에 따라 응답이 달라지므로({@code isMyCollection}, 비공개 접근 허용 여부)
     * 토큰이 있으면 그대로 인증에 사용한다.
     */
    @GetMapping("/collections/{collectionId}")
    public ResponseEntity<CollectionGetResponse> getCollection(
            @AuthenticationPrincipal User user,
            @PathVariable("collectionId") Long collectionId,
            @RequestParam(value = "sortCriteria", required = false) SortCriteria sortCriteria) {
        return ResponseEntity
                .status(OK)
                .body(collectionFindApplication.getCollection(user, collectionId, sortCriteria));
    }
}
