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

    public void saveAll(List<PopularNovel> popularNovels) {
        popularNovelRepository.saveAll(popularNovels);
    }

    public List<PopularNovel> getPopularNovels() {
        return popularNovelRepository.findAll();
    }

    public List<Long> getNovelIdsFromPopularNovel() {
        return new ArrayList<>(popularNovelRepository.findAll()
                .stream()
                .map(PopularNovel::getNovelId)
                .toList());
    }

    public void deleteAll(List<PopularNovel> popularNovels) {
        popularNovelRepository.deleteAll(popularNovels);
    }


}
