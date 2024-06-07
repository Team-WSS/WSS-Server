package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.userNovel.UserNovelCreateRequest;
import org.websoso.WSSServer.service.UserNovelService;
import org.websoso.WSSServer.service.UserService;

@RequestMapping("/user-novels")
@RestController
@RequiredArgsConstructor
public class UserNovelController {

    private final UserService userService;
    private final UserNovelService userNovelService;

    @PostMapping
    public ResponseEntity<Void> createUserNovel(Principal principal,
                                                @Valid @RequestBody UserNovelCreateRequest request){
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        userNovelService.createUserNovel(user, request);

        return ResponseEntity
                .status(CREATED)
                .build();
    }

}
