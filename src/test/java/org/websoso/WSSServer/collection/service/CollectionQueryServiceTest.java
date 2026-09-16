package org.websoso.WSSServer.collection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.COLLECTION_NOT_FOUND;
import static org.websoso.WSSServer.domain.common.SortCriteria.OLD;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.collection.controller.dto.CollectionNovelSummaryGetResponse;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.collection.repository.CollectionQueryRepository;
import org.websoso.WSSServer.collection.repository.projection.CollectionDetailRow;
import org.websoso.WSSServer.collection.repository.projection.CollectionNovelPreviewRow;
import org.websoso.WSSServer.collection.repository.projection.CollectionNovelRow;

@ExtendWith(MockitoExtension.class)
class CollectionQueryServiceTest {

    private static final long COLLECTION_ID = 100L;
    private static final int PREVIEW_SIZE = 5;

    @InjectMocks
    private CollectionQueryService service;

    @Mock
    private CollectionQueryRepository collectionQueryRepository;

    @DisplayName("한 번의 조회로 읽은 미리보기를 컬렉션별로 묶는다")
    @Test
    void groupsPreviewsByCollection() {
        given(collectionQueryRepository.findRecentNovelPreviewRows(List.of(11L, 12L), PREVIEW_SIZE))
                .willReturn(List.of(previewRow(11L, 1L), previewRow(11L, 2L), previewRow(12L, 3L)));

        Map<Long, List<CollectionNovelSummaryGetResponse>> previews =
                service.findRecentNovelPreviews(List.of(11L, 12L), PREVIEW_SIZE);

        assertThat(previews.get(11L)).extracting(CollectionNovelSummaryGetResponse::novelId)
                .containsExactly(1L, 2L);
        assertThat(previews.get(12L)).extracting(CollectionNovelSummaryGetResponse::novelId)
                .containsExactly(3L);
    }

    @DisplayName("미리보기를 묶을 때 조회 순서를 그대로 유지한다")
    @Test
    void keepsQueryOrderWithinCollection() {
        given(collectionQueryRepository.findRecentNovelPreviewRows(List.of(11L), PREVIEW_SIZE))
                .willReturn(List.of(previewRow(11L, 9L), previewRow(11L, 3L), previewRow(11L, 7L)));

        Map<Long, List<CollectionNovelSummaryGetResponse>> previews =
                service.findRecentNovelPreviews(List.of(11L), PREVIEW_SIZE);

        assertThat(previews.get(11L)).extracting(CollectionNovelSummaryGetResponse::novelId)
                .containsExactly(9L, 3L, 7L);
    }

    @DisplayName("포함 작품이 없는 컬렉션은 미리보기 결과에 들어 있지 않다")
    @Test
    void omitsCollectionsWithoutPreview() {
        given(collectionQueryRepository.findRecentNovelPreviewRows(List.of(11L, 12L), PREVIEW_SIZE))
                .willReturn(List.of(previewRow(11L, 1L)));

        assertThat(service.findRecentNovelPreviews(List.of(11L, 12L), PREVIEW_SIZE)).doesNotContainKey(12L);
    }

    @DisplayName("미리보기도 상세와 같은 작품 요약으로 내보낸다")
    @Test
    void previewCarriesSharedNovelSummary() {
        given(collectionQueryRepository.findRecentNovelPreviewRows(List.of(11L), PREVIEW_SIZE))
                .willReturn(List.of(previewRow(11L, 9L)));

        CollectionNovelSummaryGetResponse preview =
                service.findRecentNovelPreviews(List.of(11L), PREVIEW_SIZE).get(11L).get(0);

        assertThat(preview).isEqualTo(new CollectionNovelSummaryGetResponse(
                9L, "작품", "https://image/novel.png", "작가"));
    }

    @DisplayName("존재하지 않는 컬렉션 상세는 컬렉션 도메인 오류로 처리한다")
    @Test
    void detailThrowsWhenCollectionMissing() {
        given(collectionQueryRepository.findCollectionDetailRow(COLLECTION_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCollectionDetailRowOrException(COLLECTION_ID))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(COLLECTION_NOT_FOUND);
    }

    @DisplayName("상세 작품은 조회 순서를 유지한 작품 요약으로 바뀐다")
    @Test
    void mapsNovelRowsInQueryOrder() {
        given(collectionQueryRepository.findCollectionNovelRows(COLLECTION_ID, OLD))
                .willReturn(List.of(novelRow(9L), novelRow(5L)));

        List<CollectionNovelSummaryGetResponse> novels = service.findCollectionNovels(COLLECTION_ID, OLD);

        assertThat(novels).extracting(CollectionNovelSummaryGetResponse::novelId).containsExactly(9L, 5L);
    }

    @DisplayName("상세 작품 요약은 목록 미리보기와 같은 네 가지 값만 담는다")
    @Test
    void detailNovelCarriesSharedNovelSummary() {
        given(collectionQueryRepository.findCollectionNovelRows(COLLECTION_ID, OLD))
                .willReturn(List.of(novelRow(9L)));

        CollectionNovelSummaryGetResponse novel = service.findCollectionNovels(COLLECTION_ID, OLD).get(0);

        assertThat(novel).isEqualTo(new CollectionNovelSummaryGetResponse(
                9L, "작품", "https://image/novel.png", "작가"));
    }

    private CollectionNovelPreviewRow previewRow(Long collectionId, Long novelId) {
        return new CollectionNovelPreviewRow(collectionId, novelId, "작품", "https://image/novel.png", "작가");
    }

    private CollectionNovelRow novelRow(Long novelId) {
        return new CollectionNovelRow(novelId, "작품", "https://image/novel.png", "작가");
    }

    @DisplayName("상세 정보를 그대로 돌려준다")
    @Test
    void returnsDetailRow() {
        CollectionDetailRow row = new CollectionDetailRow(COLLECTION_ID, "취향 저격 로판", null, true,
                7L, "웹소소", "https://image/avatar.png", 5L, 3L);
        given(collectionQueryRepository.findCollectionDetailRow(COLLECTION_ID)).willReturn(Optional.of(row));

        assertThat(service.getCollectionDetailRowOrException(COLLECTION_ID)).isEqualTo(row);
    }
}
