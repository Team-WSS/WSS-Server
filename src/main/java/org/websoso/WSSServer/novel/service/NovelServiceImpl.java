package org.websoso.WSSServer.novel.service;

import static org.websoso.WSSServer.exception.error.CustomNovelError.NOVEL_NOT_FOUND;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.novel.domain.PopularNovel;
import org.websoso.WSSServer.dto.platform.PlatformGetResponse;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.library.domain.Keyword;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.domain.NovelGenre;
import org.websoso.WSSServer.novel.repository.NovelGenreRepository;
import org.websoso.WSSServer.novel.repository.NovelPlatformRepository;
import org.websoso.WSSServer.novel.repository.NovelRepository;
import org.websoso.WSSServer.novel.repository.PopularNovelRepository;

@Service
@RequiredArgsConstructor
public class NovelServiceImpl {

    private final NovelRepository novelRepository;
    private final NovelGenreRepository novelGenreRepository;
    private final NovelPlatformRepository novelPlatformRepository;

    @Transactional(readOnly = true)
    public Novel getNovelOrException(Long novelId) {
        return novelRepository.findById(novelId)
                .orElseThrow(() -> new CustomNovelException(NOVEL_NOT_FOUND,
                        "novel with the given id is not found"));
    }

    public Page<Novel> searchNovels(PageRequest pageRequest, String searchQuery) {
        return novelRepository.findSearchedNovels(pageRequest, searchQuery);
    }

    public Page<Novel> findFilteredNovels(PageRequest pageRequest, List<Genre> genres, List<Keyword> keywords,
                                          Boolean isCompleted, Float novelRating) {
        return novelRepository.findFilteredNovels(pageRequest, genres, isCompleted, novelRating,
                keywords);
    }

    public List<NovelGenre> getGenresByNovel(Novel novel) {
        return novelGenreRepository.findAllByNovel(novel);
    }

    public List<PlatformGetResponse> getPlatforms(Novel novel) {
        return novelPlatformRepository.findAllByNovel(novel).stream()
                .map(PlatformGetResponse::of)
                .toList();
    }

    public List<Novel> getSelectedPopularNovels(List<Long> selectedPopularNovelIds) {
        return novelRepository.findAllById(selectedPopularNovelIds);
    }

}
