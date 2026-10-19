package org.websoso.WSSServer.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.websoso.WSSServer.library.service.LibraryService;
import org.websoso.WSSServer.novel.domain.PopularNovel;
import org.websoso.WSSServer.novel.service.PopularNovelService;

@ExtendWith(MockitoExtension.class)
class PopularNovelSchedulerTest {

    private static final PageRequest TOP_30 = PageRequest.of(0, 30);

    @InjectMocks
    private PopularNovelScheduler scheduler;

    @Mock
    private PopularNovelService popularNovelService;

    @Mock
    private LibraryService libraryService;

    @Captor
    private ArgumentCaptor<List<PopularNovel>> popularNovelsCaptor;

    @DisplayName("후보가 없으면 기존 인기작 유무와 관계없이 저장과 삭제 없이 반환한다")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void returnsWithoutSavingOrDeletingWhenNoCandidates(boolean hasPreviousPopularNovels) {
        List<PopularNovel> previous = previousPopularNovels(hasPreviousPopularNovels);
        given(popularNovelService.getPopularNovels()).willReturn(previous);
        given(libraryService.getTodayPopularNovelIds(TOP_30)).willReturn(List.of());

        scheduler.updatePopularNovels();

        then(popularNovelService).should().getPopularNovels();
        then(popularNovelService).shouldHaveNoMoreInteractions();
        then(libraryService).should().getTodayPopularNovelIds(TOP_30);
        then(libraryService).shouldHaveNoMoreInteractions();
    }

    @DisplayName("후보가 있으면 조회 순서대로 새 인기작을 저장한 다음 기존 인기작만 삭제한다")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void savesCandidatesBeforeDeletingPreviousPopularNovels(boolean hasPreviousPopularNovels) {
        List<PopularNovel> previous = previousPopularNovels(hasPreviousPopularNovels);
        given(popularNovelService.getPopularNovels()).willReturn(previous);
        given(libraryService.getTodayPopularNovelIds(TOP_30)).willReturn(List.of(3L, 2L, 4L));

        scheduler.updatePopularNovels();

        InOrder order = inOrder(popularNovelService);
        then(popularNovelService).should(order).getPopularNovels();
        then(popularNovelService).should(order).saveAll(popularNovelsCaptor.capture());
        then(popularNovelService).should(order).deleteAll(previous);
        then(popularNovelService).shouldHaveNoMoreInteractions();
        then(libraryService).should().getTodayPopularNovelIds(TOP_30);
        then(libraryService).shouldHaveNoMoreInteractions();

        assertThat(popularNovelsCaptor.getValue())
                .extracting(PopularNovel::getNovelId)
                .containsExactly(3L, 2L, 4L);
        assertThat(popularNovelsCaptor.getValue())
                .extracting(PopularNovel::getPopularNovelId)
                .containsOnlyNulls();
    }

    @DisplayName("후보 조회가 실패하면 기존 인기작을 저장하거나 삭제하지 않는다")
    @Test
    void doesNotChangePopularNovelsWhenCandidateLookupFails() {
        given(popularNovelService.getPopularNovels()).willReturn(previousPopularNovels(true));
        IllegalStateException failure = new IllegalStateException("candidate lookup failed");
        given(libraryService.getTodayPopularNovelIds(TOP_30)).willThrow(failure);

        assertThatThrownBy(scheduler::updatePopularNovels).isSameAs(failure);

        then(popularNovelService).should().getPopularNovels();
        then(popularNovelService).shouldHaveNoMoreInteractions();
    }

    @DisplayName("새 인기작 저장이 실패하면 기존 인기작을 삭제하지 않는다")
    @Test
    void doesNotDeletePreviousPopularNovelsWhenSavingFails() {
        given(popularNovelService.getPopularNovels()).willReturn(previousPopularNovels(true));
        given(libraryService.getTodayPopularNovelIds(TOP_30)).willReturn(List.of(3L));
        IllegalStateException failure = new IllegalStateException("save failed");
        willThrow(failure).given(popularNovelService).saveAll(anyList());

        assertThatThrownBy(scheduler::updatePopularNovels).isSameAs(failure);

        then(popularNovelService).should().getPopularNovels();
        then(popularNovelService).should().saveAll(popularNovelsCaptor.capture());
        then(popularNovelService).shouldHaveNoMoreInteractions();
    }

    private List<PopularNovel> previousPopularNovels(boolean hasPreviousPopularNovels) {
        return hasPreviousPopularNovels
                ? List.of(PopularNovel.from(1L), PopularNovel.from(2L))
                : List.of();
    }
}
