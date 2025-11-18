package org.websoso.WSSServer.library.service;

import static org.websoso.WSSServer.exception.error.CustomAttractivePointError.INVALID_ATTRACTIVE_POINT;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.library.domain.AttractivePoint;
import org.websoso.WSSServer.exception.exception.CustomAttractivePointException;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.domain.UserNovelAttractivePoint;
import org.websoso.WSSServer.library.repository.AttractivePointRepository;
import org.websoso.WSSServer.library.repository.UserNovelAttractivePointRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AttractivePointService {

    private final AttractivePointRepository attractivePointRepository;
    private final UserNovelAttractivePointRepository userNovelAttractivePointRepository;

    @Transactional(readOnly = true)
    public AttractivePoint getAttractivePointOrException(String attractivePoint) {
        return attractivePointRepository.findByAttractivePointName(attractivePoint)
                .orElseThrow(() -> new CustomAttractivePointException(INVALID_ATTRACTIVE_POINT,
                        "invalid attractive point provided in the request"));
    }

    public AttractivePoint getAttractivePointByString(String request) {
        String lowerCaseRequest = request.toLowerCase();
        return getAttractivePointOrException(lowerCaseRequest);
    }

    public void createUserNovelAttractivePoints(UserNovel userNovel, List<String> request) {
        for (String stringAttractivePoint : request) {
            AttractivePoint attractivePoint = getAttractivePointByString(stringAttractivePoint);
            userNovelAttractivePointRepository.save(UserNovelAttractivePoint.create(userNovel, attractivePoint));
        }
    }

}
