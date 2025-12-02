package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.NO_CONTENT;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.application.LibraryInterestApplication;
import org.websoso.WSSServer.user.domain.User;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryInterestApplication libraryInterestApplication;

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

}
