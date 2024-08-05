package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomAvatarError.AVATAR_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomGenreError.GENRE_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserError.ALREADY_SET_AVATAR;
import static org.websoso.WSSServer.exception.error.CustomUserError.ALREADY_SET_INTRO;
import static org.websoso.WSSServer.exception.error.CustomUserError.ALREADY_SET_NICKNAME;
import static org.websoso.WSSServer.exception.error.CustomUserError.ALREADY_SET_PROFILE_STATUS;
import static org.websoso.WSSServer.exception.error.CustomUserError.DUPLICATED_NICKNAME;
import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;

import java.util.List;
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
import org.websoso.WSSServer.dto.user.ProfileStatusResponse;
import org.websoso.WSSServer.dto.user.RegisterUserInfoRequest;
import org.websoso.WSSServer.dto.user.UpdateMyProfileRequest;
import org.websoso.WSSServer.exception.error.CustomUserError;
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
        checkIfAlreadySetOrThrow(user.getNickname(), nickname,
                ALREADY_SET_NICKNAME, "nickname with given is already set");
        checkNicknameIfAlreadyExist(nickname);

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
        checkIfAlreadySetOrThrow(user.getIsProfilePublic(), editProfileStatusRequest.isProfilePublic(),
                ALREADY_SET_PROFILE_STATUS, "profile status with given is already set");

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

    public void updateMyProfileInfo(User user, UpdateMyProfileRequest updateMyProfileRequest) {
        checkIfAlreadySetOrThrow(user.getAvatarId(), updateMyProfileRequest.avatarId(),
                ALREADY_SET_AVATAR, "avatarId with given is already set");

        checkIfAlreadySetOrThrow(user.getNickname(), updateMyProfileRequest.nickname(),
                ALREADY_SET_NICKNAME, "nickname with given is already set");
        checkNicknameIfAlreadyExist(updateMyProfileRequest.nickname());

        checkIfAlreadySetOrThrow(user.getIntro(), updateMyProfileRequest.intro(),
                ALREADY_SET_INTRO, "intro with given is already set");

        List<GenrePreference> currentPreferGenres = genrePreferenceRepository.findByUser(user);
        List<GenrePreference> newPreferGenres = createGenrePreferences(user, updateMyProfileRequest.genrePreferences());
        genrePreferenceRepository.deleteAll(currentPreferGenres);
        genrePreferenceRepository.saveAll(newPreferGenres);

        user.updateUserProfile(updateMyProfileRequest);
        userRepository.save(user);
    }

    public void registerUserInfo(User user, RegisterUserInfoRequest registerUserInfoRequest) {
        checkNicknameIfAlreadyExist(registerUserInfoRequest.nickname());
        user.updateUserInfo(registerUserInfoRequest);
        List<GenrePreference> preferGenres = createGenrePreferences(user, registerUserInfoRequest.genrePreferences());
        genrePreferenceRepository.saveAll(preferGenres);
    }

    private void checkNicknameIfAlreadyExist(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new CustomUserException(DUPLICATED_NICKNAME, "nickname is duplicated.");
        }
    }

    private <T> void checkIfAlreadySetOrThrow(T currentValue, T newValue,
                                              CustomUserError customUserError, String message) {
        if (newValue != null && newValue.equals(currentValue)) {
            throw new CustomUserException(customUserError, message);
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
