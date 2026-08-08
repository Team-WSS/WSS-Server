package org.websoso.WSSServer.collection.service;

import static org.websoso.WSSServer.collection.exception.CustomCollectionError.COLLECTION_NOT_FOUND;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.DUPLICATE_COLLECTION_NOVEL;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.collection.domain.Collection;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.collection.exception.DuplicateCollectionNovelException;
import org.websoso.WSSServer.collection.repository.CollectionNovelConstraintViolationDetector;
import org.websoso.WSSServer.collection.repository.CollectionRepository;

/**
 * 컬렉션 영속화를 담당하며 Repository 접근을 캡슐화한다.
 * <p>
 * 유스케이스 전체의 트랜잭션 경계는 Application 계층이 소유한다. 이 클래스의 메서드는 메서드 단위로
 * 필요한 트랜잭션 속성을 명시하되, 기본 전파 속성을 사용하므로 Application이 연 트랜잭션이 있으면
 * 새 트랜잭션을 열지 않고 거기에 참여한다. 따라서 쓰기 잠금, 지연 로딩, {@code flush}, 연쇄 삭제는
 * 여전히 Application이 연 하나의 트랜잭션 안에서 동작한다.
 */
@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionNovelConstraintViolationDetector constraintViolationDetector;

    @Transactional
    public Collection create(Collection collection) {
        return translateDuplicateNovel(() -> collectionRepository.saveAndFlush(collection));
    }

    /**
     * 수정 결과를 즉시 반영해, 동시에 같은 작품을 추가한 요청이 만든 유니크 제약 위반을
     * 컬렉션 도메인 예외로 바꿀 수 있는 시점에서 확인한다.
     */
    @Transactional
    public void flushChanges() {
        translateDuplicateNovel(() -> {
            collectionRepository.flush();
            return null;
        });
    }

    @Transactional
    public void delete(Collection collection) {
        collectionRepository.delete(collection);
    }

    /**
     * 수정·삭제 대상 컬렉션을 잠그고 가져온다.
     * <p>
     * 같은 컬렉션에 대한 동시 수정 요청이 각자 읽은 포함 작품을 기준으로 delta를 계산해
     * 서로의 변경을 덮어쓰는 것을 막는다.
     * <p>
     * 조회만 하지만 비관적 쓰기 잠금을 걸므로 읽기 전용이 아닌 쓰기 트랜잭션이 필요하다.
     */
    @Transactional
    public Collection getOwnedCollectionOrException(Long collectionId, Long userId) {
        Collection collection = collectionRepository.findByIdForUpdate(collectionId)
                .orElseThrow(() -> new CustomCollectionException(
                        COLLECTION_NOT_FOUND,
                        "collection with the given id is not found"
                ));
        collection.validateOwner(userId);

        // 잠근 컬렉션의 포함 작품을 한 번의 조회로 초기화한다.
        // 같은 트랜잭션의 영속성 컨텍스트이므로 위에서 잠근 인스턴스가 그대로 반환된다.
        return getCollectionOrException(collectionId);
    }

    /**
     * 잠금 없이 컬렉션과 포함 작품을 조회한다. 조회 전용 경로에서 사용한다.
     */
    @Transactional(readOnly = true)
    public Collection getCollectionOrException(Long collectionId) {
        return collectionRepository.findByIdWithNovels(collectionId)
                .orElseThrow(() -> new CustomCollectionException(
                        COLLECTION_NOT_FOUND,
                        "collection with the given id is not found"
                ));
    }

    /**
     * 회원 탈퇴 시 해당 사용자가 만든 컬렉션의 소유자를 알 수 없는 사용자로 넘긴다.
     * <p>
     * {@code collection.user_id}가 NOT NULL이고 참조 DDL에 {@code ON DELETE CASCADE}가 없으므로
     * 사용자 삭제 전에 애플리케이션이 소유자를 옮긴다. 컬렉션과 포함 작품은 지우지 않고 보존하며,
     * 피드·댓글 작성자 익명화와 같은 방식이다.
     */
    @Transactional
    public void updateOwnerToUnknown(Long userId) {
        collectionRepository.updateOwnerToUnknown(userId);
    }

    private <T> T translateDuplicateNovel(Supplier<T> persistence) {
        try {
            return persistence.get();
        } catch (DataIntegrityViolationException exception) {
            if (constraintViolationDetector.isDuplicateCollectionNovel(exception)) {
                throw new DuplicateCollectionNovelException(
                        DUPLICATE_COLLECTION_NOVEL,
                        "collection cannot contain the same novel more than once"
                );
            }
            throw exception;
        }
    }
}
