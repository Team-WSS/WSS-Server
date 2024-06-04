package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.OK;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.dto.novel.NovelGetResponse1;
import org.websoso.WSSServer.service.NovelService;
import org.websoso.WSSServer.service.UserService;

@RestController
@RequestMapping("/novels")
@RequiredArgsConstructor
public class NovelController {

    private final NovelService novelService;
    private final UserService userService;

    // TODO 이름 변경(작품 정보 조회 뷰에서 상단, 기본정보를 제공하는 부분)
    @GetMapping("/{novelId}")
    public ResponseEntity<NovelGetResponse1> getNovelInfo1(Principal principal, @PathVariable Long novelId) {
        if (principal == null) {
            return ResponseEntity
                    .status(OK)
                    .body(novelService.getNovelInfo1(null, novelId));
        }
        return ResponseEntity
                .status(OK)
                .body(novelService.getNovelInfo1(userService.getUserOrException(Long.valueOf(principal.getName())),
                        novelId));
    }
}
