package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.block.BlocksGetResponse;
import org.websoso.WSSServer.service.BlockService;
import org.websoso.WSSServer.service.UserService;
import org.websoso.WSSServer.validation.BlockIdConstraint;
import org.websoso.WSSServer.validation.UserIdConstraint;

@RestController
@RequestMapping("/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final UserService userService;
    private final BlockService blockService;

    @PostMapping
    public ResponseEntity<Void> block(Principal principal,
                                      @RequestParam("userId") @UserIdConstraint Long blockedId) {
        User blocker = userService.getUserOrException(Long.valueOf(principal.getName()));
        blockService.block(blocker, blockedId);
        return ResponseEntity
                .status(CREATED)
                .build();
    }

    @GetMapping
    public ResponseEntity<BlocksGetResponse> getBlockList(Principal principal) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        return ResponseEntity
                .status(OK)
                .body(blockService.getBlockList(user));
    }

    @DeleteMapping("/{blockId}")
    public ResponseEntity<Void> deleteBlock(Principal principal,
                                            @PathVariable("blockId") @BlockIdConstraint Long blockId) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        blockService.deleteBlock(user, blockId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }
}
