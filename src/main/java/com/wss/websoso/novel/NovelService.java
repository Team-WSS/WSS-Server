package com.wss.websoso.novel;

import com.wss.websoso.novel.dto.NovelDetailGetResponse;
import com.wss.websoso.novel.dto.NovelGetResponse;
import com.wss.websoso.novel.dto.NovelsGetResponse;
import com.wss.websoso.platform.dto.PlatformGetResponse;
import com.wss.websoso.user.User;
import com.wss.websoso.user.UserRepository;
import com.wss.websoso.userNovel.UserNovel;
import com.wss.websoso.userNovel.UserNovelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NovelService {

    private static final int DEFAULT_PAGE_NUMBER = 0;
    private final NovelRepository novelRepository;
    private final UserRepository userRepository;
    private final UserNovelRepository userNovelRepository;

    public NovelsGetResponse getNovelsByWord(Long lastNovelId, int size, String word) {
        PageRequest pageRequest = PageRequest.of(DEFAULT_PAGE_NUMBER, size);
        Slice<Novel> entitySlice = novelRepository.findByIdLessThanOrderByIdDesc(lastNovelId, pageRequest,
                word.replaceAll(" ", ""));
        return new NovelsGetResponse(entitySlice.getContent().stream()
                .map(novel -> new NovelGetResponse(
                        novel.getNovelId(),
                        novel.getNovelTitle(),
                        novel.getNovelAuthor(),
                        novel.getNovelGenre(),
                        novel.getNovelImg()
                ))
                .toList());
    }

    public NovelDetailGetResponse getNovelByNovelId(Long novelId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 사용자가 없습니다."));

        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 작품이 없습니다."));

        try {
            UserNovel userNovel = userNovelRepository.findByUserAndNovelId(user, novelId)
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 등록된 작품이 없습니다."));

            NovelDetailGetResponse novelDetailGetResponse = new NovelDetailGetResponse(
                    null,
                    userNovel.getUserNovelId(),
                    null,
                    userNovel.getUserNovelTitle(),
                    null,
                    userNovel.getUserNovelAuthor(),
                    null,
                    userNovel.getUserNovelGenre(),
                    null,
                    userNovel.getUserNovelImg(),
                    null,
                    userNovel.getUserNovelDescription(),
                    userNovel.getUserNovelRating(),
                    userNovel.getUserNovelReadStatus(),
                    userNovel.getPlatforms().stream()
                            .map(platform -> new PlatformGetResponse(
                                    platform.getPlatformName(),
                                    platform.getPlatformUrl()
                            ))
                            .toList()
            );
            novelDetailGetResponse.setUserNovelReadEndDate(userNovel.getUserNovelReadStartDate(), userNovel.getUserNovelReadEndDate());
            return novelDetailGetResponse;
        } catch (IllegalArgumentException e) {
            return new NovelDetailGetResponse(
                    novel.getNovelId(),
                    null,
                    novel.getNovelTitle(),
                    null,
                    novel.getNovelAuthor(),
                    null,
                    novel.getNovelGenre(),
                    null,
                    novel.getNovelImg(),
                    null,
                    novel.getNovelDescription(),
                    null,
                    null,
                    null,
                    novel.getPlatforms().stream()
                            .map(platform -> new PlatformGetResponse(
                                    platform.getPlatformName(),
                                    platform.getPlatformUrl()
                            ))
                            .toList()
            );
        }
    }
}