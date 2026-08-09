package org.websoso.WSSServer.collection.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.collection.domain.CollectionAccess;
import org.websoso.WSSServer.collection.service.CollectionLikeService;
import org.websoso.WSSServer.collection.service.CollectionService;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.BlockService;

/**
 * 컬렉션 좋아요 등록·취소 유스케이스.
 * <p>
 * 두 API 모두 같은 요청을 반복해도 최종 상태가 같다. 이미 좋아요한 컬렉션에 다시 좋아요를 눌러도
 * 좋아요는 하나만 남고, 좋아요하지 않은 컬렉션의 좋아요를 취소해도 오류가 아니다.
 * 따라서 응답은 "이번 호출이 실제로 무엇을 바꿨는지"가 아니라 "요청한 최종 상태"만 알린다.
 */
@Service
@RequiredArgsConstructor
public class CollectionLikeApplication {

    private final CollectionService collectionService;
    private final CollectionLikeService collectionLikeService;
    private final BlockService blockService;

    /**
     * 좋아요를 등록한다.
     * <p>
     * 이 유스케이스에는 바깥 트랜잭션을 열지 않는다. 접근 검증(읽기) 뒤에 등록 한 건으로 끝나므로 등록과
     * 함께 원자적으로 되돌릴 후속 작업이 없고, 읽기와 쓰기가 순차적으로 각자의 트랜잭션을 쓰면 한 요청이
     * 커넥션을 동시에 두 개 점유하지 않는다.
     * <p>
     * 등록이 실패할 수 있다는 사실은 여기에 드러나지 않는다. 중복을 어떻게 흡수하고 어떤 무결성 오류를
     * 사라진 컬렉션으로 볼지는 영속화의 사정이므로 등록하는 계층이 판단해 끝낸다(정책 12.5절).
     */
    public void create(User user, Long collectionId) {

        // 1. 좋아요를 누를 수 있는 컬렉션인지 확인한다. 자신의 컬렉션에도 좋아요를 누를 수 있다.
        CollectionAccess collection = getAccessibleCollection(user.getUserId(), collectionId);

        // 2. 좋아요한 상태로 만든다. 이미 좋아요한 컬렉션이면 아무것도 바뀌지 않는다.
        //    검증에서 얻은 것은 값뿐이므로 등록에도 식별자만 넘긴다.
        collectionLikeService.create(user.getUserId(), collection.collectionId());
    }

    /**
     * 좋아요를 취소한다.
     * <p>
     * 등록과 달리 유스케이스 전체를 하나의 트랜잭션으로 묶는다. 삭제는 제약조건 위반을 유스케이스 밖에서
     * 받아야 할 이유가 없고, 접근 검증과 삭제를 한 트랜잭션에 담아도 커넥션을 하나만 쓴다.
     */
    @Transactional
    public void delete(User user, Long collectionId) {

        // 1. 등록과 같은 접근 정책을 적용해, 볼 수 없는 컬렉션의 좋아요는 취소도 할 수 없게 한다.
        getAccessibleCollection(user.getUserId(), collectionId);

        // 2. 좋아요하지 않은 컬렉션이면 지울 행이 없을 뿐 오류가 아니다.
        collectionLikeService.delete(user.getUserId(), collectionId);
    }

    /**
     * 컬렉션 상세 조회와 같은 순서로 접근 정책을 검증한다(정책 10절).
     * 차단 관계라면 공개 컬렉션이어도 접근할 수 없으므로 차단 검증을 공개 여부 검증보다 먼저 한다.
     */
    private CollectionAccess getAccessibleCollection(Long userId, Long collectionId) {
        CollectionAccess collection = collectionService.getCollectionAccessOrException(collectionId);

        blockService.validateNotBlocked(userId, collection.ownerId());
        collection.validateVisibleTo(userId);

        return collection;
    }
}
