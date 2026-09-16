package org.websoso.WSSServer.collection.service;

import static org.websoso.WSSServer.collection.exception.CustomCollectionError.COLLECTION_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.collection.repository.CollectionLikeConstraintViolationDetector;
import org.websoso.WSSServer.collection.repository.CollectionLikeRepository;

/**
 * 컬렉션 좋아요 영속화를 담당하며 Repository 접근을 캡슐화한다.
 * <p>
 * 다른 Service를 호출하지 않는다. 여러 Service를 조합하는 것은 Application의 책임이고,
 * 이 계층은 좋아요 테이블 하나만 다룬다.
 * <p>
 * 모든 메서드가 각자 필요한 트랜잭션 속성을 애너테이션으로 명시하며 전파 속성은 모두 기본값이다.
 * 저장 실패를 컬렉션 도메인 예외로 옮기는 책임도 이 계층이 소유한다. 어떤 무결성 오류를 무엇으로 볼지는
 * 영속화의 사정이지 유스케이스의 사정이 아니므로, 유스케이스는 등록을 호출할 뿐 제약조건이나 무결성
 * 예외를 알지 않는다(정책 12.5절).
 */
@Service
@RequiredArgsConstructor
public class CollectionLikeService {

    private final CollectionLikeRepository collectionLikeRepository;
    private final CollectionLikeConstraintViolationDetector constraintViolationDetector;

    /**
     * 좋아요를 등록한다. 이미 좋아요한 컬렉션이면 아무것도 바뀌지 않는다.
     * <p>
     * 중복은 예외가 아니다. upsert 한 문장이 DB에서 정상 종료하므로 사전 존재 확인도, 유니크 제약조건
     * 위반을 잡아 멱등하게 넘기는 처리도 없다. 잡을 예외가 없으니 트랜잭션 경계도 애너테이션 하나로 끝난다.
     * <p>
     * 등록과 컬렉션 삭제의 경쟁은 DB의 외래 키 잠금 순서에 맡긴다(정책 16.7절). 등록이 먼저면 삭제가
     * 기다렸다가 그 좋아요까지 함께 지우고, 삭제가 먼저면 등록이 외래 키 위반으로 끝난다.
     * 후자는 서버 오류가 아니라 컬렉션을 찾을 수 없는 요청이므로 컬렉션 도메인 예외로 바꾼다.
     * 예외를 던지므로 이 트랜잭션은 롤백되고, 판별하지 못한 무결성 오류는 그대로 재전파한다.
     * <p>
     * 이 호출이 실제로 행을 넣었는지는 알리지 않는다. MySQL의 갱신 행 수는 커넥터 설정에 따라 달라져
     * 중복 여부의 근거가 될 수 없고, 등록 API는 최종 상태(204)만 보장한다.
     */
    @Transactional
    public void create(Long userId, Long collectionId) {
        try {
            collectionLikeRepository.upsertLike(userId, collectionId);
        } catch (DataIntegrityViolationException exception) {
            throw translateMissingCollection(exception);
        }
    }

    private RuntimeException translateMissingCollection(DataIntegrityViolationException exception) {
        if (constraintViolationDetector.isMissingCollection(exception)) {
            return new CustomCollectionException(
                    COLLECTION_NOT_FOUND,
                    "collection with the given id was deleted while the like was being saved"
            );
        }

        return exception;
    }

    /**
     * 좋아요를 취소한다. 좋아요하지 않은 컬렉션이면 지울 행이 없을 뿐 오류가 아니다.
     *
     * @return 이 호출로 좋아요가 지워졌으면 {@code true}, 이미 취소된 상태였으면 {@code false}
     */
    @Transactional
    public boolean delete(Long userId, Long collectionId) {
        return collectionLikeRepository.deleteLike(userId, collectionId) > 0;
    }

    /**
     * 컬렉션 삭제 시 해당 컬렉션의 좋아요를 모두 지운다. 좋아요는 컬렉션과 별도 애그리거트이므로
     * {@code cascade}로 따라 지워지지 않고 컬렉션 삭제 유스케이스가 이 메서드로 먼저 정리한다.
     */
    @Transactional
    public long deleteAllByCollectionId(Long collectionId) {
        return collectionLikeRepository.deleteAllByCollectionId(collectionId);
    }

    @Transactional(readOnly = true)
    public long countByCollectionId(Long collectionId) {
        return collectionLikeRepository.countByCollectionCollectionId(collectionId);
    }

    @Transactional(readOnly = true)
    public boolean isLikedBy(Long userId, Long collectionId) {
        if (userId == null) {
            return false;
        }

        return collectionLikeRepository.existsByUserIdAndCollectionCollectionId(userId, collectionId);
    }
}
