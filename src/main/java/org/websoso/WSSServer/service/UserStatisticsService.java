package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomUserStatisticsError.USER_STATISTICS_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserStatistics;
import org.websoso.WSSServer.exception.exception.CustomUserStatisticsException;
import org.websoso.WSSServer.repository.UserStatisticsRepository;

@Service
@RequiredArgsConstructor
public class UserStatisticsService {

    private final UserStatisticsRepository userStatisticsRepository;

    public UserStatistics getUserStatisticsOrException(User user) {
        return userStatisticsRepository.findByUser(user).orElseThrow(
                () -> new CustomUserStatisticsException(USER_STATISTICS_NOT_FOUND,
                        "user statistics with the given user is not found"));
    }

}
