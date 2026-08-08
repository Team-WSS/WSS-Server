package org.websoso.WSSServer.collection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.COLLECTION_NOT_FOUND;
import static org.websoso.WSSServer.domain.common.SortCriteria.OLD;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.collection.controller.dto.CollectionNovelGetResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionNovelPreviewGetResponse;
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

        Map<Long, List<CollectionNovelPreviewGetResponse>> previews =
                service.findRecentNovelPreviews(List.of(11L, 12L), PREVIEW_SIZE);

        assertThat(previews.get(11L)).extracting(CollectionNovelPreviewGetResponse::novelId)
                .containsExactly(1L, 2L);
        assertThat(previews.get(12L)).extracting(CollectionNovelPreviewGetResponse::novelId)
                .containsExactly(3L);
    }

    @DisplayName("미리보기를 묶을 때 조회 순서를 그대로 유지한다")
    @Test
    void keepsQueryOrderWithinCollection() {
        given(collectionQueryRepository.findRecentNovelPreviewRows(List.of(11L), PREVIEW_SIZE))
                .willReturn(List.of(previewRow(11L, 9L), previewRow(11L, 3L), previewRow(11L, 7L)));

        Map<Long, List<CollectionNovelPreviewGetResponse>> previews =
                service.findRecentNovelPreviews(List.of(11L), PREVIEW_SIZE);

        assertThat(previews.get(11L)).extracting(CollectionNovelPreviewGetResponse::novelId)
                .containsExactly(9L, 3L, 7L);
    }

    @DisplayName("포함 작품이 없는 컬렉션은 미리보기 결과에 들어 있지 않다")
    @Test
    void omitsCollectionsWithoutPreview() {
        given(collectionQueryRepository.findRecentNovelPreviewRows(List.of(11L, 12L), PREVIEW_SIZE))
                .willReturn(List.of(previewRow(11L, 1L)));

        assertThat(service.findRecentNovelPreviews(List.of(11L, 12L), PREVIEW_SIZE)).doesNotContainKey(12L);
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

    @DisplayName("상세 작품은 조회 순서를 유지한 화면 표시 정보로 바뀐다")
    @Test
    void mapsNovelRowsInQueryOrder() {
        given(collectionQueryRepository.findCollectionNovelRows(COLLECTION_ID, OLD))
                .willReturn(List.of(novelRow(9L, BigDecimal.valueOf(4.55), 12L), novelRow(5L, null, null)));

        List<CollectionNovelGetResponse> novels = service.findCollectionNovels(COLLECTION_ID, OLD);

        assertThat(novels).extracting(CollectionNovelGetResponse::novelId).containsExactly(9L, 5L);
        assertThat(novels.get(0).novelRating()).isEqualTo(4.6f);
        assertThat(novels.get(0).novelRatingCount()).isEqualTo(12L);
    }

    @DisplayName("평점 통계가 없는 작품은 평점과 평점 수를 0으로 내보낸다")
    @Test
    void treatsMissingStatisticsAsZero() {
        given(collectionQueryRepository.findCollectionNovelRows(COLLECTION_ID, OLD))
                .willReturn(List.of(novelRow(5L, null, null)));

        CollectionNovelGetResponse novel = service.findCollectionNovels(COLLECTION_ID, OLD).get(0);

        assertThat(novel.novelRating()).isZero();
        assertThat(novel.novelRatingCount()).isZero();
    }

    private CollectionNovelPreviewRow previewRow(Long collectionId, Long novelId) {
        return new CollectionNovelPreviewRow(collectionId, novelId, "작품", "https://image/novel.png");
    }

    private CollectionNovelRow novelRow(Long novelId, BigDecimal averageRating, Long ratingCount) {
        return new CollectionNovelRow(novelId, "작품", "작가", "https://image/novel.png", true,
                averageRating, ratingCount);
    }

    @DisplayName("상세 정보를 그대로 돌려준다")
    @Test
    void returnsDetailRow() {
        CollectionDetailRow row = new CollectionDetailRow(COLLECTION_ID, "취향 저격 로판", null, true,
                7L, "웹소소", null, 5L);
        given(collectionQueryRepository.findCollectionDetailRow(COLLECTION_ID)).willReturn(Optional.of(row));

        assertThat(service.getCollectionDetailRowOrException(COLLECTION_ID)).isEqualTo(row);
    }
}
