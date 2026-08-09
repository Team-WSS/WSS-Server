package org.websoso.WSSServer.collection.application;

import static org.websoso.WSSServer.exception.error.CustomNovelError.NOVEL_NOT_FOUND;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.collection.controller.dto.CollectionCreateRequest;
import org.websoso.WSSServer.collection.controller.dto.CollectionCreateResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionUpdateRequest;
import org.websoso.WSSServer.collection.domain.Collection;
import org.websoso.WSSServer.collection.service.CollectionLikeService;
import org.websoso.WSSServer.collection.service.CollectionService;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;
import org.websoso.WSSServer.user.domain.User;

@Service
@RequiredArgsConstructor
@Transactional
public class CollectionManagementApplication {

    private final CollectionService collectionService;
    private final CollectionLikeService collectionLikeService;
    private final NovelServiceImpl novelService;

    public CollectionCreateResponse create(User user, CollectionCreateRequest request) {

        // 1. 작품 수, 중복 작품, 대표 작품 포함 여부를 먼저 검증한다.
        Collection.validateNovelPolicy(request.novelIds(), request.representativeNovelId());

        // 2. 요청한 작품이 모두 존재하는지 한 번의 조회로 검증한다.
        List<Novel> novels = getExistingNovelsInRequestedOrder(request.novelIds());

        // 3. 컬렉션 생성
        Collection collection = Collection.create(
                user,
                request.name(),
                request.description(),
                request.isPublic(),
                novels,
                request.representativeNovelId()
        );

        return CollectionCreateResponse.of(collectionService.create(collection));
    }

    public void update(User user, Long collectionId, CollectionUpdateRequest request) {

        // 1. 존재하는 컬렉션인지, 요청한 사용자가 소유자인지 확인한다.
        Collection collection = collectionService.getOwnedCollectionOrException(collectionId, user.getUserId());

        // 2. 작품 수, 중복 작품, 대표 작품 포함 여부를 검증한다.
        Collection.validateNovelPolicy(request.novelIds(), request.representativeNovelId());

        // 3. 요청한 작품이 모두 존재하는지 한 번의 조회로 검증한다.
        List<Novel> novels = getExistingNovelsInRequestedOrder(request.novelIds());

        // 4. 계속 포함되는 작품의 추가 시점을 보존하는 delta 갱신
        collection.update(
                request.name(),
                request.description(),
                request.isPublic(),
                novels,
                request.representativeNovelId()
        );

        collectionService.flushChanges();
    }

    public void delete(User user, Long collectionId) {

        // 1. 존재하는 컬렉션인지, 요청한 사용자가 소유자인지 확인한다.
        Collection collection = collectionService.getOwnedCollectionOrException(collectionId, user.getUserId());

        // 2. 컬렉션 좋아요는 별도 애그리거트라 cascade로 따라 지워지지 않으므로 컬렉션보다 먼저 지운다.
        //    좋아요가 남아 있으면 외래 키 때문에 컬렉션 삭제 자체가 실패한다.
        collectionLikeService.deleteAllByCollectionId(collectionId);

        // 3. 컬렉션 작품은 cascade + orphanRemoval로 함께 삭제된다.
        collectionService.delete(collection);
    }

    /**
     * 요청한 작품을 한 번의 조회로 가져오고, 하나라도 없으면 작품 도메인 예외로 처리한다.
     * 요청 배열 순서를 유지해 같은 요청으로 추가된 작품끼리도 식별자 순서가 결정적으로 정해지도록 한다.
     */
    private List<Novel> getExistingNovelsInRequestedOrder(List<Long> novelIds) {
        Map<Long, Novel> novelsById = novelService.findAllByIds(novelIds).stream()
                .collect(Collectors.toMap(Novel::getNovelId, Function.identity()));

        return novelIds.stream()
                .map(novelId -> {
                    Novel novel = novelsById.get(novelId);
                    if (novel == null) {
                        throw new CustomNovelException(NOVEL_NOT_FOUND,
                                "novel with id " + novelId + " is not found");
                    }
                    return novel;
                })
                .toList();
    }
}
