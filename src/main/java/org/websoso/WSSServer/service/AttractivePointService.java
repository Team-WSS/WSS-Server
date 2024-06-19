package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.domain.common.Flag.Y;
import static org.websoso.WSSServer.exception.attractivePoint.AttractivePointErrorCode.INVALID_ATTRACTIVE_POINT;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.AttractivePoint;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.exception.attractivePoint.exception.InvalidAttractivePointException;

@Service
@RequiredArgsConstructor
@Transactional
public class AttractivePointService {

    protected static AttractivePoint createAndGetAttractivePoint(List<String> request, UserNovel userNovel) {
        AttractivePoint attractivePoint = AttractivePoint.create(userNovel);

        for (String point : request) {
            switch (point.toLowerCase()) {
                case "universe" -> attractivePoint.setUniverse(Y);
                case "vibe" -> attractivePoint.setVibe(Y);
                case "material" -> attractivePoint.setMaterial(Y);
                case "character" -> attractivePoint.setCharacters(Y);
                case "relationship" -> attractivePoint.setRelationship(Y);
                default -> throw new InvalidAttractivePointException(INVALID_ATTRACTIVE_POINT,
                        "invalid attractive point provided in the request");
            }
        }
        return attractivePoint;
    }
}
