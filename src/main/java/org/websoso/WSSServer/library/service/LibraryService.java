package org.websoso.WSSServer.library.service;

import static org.websoso.WSSServer.domain.common.ReadStatus.QUIT;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHED;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHING;
import static org.websoso.WSSServer.exception.error.CustomUserNovelError.USER_NOVEL_NOT_FOUND;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.domain.common.ReadStatus;
import org.websoso.WSSServer.exception.exception.CustomUserNovelException;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.domain.UserNovelStatistics;
import org.websoso.WSSServer.library.repository.UserNovelRepository;
import org.websoso.WSSServer.library.repository.projection.NovelInterestCount;
import org.websoso.WSSServer.novel.domain.Novel;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final UserNovelRepository userNovelRepository;

    // TODO: novelId로 불러옴
    @Transactional
    public UserNovel getLibraryForUpdateOrException(User user, Long novelId) {
        return userNovelRepository.findByNovelIdAndUserForUpdate(novelId, user)
                .orElseThrow(() -> new CustomUserNovelException(USER_NOVEL_NOT_FOUND,
                        "user novel with the given user and novel is not found"));
    }

    // TODO: Novel 객체로 불러옴
    // TODO: 사용자 객체가 Null이면 Null로 반환함 헷갈리수도?
    @Transactional(readOnly = true)
    public UserNovel getLibraryOrNull(User user, Novel novel) {
        if (user == null) {
            return null;
        }

        return userNovelRepository.findByNovel_NovelIdAndUser(novel.getNovelId(), user).orElse(null);
    }

    @Transactional(readOnly = true)
    public UserNovel getLibraryOrNull(User user, long novelId) {
        if (user == null) {
            return null;
        }

        return userNovelRepository.findByNovel_NovelIdAndUser(novelId, user).orElse(null);
    }

    @Transactional
    public UserNovel createLibrary(ReadStatus status, Float userNovelRating, LocalDate startDate, LocalDate endDate,
                                   User user, Novel novel) {
        UserNovel userNovel = userNovelRepository.save(UserNovel.create(
                status,
                userNovelRating,
                startDate,
                endDate,
                user,
                novel));
        updateNovelStatistics(UserNovelStatistics.EMPTY, UserNovelStatistics.from(userNovel), novel);
        return userNovel;
    }

    /**
     * <p>관심있어요를 등록한다.</p>
     * 서재 내역이 없다면, 서재 내역을 생성하면서 등록한다.
     *
     * @param user  사용자 Entity
     * @param novel 서재 Entity
     */
    @Transactional
    public void registerInterest(User user, Novel novel) {
        int insertedCount = userNovelRepository.insertInterestIfAbsent(
                user.getUserId(),
                novel.getNovelId(),
                UserNovel.DEFAULT_RATING,
                UserNovel.DEFAULT_STATUS
        );

        UserNovel library = userNovelRepository.findByNovelIdAndUserForUpdate(novel.getNovelId(), user)
                .orElseThrow(() -> new IllegalStateException("inserted user novel could not be found"));

        UserNovelStatistics before = insertedCount == 1
                ? UserNovelStatistics.EMPTY
                : UserNovelStatistics.from(library);

        library.markAsInterested();
        updateNovelStatistics(before, UserNovelStatistics.from(library), novel);
    }

    /**
     * <p>관심있어요를 해제한다.</p>
     * 만약, 서재 정보가 없다면 삭제한다.
     *
     * @param user    사용자 Entity
     * @param novelId 소설 ID
     */
    @Transactional
    public void unregisterInterest(User user, Long novelId) {
        UserNovel library = userNovelRepository.findByNovelIdAndUserForUpdate(novelId, user).orElse(null);

        if (library == null) {
            return;
        }

        if (Boolean.FALSE.equals(library.getIsInterest())) {
            return;
        }

        UserNovelStatistics before = UserNovelStatistics.from(library);
        library.unmarkAsInterested();

        if (library.isSafeToDelete()) {
            userNovelRepository.delete(library);
            updateNovelStatistics(before, UserNovelStatistics.EMPTY, library.getNovel());
            return;
        }

        updateNovelStatistics(before, UserNovelStatistics.from(library), library.getNovel());
    }

    @Transactional
    public void updateEvaluation(UserNovel library, Float userNovelRating, ReadStatus status, LocalDate startDate,
                                 LocalDate endDate) {
        UserNovelStatistics before = UserNovelStatistics.from(library);
        library.updateUserNovel(userNovelRating, status, startDate, endDate);
        updateNovelStatistics(before, UserNovelStatistics.from(library), library.getNovel());
    }

    @Transactional
    public void deleteEvaluation(UserNovel library) {
        UserNovelStatistics before = UserNovelStatistics.from(library);
        library.deleteEvaluation();
        updateNovelStatistics(before, UserNovelStatistics.from(library), library.getNovel());
    }

    @Transactional
    public void delete(UserNovel library) {
        UserNovelStatistics before = UserNovelStatistics.from(library);
        userNovelRepository.delete(library);
        updateNovelStatistics(before, UserNovelStatistics.EMPTY, library.getNovel());
    }

    /**
     * UserNovel 변경 전후의 통계 기여분 차이를 계산해 작품의 역정규화 통계를 원자적으로 갱신한다.
     * 평점 합, 평점 수, 인기도에 변화가 없으면 UPDATE 쿼리를 실행하지 않는다.
     */
    private void updateNovelStatistics(UserNovelStatistics before, UserNovelStatistics after, Novel novel) {
        BigDecimal ratingSumDelta = after.ratingSum().subtract(before.ratingSum());
        long ratingCountDelta = after.ratingCount() - before.ratingCount();
        long popularityDelta = after.popularity() - before.popularity();

        if (ratingSumDelta.signum() == 0
                && ratingCountDelta == 0
                && popularityDelta == 0) {
            return;
        }

        userNovelRepository.flush();
        userNovelRepository.updateNovelStatisticsByDelta(
                novel.getNovelId(),
                ratingSumDelta,
                ratingCountDelta,
                popularityDelta
        );
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> getInterestCountsByNovelIds(List<Long> novelIds) {
        if (novelIds.isEmpty()) {
            return Map.of();
        }

        return userNovelRepository.findInterestCountsByNovelIds(novelIds).stream()
                .collect(Collectors.toMap(
                        NovelInterestCount::novelId,
                        NovelInterestCount::interestCount
                ));
    }

    public int getRatingCount(Novel novel) {
        return userNovelRepository.countByNovelAndUserNovelRatingNot(novel, 0.0f);
    }

    public float getRatingSum(Novel novel) {
        return userNovelRepository.sumUserNovelRatingByNovel(novel);
    }

    public int getInterestCount(Novel novel) {
        return userNovelRepository.countByNovelAndIsInterestTrue(novel);
    }


    public int getWatchingCount(Novel novel) {
        return userNovelRepository.countByNovelAndStatus(novel, WATCHING);
    }

    public int getWatchedCount(Novel novel) {
        return userNovelRepository.countByNovelAndStatus(novel, WATCHED);
    }

    public int getQuitCount(Novel novel) {
        return userNovelRepository.countByNovelAndStatus(novel, QUIT);
    }

    public List<Novel> getTasteNovels(List<Genre> preferGenres) {
        return userNovelRepository.findTasteNovels(preferGenres);
    }

    @Transactional(readOnly = true)
    public List<Novel> getInterestNovels(User user) {
        return userNovelRepository.findByUserAndIsInterestTrue(user).stream()
                .map(UserNovel::getNovel)
                .toList();
    }


    public List<Long> getTodayPopularNovelIds(PageRequest pageRequest) {
        return userNovelRepository.findTodayPopularNovelsId(pageRequest);
    }

}
