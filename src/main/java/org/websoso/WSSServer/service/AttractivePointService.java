package org.websoso.WSSServer.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.domain.AttractivePoint;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.domain.common.Flag;

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
                default -> throw new IllegalArgumentException("Invalid attractive point: " + point);
            }
        }
        return attractivePoint;
    }
}
