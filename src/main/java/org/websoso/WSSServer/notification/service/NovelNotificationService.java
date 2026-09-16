package org.websoso.WSSServer.notification.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.notification.domain.NovelNotificationSubscription;
import org.websoso.WSSServer.notification.domain.NovelNotificationType;
import org.websoso.WSSServer.notification.repository.NovelNotificationSubscriptionRepository;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.user.domain.User;

/** 작품 알림 구독의 멱등 등록, 상태 조회, 목록 조회와 삭제를 처리한다. */
@Service
@RequiredArgsConstructor
public class NovelNotificationService {

    private final NovelNotificationSubscriptionRepository subscriptionRepository;

    /** 두 작품 알림을 요청된 활성화 상태와 동일하게 맞춘다. */
    @Transactional
    public void updateSettings(
            User user,
            Novel novel,
            boolean isCompletionEnabled,
            boolean isHiatusReturnEnabled
    ) {
        List<NovelNotificationSubscription> subscriptions = subscriptionRepository
                .findAllByUser_UserIdAndNovel_NovelId(user.getUserId(), novel.getNovelId());

        synchronizeSubscription(user, novel, NovelNotificationType.COMPLETION, isCompletionEnabled, subscriptions);
        synchronizeSubscription(user, novel, NovelNotificationType.HIATUS_RETURN, isHiatusReturnEnabled,
                subscriptions);
    }

    /**
     * 작품에 현재 등록된 사용자의 알림 유형을 조회한다.
     * 발송이 끝난 구독은 제외한다. 이미 알림을 받은 뒤에도 토글이 켜져 있으면
     * 다시 알림을 받는 것으로 오해하기 때문이다.
     */
    @Transactional(readOnly = true)
    public Set<NovelNotificationType> getEnabledTypes(Long userId, Long novelId) {
        Set<NovelNotificationType> enabledTypes = subscriptionRepository
                .findAllByUser_UserIdAndNovel_NovelId(userId, novelId)
                .stream()
                .filter(subscription -> !subscription.isSent())
                .map(NovelNotificationSubscription::getNotificationType)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(NovelNotificationType.class)));

        return Set.copyOf(enabledTypes);
    }

    /** 사용자가 등록한 작품 알림 중 아직 발송되지 않은 것을 유형별 최신순으로 조회한다. */
    @Transactional(readOnly = true)
    public Slice<NovelNotificationSubscription> getSubscriptions(
            Long userId,
            NovelNotificationType notificationType,
            Long lastSubscriptionId,
            int size
    ) {
        return subscriptionRepository.findSubscriptions(
                userId,
                notificationType,
                lastSubscriptionId,
                PageRequest.of(0, size)
        );
    }

    /** 사용자가 선택한 같은 유형의 작품 알림을 일괄 삭제한다. */
    @Transactional
    public void deleteSubscriptions(
            Long userId,
            NovelNotificationType notificationType,
            List<Long> novelIds
    ) {
        subscriptionRepository.deleteSubscriptions(userId, notificationType, novelIds);
    }

    /**
     * 단일 알림 유형을 요청 상태에 맞춘다.
     * 이미 요청과 같은 상태면 아무것도 하지 않아, 같은 요청을 반복해도 결과가 같다.
     */
    private void synchronizeSubscription(
            User user,
            Novel novel,
            NovelNotificationType notificationType,
            boolean enabled,
            List<NovelNotificationSubscription> subscriptions
    ) {
        NovelNotificationSubscription subscription = subscriptions.stream()
                .filter(it -> it.getNotificationType() == notificationType)
                .findFirst()
                .orElse(null);

        if (enabled) {
            enableSubscription(user, novel, notificationType, subscription);
            return;
        }

        if (subscription != null) {
            subscriptionRepository.delete(subscription);
        }
    }

    /**
     * 알림을 켠다. 발송이 끝난 구독은 지우고 새로 등록해 등록일을 갱신한다.
     * 사용자가 알림을 받은 뒤 다시 켠 것이므로 지난 등록일을 유지하면
     * 어드민이 이전 발송 시점 기준으로 판단하게 된다.
     * (user, novel, type) UNIQUE 제약이 있어 새로 저장하기 전에 삭제를 먼저 반영한다.
     */
    private void enableSubscription(
            User user,
            Novel novel,
            NovelNotificationType notificationType,
            NovelNotificationSubscription subscription
    ) {
        if (subscription != null && !subscription.isSent()) {
            return;
        }

        if (subscription != null) {
            subscriptionRepository.delete(subscription);
            subscriptionRepository.flush();
        }

        subscriptionRepository.save(NovelNotificationSubscription.create(user, novel, notificationType));
    }
}
