package org.websoso.WSSServer.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.dto.sosoPick.SosoPickGetResponse;
import org.websoso.WSSServer.dto.sosoPick.SosoPickNovelGetResponse;
import org.websoso.WSSServer.repository.UserNovelRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class SosoPickService {

    private final UserNovelRepository userNovelRepository;

    @Transactional(readOnly = true)
    public SosoPickGetResponse getSosoPick() {
        int pageSize = 20;
        Pageable pageable = PageRequest.of(0, pageSize);
        Set<Novel> novels = new LinkedHashSet<>();

        while (novels.size() < 10) {
            List<UserNovel> userNovels = userNovelRepository.findByOrderByCreatedDateDesc(pageable).getContent();
            for (UserNovel userNovel : userNovels) {
                if (novels.size() >= 10) {
                    break;
                }
                novels.add(userNovel.getNovel());
            }
            if (userNovels.size() < pageSize) {
                break;
            }
            pageable = PageRequest.of(pageable.getPageNumber() + 1, pageSize);
        }

        List<SosoPickNovelGetResponse> sosoPickNovels = novels.stream().map(SosoPickNovelGetResponse::of).toList();
        return SosoPickGetResponse.of(sosoPickNovels);
    }

}
