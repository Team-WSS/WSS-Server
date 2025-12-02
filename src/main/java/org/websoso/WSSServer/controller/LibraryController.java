package org.websoso.WSSServer.controller;

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
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.application.LibraryEvaluationApplication;
import org.websoso.WSSServer.application.LibraryInterestApplication;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.userNovel.UserNovelCreateRequest;
import org.websoso.WSSServer.dto.userNovel.UserNovelGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserNovelUpdateRequest;
import org.websoso.WSSServer.library.service.LibraryService;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryInterestApplication libraryInterestApplication;
    private final LibraryEvaluationApplication libraryEvaluationApplication;

    /**
     * 관심있어요 등록
     *
     * @param user    로그인 유저 객체
     * @param novelId 소설 ID
     * @return 204 NO_CONTENT
     */
    @PostMapping("/novels/{novelId}/is-interest")
    @PreAuthorize("isAuthenticated() and @authorizationService.validate(#novelId, #user, T(org.websoso.WSSServer.novel.domain.Novel))")
    public ResponseEntity<Void> registerAsInterest(@AuthenticationPrincipal User user,
                                                   @PathVariable("novelId") Long novelId) {
        libraryInterestApplication.registerAsInterest(user, novelId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    /**
     * 관심있어요 삭제
     *
     * @param user    로그인 유저 객체
     * @param novelId 소설 ID
     * @return 204 NO_CONTENT
     */
    @DeleteMapping("/novels/{novelId}/is-interest")
    @PreAuthorize("isAuthenticated() and @authorizationService.validate(#novelId, #user, T(org.websoso.WSSServer.library.domain.UserNovel))")
    public ResponseEntity<Void> unregisterAsInterest(@AuthenticationPrincipal User user,
                                                     @PathVariable("novelId") Long novelId) {
        libraryInterestApplication.unregisterAsInterest(user, novelId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    /**
     * 작품 평가하기
     *
     * @param user    로그인 유저 객체
     * @param request 서재 (작품 평가) 생성 객체
     * @return 201 CREATED
     */
    @PostMapping("/user-novels")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> createEvaluation(@AuthenticationPrincipal User user,
                                                 @Valid @RequestBody UserNovelCreateRequest request) {
        libraryEvaluationApplication.createEvaluation(user, request);
        return ResponseEntity
                .status(CREATED)
                .build();
    }

    /**
     * 작품 평가 불러오기
     *
     * @param user    로그인 유저 객체
     * @param novelId 소설 ID
     * @return UserNovelGetResponse
     */
    @GetMapping("/user-novels/{novelId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserNovelGetResponse> getEvaluation(@AuthenticationPrincipal User user,
                                                              @PathVariable Long novelId) {
        return ResponseEntity
                .status(OK)
                .body(libraryEvaluationApplication.getEvaluation(user, novelId));
    }

    /**
     * 작품 평가 업데이트하기
     *
     * @param user    로그인 유저 객체
     * @param novelId 소설 ID
     * @param request 서재 (작품 평가) 업데이트 객체
     * @return 204 NO_CONTENT
     */
    @PutMapping("/user-novels/{novelId}")
    @PreAuthorize("isAuthenticated() and @authorizationService.validate(#novelId, #user, T(org.websoso.WSSServer.library.domain.UserNovel))")
    public ResponseEntity<Void> updateEvaluation(@AuthenticationPrincipal User user,
                                                 @PathVariable Long novelId,
                                                 @Valid @RequestBody UserNovelUpdateRequest request) {
        libraryEvaluationApplication.updateEvaluation(user, novelId, request);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    /**
     * 작품 평가 삭제하기
     *
     * @param user    로그인 유저 객체
     * @param novelId 소설 ID
     * @return 204 NO_CONTENT
     */
    @DeleteMapping("/user-novels/{novelId}")
    @PreAuthorize("isAuthenticated() and @authorizationService.validate(#novelId, #user, T(org.websoso.WSSServer.library.domain.UserNovel))")
    public ResponseEntity<Void> deleteEvaluation(@AuthenticationPrincipal User user,
                                                 @PathVariable Long novelId) {
        libraryEvaluationApplication.deleteEvaluation(user, novelId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

}
