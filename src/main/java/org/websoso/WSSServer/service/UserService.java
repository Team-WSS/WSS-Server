package org.websoso.WSSServer.service;

import static java.lang.Boolean.FALSE;
import static org.websoso.WSSServer.domain.common.DiscordWebhookMessageType.JOIN;
import static org.websoso.WSSServer.domain.common.DiscordWebhookMessageType.WITHDRAW;
import static org.websoso.WSSServer.exception.error.CustomAvatarError.AVATAR_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomGenreError.GENRE_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserError.ALREADY_SET_AVATAR;
import static org.websoso.WSSServer.exception.error.CustomUserError.ALREADY_SET_INTRO;
import static org.websoso.WSSServer.exception.error.CustomUserError.ALREADY_SET_NICKNAME;
import static org.websoso.WSSServer.exception.error.CustomUserError.ALREADY_SET_PROFILE_STATUS;
import static org.websoso.WSSServer.exception.error.CustomUserError.DUPLICATED_NICKNAME;
import static org.websoso.WSSServer.exception.error.CustomUserError.INACCESSIBLE_USER_PROFILE;
import static org.websoso.WSSServer.exception.error.CustomUserError.TERMS_AGREEMENT_REQUIRED;
import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.config.jwt.CustomAuthenticationToken;
import org.websoso.WSSServer.config.jwt.JwtProvider;
import org.websoso.WSSServer.domain.Avatar;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.GenrePreference;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.UserDevice;
import org.websoso.WSSServer.domain.WithdrawalReason;
import org.websoso.WSSServer.domain.common.DiscordWebhookMessage;
import org.websoso.WSSServer.domain.common.SocialLoginType;
import org.websoso.WSSServer.dto.auth.LogoutRequest;
import org.websoso.WSSServer.dto.notification.PushSettingGetResponse;
import org.websoso.WSSServer.dto.user.EditMyInfoRequest;
import org.websoso.WSSServer.dto.user.EditProfileStatusRequest;
import org.websoso.WSSServer.dto.user.FCMTokenRequest;
import org.websoso.WSSServer.dto.user.LoginResponse;
import org.websoso.WSSServer.dto.user.MyProfileResponse;
import org.websoso.WSSServer.dto.user.NicknameValidation;
import org.websoso.WSSServer.dto.user.ProfileGetResponse;
import org.websoso.WSSServer.dto.user.ProfileStatusResponse;
import org.websoso.WSSServer.dto.user.RegisterUserInfoRequest;
import org.websoso.WSSServer.dto.user.TermsSettingGetResponse;
import org.websoso.WSSServer.dto.user.UpdateMyProfileRequest;
import org.websoso.WSSServer.dto.user.UserIdAndNicknameResponse;
import org.websoso.WSSServer.dto.user.UserInfoGetResponse;
import org.websoso.WSSServer.dto.user.WithdrawalRequest;
import org.websoso.WSSServer.exception.error.CustomUserError;
import org.websoso.WSSServer.exception.exception.CustomAvatarException;
import org.websoso.WSSServer.exception.exception.CustomGenreException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.feed.repository.CommentRepository;
import org.websoso.WSSServer.feed.repository.FeedRepository;
import org.websoso.WSSServer.oauth2.service.AppleService;
import org.websoso.WSSServer.oauth2.service.KakaoService;
import org.websoso.WSSServer.repository.AvatarRepository;
import org.websoso.WSSServer.repository.GenrePreferenceRepository;
import org.websoso.WSSServer.repository.GenreRepository;
import org.websoso.WSSServer.repository.RefreshTokenRepository;
import org.websoso.WSSServer.repository.UserDeviceRepository;
import org.websoso.WSSServer.repository.UserRepository;
import org.websoso.WSSServer.repository.WithdrawalReasonRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final AvatarRepository avatarRepository;
    private final GenrePreferenceRepository genrePreferenceRepository;
    private final GenreRepository genreRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final KakaoService kakaoService;
    private final AppleService appleService;
    private final FeedRepository feedRepository;
    private final CommentRepository commentRepository;
    private final DiscordMessageClient discordMessageClient;
    private final WithdrawalReasonRepository withdrawalReasonRepository;
    private final UserDeviceRepository userDeviceRepository;
    private static final String KAKAO_PREFIX = "kakao";
    private static final String APPLE_PREFIX = "apple";

    @Transactional(readOnly = true)
    public NicknameValidation isNicknameAvailable(User user, String nickname) {
        checkIfAlreadySetOrThrow(user.getNickname(), nickname,
                ALREADY_SET_NICKNAME, "nickname with given is already set");
        if (userRepository.existsByNickname(nickname)) {
            return NicknameValidation.of(false);
        }

        return NicknameValidation.of(true);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(Long userId) {
        User user = getUserOrException(userId);

        CustomAuthenticationToken customAuthenticationToken = new CustomAuthenticationToken(user.getUserId(), null,
                null);
        String token = jwtProvider.generateAccessToken(customAuthenticationToken);

        return LoginResponse.of(token);
    }

    @Transactional(readOnly = true)
    public UserInfoGetResponse getUserInfo(User user) {
        return UserInfoGetResponse.of(user);
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
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));
    }

    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfileInfo(User user) {
        Byte avatarId = user.getAvatarId();
        Avatar avatar = findAvatarByIdOrThrow(avatarId);
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

        genrePreferenceRepository.deleteAllByUser(user);

        List<GenrePreference> newPreferGenres = createGenrePreferences(user, updateMyProfileRequest.genrePreferences());
        genrePreferenceRepository.saveAll(newPreferGenres);

        user.updateUserProfile(updateMyProfileRequest);
    }

    @Transactional(readOnly = true)
    public ProfileGetResponse getProfileInfo(User visitor, Long ownerId) {
        if (ownerId == -1L) {
            throw new CustomUserException(INACCESSIBLE_USER_PROFILE,
                    "The profile for this user is inaccessible: unknown");
        }
        User owner = getUserOrException(ownerId);
        Byte avatarId = owner.getAvatarId();
        Avatar avatar = findAvatarByIdOrThrow(avatarId);
        List<GenrePreference> genrePreferences = genrePreferenceRepository.findByUser(owner);

        boolean isOwner = visitor != null && visitor.getUserId().equals(ownerId);
        return ProfileGetResponse.of(isOwner, owner, avatar, genrePreferences);
    }

    private Avatar findAvatarByIdOrThrow(Byte avatarId) {
        return avatarRepository.findById(avatarId)
                .orElseThrow(
                        () -> new CustomAvatarException(AVATAR_NOT_FOUND, "avatar with the given id was not found"));
    }

    public void registerUserInfo(User user, RegisterUserInfoRequest registerUserInfoRequest) {
        checkNicknameIfAlreadyExist(registerUserInfoRequest.nickname());
        user.updateUserInfo(registerUserInfoRequest);
        List<GenrePreference> preferGenres = createGenrePreferences(user, registerUserInfoRequest.genrePreferences());
        genrePreferenceRepository.saveAll(preferGenres);

        discordMessageClient.sendDiscordWebhookMessage(DiscordWebhookMessage.of(
                MessageFormatter.formatUserJoinMessage(user, SocialLoginType.fromSocialId(user.getSocialId())), JOIN));
    }

    public void logout(User user, LogoutRequest request) {
        refreshTokenRepository.findByRefreshToken(request.refreshToken())
                .ifPresent(refreshTokenRepository::delete);

        userDeviceRepository.deleteByUserAndDeviceIdentifier(user, request.deviceIdentifier());

        if (user.getSocialId().startsWith(KAKAO_PREFIX)) {
            kakaoService.kakaoLogout(user);
        }
    }

    public void withdrawUser(User user, WithdrawalRequest withdrawalRequest) {
        unlinkSocialAccount(user);

        String messageContent = MessageFormatter.formatUserWithdrawMessage(user.getUserId(), user.getNickname(),
                withdrawalRequest.reason());

        cleanupUserData(user.getUserId());

        discordMessageClient.sendDiscordWebhookMessage(
                DiscordWebhookMessage.of(messageContent, WITHDRAW));

        withdrawalReasonRepository.save(WithdrawalReason.create(withdrawalRequest.reason()));
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

    private void unlinkSocialAccount(User user) {
        if (user.getSocialId().startsWith(KAKAO_PREFIX)) {
            kakaoService.unlinkFromKakao(user);
        } else if (user.getSocialId().startsWith(APPLE_PREFIX)) {
            appleService.unlinkFromApple(user);
        }
    }

    private void cleanupUserData(Long userId) {
        refreshTokenRepository.deleteAll(refreshTokenRepository.findAllByUserId(userId));
        feedRepository.updateUserToUnknown(userId);
        commentRepository.updateUserToUnknown(userId);
        userRepository.deleteById(userId);
    }

    public void editMyInfo(User user, EditMyInfoRequest editMyInfoRequest) {
        user.editMyInfo(editMyInfoRequest);
    }

    @Transactional(readOnly = true)
    public UserIdAndNicknameResponse getUserIdAndNicknameAndGender(User user) {
        return UserIdAndNicknameResponse.of(user);
    }

    public boolean registerFCMToken(User user, FCMTokenRequest fcmTokenRequest) {
        return userDeviceRepository.findByDeviceIdentifierAndUser(fcmTokenRequest.deviceIdentifier(), user)
                .map(userDevice -> {
                    userDevice.updateFcmToken(fcmTokenRequest.fcmToken());
                    return false;
                })
                .orElseGet(() -> {
                    UserDevice userDevice = UserDevice.create(
                            fcmTokenRequest.fcmToken(),
                            fcmTokenRequest.deviceIdentifier(),
                            user
                    );
                    userDeviceRepository.save(userDevice);
                    return true;
                });
    }

    public void registerPushSetting(User user, Boolean isPushEnabled) {
        user.updatePushSetting(isPushEnabled);
    }

    @Transactional(readOnly = true)
    public PushSettingGetResponse getPushSettingValue(User user) {
        return PushSettingGetResponse.of(user.getIsPushEnabled());
    }

    @Transactional(readOnly = true)
    public TermsSettingGetResponse getTermsSettingValue(User user) {
        return TermsSettingGetResponse.of(user.getServiceAgreed(), user.getPrivacyAgreed(),
                user.getMarketingAgreed());
    }

    public void updateTermsSetting(User user, Boolean serviceAgreed, Boolean privacyAgreed,
                                   Boolean marketingAgreed) {
        if (FALSE.equals(serviceAgreed) || FALSE.equals(privacyAgreed)) {
            throw new CustomUserException(TERMS_AGREEMENT_REQUIRED,
                    "service terms and personal information consent are mandatory");
        }
        user.updateTermsSetting(serviceAgreed, privacyAgreed, marketingAgreed);
    }
}
