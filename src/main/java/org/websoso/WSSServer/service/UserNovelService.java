package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.userNovel.UserNovelErrorCode.USER_NOVEL_ALREADY_EXISTS;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.AttractivePoint;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserNovel;
import org.websoso.WSSServer.domain.common.Flag;
import org.websoso.WSSServer.dto.userNovel.UserNovelCreateRequest;
import org.websoso.WSSServer.exception.userNovel.exception.NovelAlreadyRegisteredException;
import org.websoso.WSSServer.repository.NovelRepository;
import org.websoso.WSSServer.repository.UserNovelRepository;

@Service
@RequiredArgsConstructor
public class UserNovelService {

    private final UserNovelRepository userNovelRepository;
    private final NovelRepository novelRepository;

    @Transactional
    public void createUserNovel(User user, UserNovelCreateRequest request) {

        Novel novel = novelRepository.getById(request.novelId());

        if (getUserNovelOrNull(user, novel) != null) {
            throw new NovelAlreadyRegisteredException(USER_NOVEL_ALREADY_EXISTS, "this novel is already registered");
        }

        UserNovel userNovel = userNovelRepository.save(UserNovel.create(
                request.status(),
                request.userNovelRating(),
                convertToLocalDate(request.startDate()),
                convertToLocalDate(request.endDate()),
                null,
                user,
                novel));

        userNovel.setAttractivePoint(createAndGetAttractivePoint(request.attractivePoints(), userNovel));

        if (request.userNovelRating() != 0.0f) {
            novel.increaseNovelRatingCount();
            novel.increaseNovelRatingSum(request.userNovelRating());
        }
        //UserStatistics watchingNovelCount, faNovelCount, faNovelRatingSum
        //NovelStatistics watchingCount, vibeCount
    }

    private static AttractivePoint createAndGetAttractivePoint(List<String> request, UserNovel userNovel) {
        AttractivePoint attractivePoint = AttractivePoint.create(userNovel);

        for (String point : request) {
            switch (point.toLowerCase()) {
                case "universe" -> attractivePoint.setUniverse(Flag.Y);
                case "vibe" -> attractivePoint.setVibe(Flag.Y);
                case "material" -> attractivePoint.setMaterial(Flag.Y);
                case "characters" -> attractivePoint.setCharacters(Flag.Y);
                case "relationship" -> attractivePoint.setRelationship(Flag.Y);
                default -> throw new IllegalArgumentException("Invalid attractive point: " + point);
            }
        }
        return attractivePoint;
    }

    private LocalDate convertToLocalDate(String string) {
        return LocalDate.parse(string, DateTimeFormatter.ISO_DATE);
    }

    protected UserNovel getUserNovelOrNull(User user, Novel novel) {
        if (user == null) {
            return null;
        }
        return userNovelRepository.findByNovelAndUser(novel, user).orElse(null);
    }

}
