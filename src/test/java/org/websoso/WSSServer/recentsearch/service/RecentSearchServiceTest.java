package org.websoso.WSSServer.recentsearch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.websoso.WSSServer.dto.recentsearch.RecentSearchItem;
import org.websoso.WSSServer.recentsearch.domain.RecentSearch;
import org.websoso.WSSServer.recentsearch.repository.RecentSearchRepository;

@ExtendWith(MockitoExtension.class)
class RecentSearchServiceTest {

    @InjectMocks
    private RecentSearchService recentSearchService;

    @Mock
    private RecentSearchRepository repository;

    @Nested
    @DisplayName("최근 검색어 저장")
    class Add {

        @DisplayName("null 키워드는 저장하지 않는다")
        @Test
        void doesNotSaveNullKeyword() {
            // when
            recentSearchService.add(1L, null);

            // then
            verifyNoInteractions(repository);
        }

        @DisplayName("빈 문자열 키워드는 저장하지 않는다")
        @Test
        void doesNotSaveEmptyKeyword() {
            // when
            recentSearchService.add(1L, "");

            // then
            verifyNoInteractions(repository);
        }

        @DisplayName("공백만 있는 키워드는 저장하지 않는다")
        @Test
        void doesNotSaveBlankKeyword() {
            // when
            recentSearchService.add(1L, "   ");

            // then
            verifyNoInteractions(repository);
        }

        @DisplayName("정상 키워드는 앞뒤 공백을 제거한 뒤 upsert를 호출한다")
        @Test
        void savesKeywordAfterTrimming() {
            // when
            recentSearchService.add(1L, "  소설  ");

            // then
            then(repository).should().upsert(eq(1L), eq("소설"), any(LocalDateTime.class));
        }

        @DisplayName("정상 키워드 저장 후 MAX_SIZE(20) 초과분을 정리한다")
        @Test
        void trimsToMaxSizeAfterUpsert() {
            // when
            recentSearchService.add(1L, "소설");

            // then
            then(repository).should().trimToMaxSize(1L, 20);
        }

        @DisplayName("100자를 초과하는 키워드는 앞 100자만 저장한다")
        @Test
        void truncatesKeywordExceedingMaxLength() {
            // given
            String longKeyword = "가".repeat(101);

            // when
            recentSearchService.add(1L, longKeyword);

            // then
            then(repository).should().upsert(eq(1L), eq("가".repeat(100)), any(LocalDateTime.class));
        }

        @DisplayName("정확히 100자인 키워드는 그대로 저장한다")
        @Test
        void savesKeywordAtMaxLength() {
            // given
            String keyword = "a".repeat(100);

            // when
            recentSearchService.add(1L, keyword);

            // then
            then(repository).should().upsert(eq(1L), eq(keyword), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("최근 검색어 목록 조회")
    class FindRecentSearches {

        @DisplayName("검색 이력을 최신순으로 최대 20개 반환한다")
        @Test
        void returnsUpToMaxSizeRecords() {
            // given
            long userId = 1L;
            given(repository.findByUserId(eq(userId), any(Pageable.class)))
                    .willReturn(createRows(userId, 20));

            // when
            List<RecentSearchItem> result = recentSearchService.findRecentSearches(userId);

            // then
            assertThat(result).hasSize(20);
        }

        @DisplayName("검색 이력이 없으면 빈 리스트를 반환한다")
        @Test
        void returnsEmptyListWhenNoResults() {
            // given
            long userId = 1L;
            given(repository.findByUserId(eq(userId), any(Pageable.class)))
                    .willReturn(Collections.emptyList());

            // when
            List<RecentSearchItem> result = recentSearchService.findRecentSearches(userId);

            // then
            assertThat(result).isEmpty();
        }

        @DisplayName("반환된 아이템에 id, keyword, searchedAt이 올바르게 매핑된다")
        @Test
        void mapsRecentSearchFieldsCorrectly() {
            // given
            long userId = 1L;
            LocalDateTime searchedAt = LocalDateTime.of(2024, 6, 1, 12, 0);
            RecentSearch row = new RecentSearch(userId, "소설", searchedAt);
            ReflectionTestUtils.setField(row, "id", 42L);
            given(repository.findByUserId(eq(userId), any(Pageable.class)))
                    .willReturn(List.of(row));

            // when
            List<RecentSearchItem> result = recentSearchService.findRecentSearches(userId);

            // then
            RecentSearchItem item = result.get(0);
            assertThat(item.id()).isEqualTo(42L);
            assertThat(item.keyword()).isEqualTo("소설");
            assertThat(item.searchedAt()).isEqualTo(searchedAt);
        }
    }

    @Nested
    @DisplayName("최근 검색어 단건 삭제")
    class Remove {

        @DisplayName("userId와 id로 단건 삭제를 호출한다")
        @Test
        void deletesRecentSearchByIdAndUserId() {
            // when
            recentSearchService.remove(1L, 99L);

            // then
            then(repository).should().deleteByIdAndUserId(99L, 1L);
        }
    }

    @Nested
    @DisplayName("최근 검색어 전체 삭제")
    class Clear {

        @DisplayName("userId로 전체 삭제를 호출한다")
        @Test
        void deletesAllRecentSearchesByUserId() {
            // when
            recentSearchService.clear(1L);

            // then
            then(repository).should().deleteByUserId(1L);
        }
    }

    @Nested
    @DisplayName("만료 검색 기록 배치 삭제")
    class DeleteBatch {

        @DisplayName("threshold와 batchSize를 레포지토리에 그대로 전달하고 삭제 수를 반환한다")
        @Test
        void delegatesToRepositoryAndReturnsDeletedCount() {
            // given
            LocalDateTime threshold = LocalDateTime.of(2024, 1, 1, 0, 0);
            given(repository.deleteOlderThan(threshold, 1000)).willReturn(500);

            // when
            int deleted = recentSearchService.deleteBatch(threshold, 1000);

            // then
            then(repository).should().deleteOlderThan(threshold, 1000);
            assertThat(deleted).isEqualTo(500);
        }

        @DisplayName("삭제된 행이 없으면 0을 반환한다")
        @Test
        void returnsZeroWhenNothingDeleted() {
            // given
            LocalDateTime threshold = LocalDateTime.of(2024, 1, 1, 0, 0);
            given(repository.deleteOlderThan(any(), anyInt())).willReturn(0);

            // when
            int deleted = recentSearchService.deleteBatch(threshold, 1000);

            // then
            assertThat(deleted).isEqualTo(0);
        }
    }

    private List<RecentSearch> createRows(long userId, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> new RecentSearch(userId, "키워드" + i, LocalDateTime.now().minusMinutes(i)))
                .toList();
    }
}
