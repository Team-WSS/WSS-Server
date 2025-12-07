package org.websoso.WSSServer.library.service;

import static org.websoso.WSSServer.exception.error.CustomAttractivePointError.INVALID_ATTRACTIVE_POINT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.common.AttractivePointName;
import org.websoso.WSSServer.library.domain.AttractivePoint;
import org.websoso.WSSServer.exception.exception.CustomAttractivePointException;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.domain.UserNovelAttractivePoint;
import org.websoso.WSSServer.library.repository.AttractivePointRepository;
import org.websoso.WSSServer.library.repository.UserNovelAttractivePointRepository;
import org.websoso.WSSServer.novel.domain.Novel;

@Service
@RequiredArgsConstructor
@Transactional
public class AttractivePointService {

    private static final int ATTRACTIVE_POINT_SIZE = 3;

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

    public void createUserNovelAttractivePoint(UserNovel userNovel, AttractivePoint attractivePoint) {
        userNovelAttractivePointRepository.save(UserNovelAttractivePoint.create(userNovel, attractivePoint));
    }

    public void createUserNovelAttractivePoints(UserNovel userNovel, List<String> request) {
        List<UserNovelAttractivePoint> attractivePoints = request.stream()
                .map(this::getAttractivePointByString)
                .map(attractivePoint -> UserNovelAttractivePoint.create(userNovel, attractivePoint))
                .toList();

        userNovelAttractivePointRepository.saveAll(attractivePoints);
    }

    public void deleteUserNovelAttractivePoints(List<UserNovelAttractivePoint> userNovelAttractivePoints) {
        userNovelAttractivePointRepository.deleteAll(userNovelAttractivePoints);
    }

    public List<String> getAttractivePoints(Novel novel) {
        Map<String, Integer> attractivePointMap = makeAttractivePointMapExcludingZero(novel);

        if (attractivePointMap.isEmpty()) {
            return Collections.emptyList();
        }

        return getTOP3AttractivePoints(attractivePointMap);
    }

    private Map<String, Integer> makeAttractivePointMapExcludingZero(Novel novel) {
        Map<String, Integer> attractivePointMap = new HashMap<>();

        for (AttractivePointName point : AttractivePointName.values()) {
            attractivePointMap.put(point.getLabel(), getAttractivePointCount(novel, point));
        }

        attractivePointMap.entrySet().removeIf(entry -> entry.getValue() == 0);

        return attractivePointMap;
    }

    private List<String> getTOP3AttractivePoints(Map<String, Integer> attractivePointMap) {
        Map<Integer, List<String>> groupedByValue = groupAttractivePointByValue(attractivePointMap);

        List<String> result = new ArrayList<>();
        List<Integer> sortedKeys = new ArrayList<>(groupedByValue.keySet());
        sortedKeys.sort(Collections.reverseOrder());

        Random random = new Random();

        for (Integer key : sortedKeys) {
            List<String> items = groupedByValue.get(key);
            if (result.size() + items.size() > ATTRACTIVE_POINT_SIZE) {
                Collections.shuffle(items, random);
                items = items.subList(0, ATTRACTIVE_POINT_SIZE - result.size());
            }
            result.addAll(items);
            if (result.size() >= ATTRACTIVE_POINT_SIZE) {
                break;
            }
        }

        return result;
    }

    private Map<Integer, List<String>> groupAttractivePointByValue(Map<String, Integer> attractivePointMap) {
        Map<Integer, List<String>> groupedByValue = new HashMap<>();

        for (Map.Entry<String, Integer> entry : attractivePointMap.entrySet()) {
            groupedByValue
                    .computeIfAbsent(entry.getValue(), k -> new ArrayList<>())
                    .add(entry.getKey());
        }

        return groupedByValue;
    }

    public int getAttractivePointCount(Novel novel, AttractivePointName point) {
        return userNovelAttractivePointRepository.countByUserNovel_NovelAndAttractivePoint_AttractivePointName(
                novel, point.getLabel());
    }

}
