package org.websoso.WSSServer.novel.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.novel.domain.PopularNovel;
import org.websoso.WSSServer.novel.repository.PopularNovelRepository;

@Service
@RequiredArgsConstructor
public class PopularNovelService {

    private final PopularNovelRepository popularNovelRepository;

    public List<Long> getNovelIdsFromPopularNovel() {
        return new ArrayList<>(popularNovelRepository.findAll()
                .stream()
                .map(PopularNovel::getNovelId)
                .toList());
    }

}
