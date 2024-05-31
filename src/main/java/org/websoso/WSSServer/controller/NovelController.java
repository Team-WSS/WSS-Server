package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.OK;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.novel.NovelGetResponse;
import org.websoso.WSSServer.service.NovelService;
import org.websoso.WSSServer.service.UserService;

@RestController
@RequestMapping("/novels")
@RequiredArgsConstructor
public class NovelController {

    private final NovelService novelService;
    private final UserService userService;

    @GetMapping("/{novelId}")
    public ResponseEntity<NovelGetResponse> getNovelInfo1(Principal principal, @PathVariable Long novelId) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        return ResponseEntity
                .status(OK)
                .body(novelService.getNovelInfo1(user, novelId));
    }
}
