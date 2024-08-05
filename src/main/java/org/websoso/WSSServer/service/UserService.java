package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomAvatarError.AVATAR_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomGenreError.GENRE_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserError.ALREADY_SET_NICKNAME;
import static org.websoso.WSSServer.exception.error.CustomUserError.ALREADY_SET_PROFILE_STATUS;
import static org.websoso.WSSServer.exception.error.CustomUserError.DUPLICATED_NICKNAME;
import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.config.jwt.JwtProvider;
import org.websoso.WSSServer.config.jwt.UserAuthentication;
import org.websoso.WSSServer.domain.Avatar;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.GenrePreference;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.user.EditProfileStatusRequest;
import org.websoso.WSSServer.dto.user.EmailGetResponse;
import org.websoso.WSSServer.dto.user.LoginResponse;
import org.websoso.WSSServer.dto.user.MyProfileResponse;
import org.websoso.WSSServer.dto.user.NicknameValidation;
import org.websoso.WSSServer.dto.user.ProfileGetResponse;
import org.websoso.WSSServer.dto.user.ProfileStatusResponse;
import org.websoso.WSSServer.dto.user.RegisterUserInfoRequest;
import org.websoso.WSSServer.exception.exception.CustomAvatarException;
import org.websoso.WSSServer.exception.exception.CustomGenreException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.repository.AvatarRepository;
import org.websoso.WSSServer.repository.GenrePreferenceRepository;
import org.websoso.WSSServer.repository.GenreRepository;
import org.websoso.WSSServer.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final AvatarRepository avatarRepository;
    private final GenrePreferenceRepository genrePreferenceRepository;
    private final GenreRepository genreRepository;

    @Transactional(readOnly = true)
    public NicknameValidation isNicknameAvailable(User user, String nickname) {
        if (user.getNickname() != null && user.getNickname().equals(nickname)) {
            throw new CustomUserException(ALREADY_SET_NICKNAME, "nickname with given is already set");
        }
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
            throw new CustomUserException(ALREADY_SET_PROFILE_STATUS, "profile status with given is already set");
        }
        user.updateProfileStatus(editProfileStatusRequest.isProfilePublic());
    }

    @Transactional(readOnly = true)
    public User getUserOrException(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));
    }

    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfileInfo(User user) {
        Byte avatarId = user.getAvatarId();
        Avatar avatar = avatarRepository.findById(avatarId)
                .orElseThrow(
                        () -> new CustomAvatarException(AVATAR_NOT_FOUND, "avatar with the given id was not found"));
        List<GenrePreference> genrePreferences = genrePreferenceRepository.findByUser(user);
        return MyProfileResponse.of(user, avatar, genrePreferences);
    }

    @Transactional(readOnly = true)
    public ProfileGetResponse getProfileInfo(User visitor, Long ownerId) {
        User owner = getUserOrException(ownerId);
        Byte avatarId = owner.getAvatarId();
        Avatar avatar = avatarRepository.findById(avatarId)
                .orElseThrow(
                        () -> new CustomAvatarException(AVATAR_NOT_FOUND, "avatar with the given id was not found"));
        List<GenrePreference> genrePreferences = genrePreferenceRepository.findByUser(owner);

        if (visitor != null && visitor.getUserId().equals(ownerId)) {
            return ProfileGetResponse.of(owner, avatar, genrePreferences, true);
        }
        return ProfileGetResponse.of(owner, avatar, genrePreferences, false);
    }

    public void registerUserInfo(User user, RegisterUserInfoRequest registerUserInfoRequest) {
        validateNickname(registerUserInfoRequest.nickname());
        user.updateUserInfo(registerUserInfoRequest);
        List<GenrePreference> preferGenres = createGenrePreferences(user, registerUserInfoRequest.genrePreferences());
        genrePreferenceRepository.saveAll(preferGenres);
    }

    private void validateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new CustomUserException(DUPLICATED_NICKNAME, "nickname is duplicated.");
        }
    }

    private List<GenrePreference> createGenrePreferences(User user, List<String> genreNames) {
        return genreNames
                .stream()
                .map(this::findByGenreNameOrThrow)
                .map(genre -> GenrePreference.create(user, genre))
                .toList();
    }

    private Genre findByGenreNameOrThrow(String genreName) {
        return genreRepository.findByGenreName(genreName)
                .orElseThrow(() ->
                        new CustomGenreException(GENRE_NOT_FOUND, "genre with the given genreName is not found"));
    }
}
