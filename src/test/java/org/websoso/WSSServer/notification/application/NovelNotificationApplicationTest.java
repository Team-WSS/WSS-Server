package org.websoso.WSSServer.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.websoso.WSSServer.notification.controller.request.NovelNotificationUpdateRequest;
import org.websoso.WSSServer.notification.controller.response.NovelNotificationResponse;
import org.websoso.WSSServer.notification.domain.NovelNotificationType;
import org.websoso.WSSServer.notification.service.NovelNotificationService;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;
import org.websoso.WSSServer.user.domain.User;

/** 작품 검증과 알림 설정 서비스 조합을 검증한다. */
@ExtendWith(MockitoExtension.class)
class NovelNotificationApplicationTest {

    private static final Long USER_ID = 1L;
    private static final Long NOVEL_ID = 10L;

    @InjectMocks
    private NovelNotificationApplication novelNotificationApplication;

    @Mock
    private NovelServiceImpl novelService;

    @Mock
    private NovelNotificationService novelNotificationService;

    /** 존재하는 작품의 두 알림 설정을 응답으로 변환하는지 검증한다. */
    @Test
    @DisplayName("작품별 알림 설정을 조회한다")
    void getsSettings() {
        User user = createUser();
        Novel novel = createNovel();
        given(novelService.getNovelOrException(NOVEL_ID)).willReturn(novel);
        given(novelNotificationService.getEnabledTypes(USER_ID, NOVEL_ID))
                .willReturn(Set.of(NovelNotificationType.COMPLETION));

        NovelNotificationResponse result = novelNotificationApplication.getSettings(user, NOVEL_ID);

        assertThat(result.isCompletionNotificationEnabled()).isTrue();
        assertThat(result.isHiatusReturnNotificationEnabled()).isFalse();
    }

    /** 작품 검증 후 요청된 두 토글 상태를 서비스에 전달하는지 검증한다. */
    @Test
    @DisplayName("작품별 알림 설정을 갱신한다")
    void updatesSettings() {
        User user = createUser();
        Novel novel = org.mockito.Mockito.mock(Novel.class);
        NovelNotificationUpdateRequest request = new NovelNotificationUpdateRequest(true, false);
        given(novelService.getNovelOrException(NOVEL_ID)).willReturn(novel);

        novelNotificationApplication.updateSettings(user, NOVEL_ID, request);

        verify(novelNotificationService).updateSettings(user, novel, true, false);
    }

    private User createUser() {
        User user = User.createBySocial("social-id", "사용자", null);
        ReflectionTestUtils.setField(user, "userId", USER_ID);
        return user;
    }

    private Novel createNovel() {
        Novel novel = org.mockito.Mockito.mock(Novel.class);
        given(novel.getNovelId()).willReturn(NOVEL_ID);
        return novel;
    }
}
