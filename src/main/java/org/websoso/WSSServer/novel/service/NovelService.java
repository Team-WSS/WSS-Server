package org.websoso.WSSServer.novel.service;


import static org.websoso.WSSServer.exception.error.CustomNovelError.NOVEL_NOT_FOUND;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.novel.repository.NovelRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class NovelService {

    private final NovelRepository novelRepository;

    @Transactional(readOnly = true)
    public Novel getNovelOrException(Long novelId) {
        return novelRepository.findById(novelId)
                .orElseThrow(() -> new CustomNovelException(NOVEL_NOT_FOUND,
                        "novel with the given id is not found"));
    }

}
