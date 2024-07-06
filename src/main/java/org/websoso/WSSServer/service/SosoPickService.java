package org.websoso.WSSServer.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.dto.sosoPick.SosoPickGetResponse;
import org.websoso.WSSServer.dto.sosoPick.SosoPickNovelGetResponse;
import org.websoso.WSSServer.repository.NovelRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class SosoPickService {

    private final NovelRepository novelRepository;

    @Transactional(readOnly = true)
    public SosoPickGetResponse getSosoPick() {
        Pageable pageable = PageRequest.of(0, 10);
        List<SosoPickNovelGetResponse> sosoPickNovels = novelRepository.findSosoPick(pageable).stream()
                .map(SosoPickNovelGetResponse::of).collect(Collectors.toList());
        return SosoPickGetResponse.of(sosoPickNovels);
    }

}
