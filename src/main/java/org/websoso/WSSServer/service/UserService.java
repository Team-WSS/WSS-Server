package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.user.UserErrorCode.USER_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.config.jwt.JwtProvider;
import org.websoso.WSSServer.config.jwt.UserAuthentication;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.User.NicknameValidation;
import org.websoso.WSSServer.exception.user.exception.InvalidUserException;
import org.websoso.WSSServer.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Transactional(readOnly = true)
    public NicknameValidation isNicknameAvailable(String nickname) {
        boolean isNicknameTaken = userRepository.existsByNickname(nickname);
        return NicknameValidation.of(isNicknameTaken);
    }

    public String login(Long userId) {
        User user = getUserOrException(userId);

        UserAuthentication userAuthentication = new UserAuthentication(user.getUserId(), null, null);

        return jwtProvider.generateToken(userAuthentication);
    }

    public User getUserOrException(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new InvalidUserException(USER_NOT_FOUND, "user with the given id was not found"));
    }

}