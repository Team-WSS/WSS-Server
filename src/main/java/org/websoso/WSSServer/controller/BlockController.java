package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.CREATED;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.service.BlockService;
import org.websoso.WSSServer.service.UserService;

@RestController
@RequestMapping("/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final UserService userService;
    private final BlockService blockService;

    @PostMapping
    public ResponseEntity<Void> block(Principal principal,
                                      @RequestParam("userId") Long blockedId) {
        User blocker = userService.getUserOrException(Long.valueOf(principal.getName()));
        blockService.block(blocker, blockedId);
        return ResponseEntity
                .status(CREATED)
                .build();
    }
}
