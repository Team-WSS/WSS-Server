package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.attractivePoint.AttractivePointErrorCode.INVALID_ATTRACTIVE_POINT;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.AttractivePoint;
import org.websoso.WSSServer.exception.attractivePoint.exception.CustomAttractivePointException;

@Service
@RequiredArgsConstructor
@Transactional
public class AttractivePointService {

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
