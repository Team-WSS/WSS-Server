package org.websoso.WSSServer.auth;

import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserRepository userRepository;

    public boolean validate(Long resourceId, Long userId, Class<?> resourceType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));
    }
}
