package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomAttractivePointError.ATTRACTIVE_POINT_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomAttractivePointError.INVALID_ATTRACTIVE_POINT;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.AttractivePoint;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.exception.exception.CustomAttractivePointException;
import org.websoso.WSSServer.repository.AttractivePointRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AttractivePointService {

    private final AttractivePointRepository attractivePointRepository;

    @Transactional(readOnly = true)
    public AttractivePoint getAttractivePointOrException(UserNovel userNovel) {
        return attractivePointRepository.findByUserNovel(userNovel)
                .orElseThrow(() -> new CustomAttractivePointException(ATTRACTIVE_POINT_NOT_FOUND,
                        "attractive point with the given user novel is not found"));
    }

    public static void setAttractivePoint(AttractivePoint attractivePoint, List<String> request) {
        for (String point : request) {
            switch (point.toLowerCase()) {
                case "universe" -> attractivePoint.setUniverse(true);
                case "vibe" -> attractivePoint.setVibe(true);
                case "material" -> attractivePoint.setMaterial(true);
                case "character" -> attractivePoint.setCharacters(true);
                case "relationship" -> attractivePoint.setRelationship(true);
                default -> throw new CustomAttractivePointException(INVALID_ATTRACTIVE_POINT,
                        "invalid attractive point provided in the request");
            }
        }
    }
}
