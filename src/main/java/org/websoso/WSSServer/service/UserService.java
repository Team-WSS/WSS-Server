package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomUserError.DUPLICATED_NICKNAME;
import static org.websoso.WSSServer.exception.error.CustomUserError.INVALID_PROFILE_STATUS;
import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.config.jwt.JwtProvider;
import org.websoso.WSSServer.config.jwt.UserAuthentication;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.user.EditProfileStatusRequest;
import org.websoso.WSSServer.dto.user.EmailGetResponse;
import org.websoso.WSSServer.dto.user.LoginResponse;
import org.websoso.WSSServer.dto.user.NicknameValidation;
import org.websoso.WSSServer.dto.user.ProfileStatusResponse;
import org.websoso.WSSServer.exception.exception.CustomUserException;
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
            throw new CustomUserException(DUPLICATED_NICKNAME, "nickname is duplicated.");
        }
        return NicknameValidation.of(true);
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public ProfileStatusResponse getProfileStatus(User user) {
        return ProfileStatusResponse.of(user.getIsProfilePublic());
    }

    public void editProfileStatus(User user, EditProfileStatusRequest editProfileStatusRequest) {
        if (user.getIsProfilePublic().equals(editProfileStatusRequest.isProfilePublic())) {
            throw new CustomUserException(INVALID_PROFILE_STATUS, "profile status with given is already set");
        }
        user.updateProfileStatus(editProfileStatusRequest.isProfilePublic());
    }

    @Transactional(readOnly = true)
    public User getUserOrException(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));
    }

}
