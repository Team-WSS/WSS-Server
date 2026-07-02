package org.websoso.WSSServer.library.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.domain.common.ReadStatus;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.repository.UserNovelRepository;
import org.websoso.WSSServer.library.repository.projection.NovelInterestCount;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.user.domain.User;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

    private static final long NOVEL_ID = 1L;

    @InjectMocks
    private LibraryService libraryService;

    @Mock
    private UserNovelRepository userNovelRepository;

    @Mock
    private User user;

    @Mock
    private Novel novel;

    @DisplayName("평가 정보를 변경하면 UserNovel만 수정한다")
    @Test
    void updatesEvaluation() {
        UserNovel library = UserNovel.create(ReadStatus.QUIT, 2.0f, null, null, user, novel);

        libraryService.updateEvaluation(library, 4.0f, ReadStatus.WATCHING, null, null);

        assertThat(library.getUserNovelRating()).isEqualTo(4.0f);
        assertThat(library.getStatus()).isEqualTo(ReadStatus.WATCHING);
    }

    @DisplayName("관심 등록용 UserNovel이 없으면 생성한 뒤 잠금 조회한다")
    @Test
    void getsOrCreatesLibraryForInterest() {
        given(user.getUserId()).willReturn(10L);
        given(novel.getNovelId()).willReturn(NOVEL_ID);
        UserNovel library = UserNovel.create(null, 0.0f, null, null, user, novel);
        given(userNovelRepository.findByNovelIdAndUserForUpdate(NOVEL_ID, user))
                .willReturn(Optional.of(library));

        UserNovel result = libraryService.getOrCreateLibraryForInterest(user, novel);

        assertThat(result).isSameAs(library);
        then(userNovelRepository).should().insertLibraryIfAbsent(
                10L,
                NOVEL_ID,
                UserNovel.DEFAULT_RATING,
                UserNovel.DEFAULT_STATUS
        );
    }

    @DisplayName("관심 등록 시 UserNovel의 관심 상태만 변경한다")
    @Test
    void registersInterest() {
        UserNovel library = UserNovel.create(null, 0.0f, null, null, user, novel);

        libraryService.registerInterest(library);

        assertThat(library.getIsInterest()).isTrue();
    }

    @DisplayName("UserNovel 삭제를 저장소에 위임한다")
    @Test
    void deletesLibrary() {
        UserNovel library = UserNovel.create(ReadStatus.WATCHED, 4.5f, null, null, user, novel);

        libraryService.delete(library);

        then(userNovelRepository).should().delete(library);
    }

    @DisplayName("작품별 관심 수를 한 번의 집계 조회 결과로 반환한다")
    @Test
    void getsInterestCountsByNovelIds() {
        List<Long> novelIds = List.of(1L, 2L);
        given(userNovelRepository.findInterestCountsByNovelIds(novelIds))
                .willReturn(List.of(
                        new NovelInterestCount(1L, 3L),
                        new NovelInterestCount(2L, 5L)
                ));

        Map<Long, Long> result = libraryService.getInterestCountsByNovelIds(novelIds);

        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
                1L, 3L,
                2L, 5L
        ));
    }
}
