package org.websoso.WSSServer.notification.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.notification.controller.request.NovelNotificationDeleteRequest;
import org.websoso.WSSServer.notification.controller.request.NovelNotificationUpdateRequest;
import org.websoso.WSSServer.notification.controller.response.NovelNotificationPageResponse;
import org.websoso.WSSServer.notification.controller.response.NovelNotificationResponse;
import org.websoso.WSSServer.notification.domain.NovelNotificationType;
import org.websoso.WSSServer.notification.service.NovelNotificationService;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;
import org.websoso.WSSServer.user.domain.User;

/** 작품 존재 검증과 사용자별 작품 알림 유스케이스를 조합한다. */
@Service
@RequiredArgsConstructor
public class NovelNotificationApplication {

    private final NovelServiceImpl novelService;
    private final NovelNotificationService novelNotificationService;

    /** 작품에 등록된 사용자의 두 알림 설정을 조회한다. */
    public NovelNotificationResponse getSettings(User user, Long novelId) {
        Novel novel = novelService.getNovelOrException(novelId);

        return NovelNotificationResponse.from(
                novelNotificationService.getEnabledTypes(user.getUserId(), novel.getNovelId())
        );
    }

    /** 작품의 두 알림 설정을 요청 상태에 맞게 갱신한다. */
    public void updateSettings(User user, Long novelId, NovelNotificationUpdateRequest request) {
        Novel novel = novelService.getNovelOrException(novelId);

        novelNotificationService.updateSettings(
                user,
                novel,
                request.isCompletionNotificationEnabled(),
                request.isHiatusReturnNotificationEnabled()
        );
    }

    /** 사용자가 등록한 작품 알림을 유형별로 조회한다. */
    public NovelNotificationPageResponse getSubscriptions(
            User user,
            NovelNotificationType notificationType,
            Long lastSubscriptionId,
            int size
    ) {
        return NovelNotificationPageResponse.from(
                novelNotificationService.getSubscriptions(user.getUserId(), notificationType, lastSubscriptionId, size)
        );
    }

    /** 사용자가 선택한 같은 유형의 작품 알림을 일괄 삭제한다. */
    public void deleteSubscriptions(User user, NovelNotificationDeleteRequest request) {
        novelNotificationService.deleteSubscriptions(
                user.getUserId(),
                request.notificationType(),
                request.novelIds()
        );
    }
}
