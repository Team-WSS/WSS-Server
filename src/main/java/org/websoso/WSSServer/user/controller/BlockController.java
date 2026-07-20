package org.websoso.WSSServer.user.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.application.UserBlockApplication;
import org.websoso.WSSServer.dto.block.BlocksGetResponse;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.exception.DuplicateBlockException;

@RestController
@RequestMapping("/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final UserBlockApplication userBlockApplication;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Deprecated(since = "PUT /blocks/users/{blockedUserId}/v2로 완벽 교체시")
    public ResponseEntity<Void> block(@AuthenticationPrincipal User blocker,
                                      @RequestParam("userId") @Positive Long blockedId) {
        userBlockApplication.block(blocker, blockedId);
        return ResponseEntity
                .status(CREATED)
                .build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BlocksGetResponse> getBlockList(@AuthenticationPrincipal User user) {
        return ResponseEntity
                .status(OK)
                .body(userBlockApplication.getBlockList(user));
    }

    @DeleteMapping("/{blockId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteBlock(@AuthenticationPrincipal User user,
                                            @PathVariable("blockId") @Positive Long blockId) {
        userBlockApplication.deleteBlock(user, blockId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }
}
