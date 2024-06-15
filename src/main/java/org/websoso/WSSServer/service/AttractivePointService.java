package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.attractivePoint.AttractivePointErrorCode.INVALID_ATTRACTIVE_POINT;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.domain.AttractivePoint;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.domain.common.Flag;
import org.websoso.WSSServer.exception.attractivePoint.exception.InvalidAttractivePointException;

@Service
@RequiredArgsConstructor
public class AttractivePointService {

    protected static AttractivePoint createAndGetAttractivePoint(List<String> request, UserNovel userNovel) {
        AttractivePoint attractivePoint = AttractivePoint.create(userNovel);

        for (String point : request) {
            switch (point.toLowerCase()) {
                case "universe" -> attractivePoint.setUniverse(Flag.Y);
                case "vibe" -> attractivePoint.setVibe(Flag.Y);
                case "material" -> attractivePoint.setMaterial(Flag.Y);
                case "character" -> attractivePoint.setCharacters(Flag.Y);
                case "relationship" -> attractivePoint.setRelationship(Flag.Y);
                default -> throw new InvalidAttractivePointException(INVALID_ATTRACTIVE_POINT,
                        "invalid attractive point provided in the request");
            }
        }
        return attractivePoint;
    }
}
