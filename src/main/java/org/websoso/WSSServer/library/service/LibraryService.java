package org.websoso.WSSServer.library.service;

import static org.websoso.WSSServer.domain.common.ReadStatus.QUIT;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHED;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHING;
import static org.websoso.WSSServer.exception.error.CustomUserNovelError.USER_NOVEL_NOT_FOUND;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.domain.common.ReadStatus;
import org.websoso.WSSServer.exception.exception.CustomUserNovelException;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.repository.UserNovelRepository;
import org.websoso.WSSServer.novel.domain.Novel;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final UserNovelRepository userNovelRepository;

    // TODO: novelId로 불러옴
    @Transactional(readOnly = true)
    public UserNovel getLibraryOrException(User user, Long novelId) {
        return userNovelRepository.findByNovel_NovelIdAndUser(novelId, user)
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

    @Transactional
    public UserNovel createLibrary(ReadStatus status, Float userNovelRating, LocalDate startDate, LocalDate endDate,
                                   User user, Novel novel) {
        return userNovelRepository.save(UserNovel.create(
                status,
                userNovelRating,
                startDate,
                endDate,
                user,
                novel));
    }

    @Transactional
    public void registerInterest(User user, Novel novel) {
        userNovelRepository.upsertInterest(
                user.getUserId(),
                novel.getNovelId(),
                UserNovel.DEFAULT_RATING,
                UserNovel.DEFAULT_STATUS
        );
    }

    @Transactional
    public void delete(UserNovel library) {
        userNovelRepository.delete(library);
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


    public List<Long> getTodayPopularNovelIds(PageRequest pageRequest) {
        return userNovelRepository.findTodayPopularNovelsId(pageRequest);
    }
}
