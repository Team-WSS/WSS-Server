package org.websoso.WSSServer.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.websoso.WSSServer.user.exception.CustomBlockError.ALREADY_BLOCKED;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.websoso.WSSServer.application.UserBlockApplication;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.exception.DuplicateBlockException;

class BlockControllerTest {

    private final UserBlockApplication userBlockApplication = Mockito.mock(UserBlockApplication.class);
    private final BlockController blockController = new BlockController(userBlockApplication);
    private final User blocker = Mockito.mock(User.class);

    @DisplayName("기존 차단 API는 201 Created를 반환한다")
    @Test
    void returnsCreatedForLegacyBlock() {
        ResponseEntity<Void> response = blockController.block(blocker, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        then(userBlockApplication).should().block(blocker, 1L);
    }

    @DisplayName("v2 차단 API는 204 No Content를 반환한다")
    @Test
    void returnsNoContentForV2Block() {
        ResponseEntity<Void> response = blockController.blockV2(blocker, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        then(userBlockApplication).should().blockV2(blocker, 1L);
    }

    @DisplayName("v2 차단 API는 중복 차단도 204 No Content를 반환한다")
    @Test
    void returnsNoContentForDuplicateV2Block() {
        willThrow(new DuplicateBlockException(ALREADY_BLOCKED, "already blocked"))
                .given(userBlockApplication).blockV2(blocker, 1L);

        ResponseEntity<Void> response = blockController.blockV2(blocker, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
