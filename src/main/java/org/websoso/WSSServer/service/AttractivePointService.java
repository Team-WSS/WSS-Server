package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomAttractivePointError.INVALID_ATTRACTIVE_POINT;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.AttractivePoint;
import org.websoso.WSSServer.exception.exception.CustomAttractivePointException;
import org.websoso.WSSServer.repository.AttractivePointRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AttractivePointService {

    private final AttractivePointRepository attractivePointRepository;

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
}
