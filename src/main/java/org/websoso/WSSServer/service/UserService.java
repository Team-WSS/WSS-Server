package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.user.UserErrorCode.DUPLICATED_NICKNAME;
import static org.websoso.WSSServer.exception.user.UserErrorCode.USER_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.config.jwt.JwtProvider;
import org.websoso.WSSServer.config.jwt.UserAuthentication;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.User.LoginResponse;
import org.websoso.WSSServer.dto.User.NicknameValidation;
import org.websoso.WSSServer.dto.User.EmailGetResponse;
import org.websoso.WSSServer.exception.user.exception.DuplicatedNicknameException;
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
        if (userRepository.existsByNickname(nickname)) {
            throw new DuplicatedNicknameException(DUPLICATED_NICKNAME, "nickname is duplicated.");
        }
        return NicknameValidation.of(true);
    }

    public LoginResponse login(Long userId) {
        User user = getUserOrException(userId);

        UserAuthentication userAuthentication = new UserAuthentication(user.getUserId(), null, null);
        String token = jwtProvider.generateToken(userAuthentication);

        return LoginResponse.of(token);
    }

    @Transactional(readOnly = true)
    public EmailGetResponse getEmail(User user) {
        return EmailGetResponse.of(user.getEmail());
    }

    public User getUserOrException(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new InvalidUserException(USER_NOT_FOUND, "user with the given id was not found"));
    }

}
