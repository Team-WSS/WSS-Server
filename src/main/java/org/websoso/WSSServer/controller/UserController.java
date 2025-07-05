package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.SortCriteria;
import org.websoso.WSSServer.dto.feed.UserFeedsGetResponse;
import org.websoso.WSSServer.dto.notification.PushSettingGetResponse;
import org.websoso.WSSServer.dto.notification.PushSettingRequest;
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
import org.websoso.WSSServer.dto.user.TermsSettingRequest;
import org.websoso.WSSServer.dto.user.UpdateMyProfileRequest;
import org.websoso.WSSServer.dto.user.UserIdAndNicknameResponse;
import org.websoso.WSSServer.dto.user.UserInfoGetResponse;
import org.websoso.WSSServer.dto.user.UserNovelCountGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserGenrePreferencesGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserNovelAndNovelsGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserTasteAttractivePointPreferencesAndKeywordsGetResponse;
import org.websoso.WSSServer.service.FeedService;
import org.websoso.WSSServer.service.UserNovelService;
import org.websoso.WSSServer.service.UserService;
import org.websoso.WSSServer.validation.NicknameConstraint;

@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserNovelService userNovelService;
    private final FeedService feedService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody String userId) {
        LoginResponse response = userService.login(Long.valueOf(userId));
        return ResponseEntity
                .status(OK)
                .body(response);
    }

    @PostMapping("/fcm-token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> registerFCMToken(@AuthenticationPrincipal User user,
                                                 @Valid @RequestBody FCMTokenRequest fcmTokenRequest) {
        return userService.registerFCMToken(user, fcmTokenRequest)
                ? ResponseEntity.status(CREATED).build()
                : ResponseEntity.status(NO_CONTENT).build();
    }

    @GetMapping("/nickname/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NicknameValidation> checkNicknameAvailability(@AuthenticationPrincipal User user,
                                                                        @RequestParam("nickname") @NicknameConstraint String nickname) {
        return ResponseEntity
                .status(OK)
                .body(userService.isNicknameAvailable(user, nickname));
    }

    @PostMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> registerUserInfo(@AuthenticationPrincipal User user,
                                                 @Valid @RequestBody RegisterUserInfoRequest registerUserInfoRequest) {
        userService.registerUserInfo(user, registerUserInfoRequest);
        return ResponseEntity
                .status(CREATED)
                .build();
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<ProfileGetResponse> getProfileInfo(@AuthenticationPrincipal User user,
                                                             @PathVariable("userId") Long userId) {
        return ResponseEntity
                .status(OK)
                .body(userService.getProfileInfo(user, userId));
    }

    @GetMapping("/my-profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MyProfileResponse> getMyProfileInfo(@AuthenticationPrincipal User user) {
        return ResponseEntity
                .status(OK)
                .body(userService.getMyProfileInfo(user));
    }

    @PatchMapping("/my-profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateMyProfileInfo(@AuthenticationPrincipal User user,
                                                    @RequestBody @Valid UpdateMyProfileRequest updateMyProfileRequest) {
        userService.updateMyProfileInfo(user, updateMyProfileRequest);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @GetMapping("/profile-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileStatusResponse> getProfileStatus(@AuthenticationPrincipal User user) {
        return ResponseEntity
                .status(OK)
                .body(userService.getProfileStatus(user));
    }

    @PatchMapping("/profile-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> editProfileStatus(@AuthenticationPrincipal User user,
                                                  @Valid @RequestBody EditProfileStatusRequest editProfileStatusRequest) {
        userService.editProfileStatus(user, editProfileStatusRequest);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserIdAndNicknameResponse> getUserIdAndNicknameAndGender(@AuthenticationPrincipal User user) {
        return ResponseEntity
                .status(OK)
                .body(userService.getUserIdAndNicknameAndGender(user));
    }

    @GetMapping("/{userId}/novels")
    public ResponseEntity<UserNovelAndNovelsGetResponse> getUserNovelsAndNovels(@AuthenticationPrincipal User visitor,
                                                                                @PathVariable("userId") Long userId,
                                                                                @RequestParam(value = "isInterest", required = false) Boolean isInterest,
                                                                                @RequestParam(value = "readStatuses", required = false) List<String> readStatuses,
                                                                                @RequestParam(value = "attractivePoints", required = false) List<String> attractivePoints,
                                                                                @RequestParam(value = "novelRating", required = false) Float novelRating,
                                                                                @RequestParam(value = "query", required = false) String query,
                                                                                @RequestParam("lastUserNovelId") Long lastUserNovelId,
                                                                                @RequestParam("size") int size,
                                                                                @RequestParam("sortCriteria") SortCriteria sortCriteria,
                                                                                @RequestParam(value = "updatedSince", required = false) LocalDateTime updatedSince) {
        return ResponseEntity
                .status(OK)
                .body(userNovelService.getUserNovelsAndNovels(
                        visitor, userId, isInterest, readStatuses, attractivePoints, novelRating, query,
                        lastUserNovelId, size, sortCriteria, updatedSince));
    }

    @GetMapping("/{userId}/feeds")
    public ResponseEntity<UserFeedsGetResponse> getUserFeeds(@AuthenticationPrincipal User visitor,
                                                             @PathVariable("userId") Long userId,
                                                             @RequestParam("lastFeedId") Long lastFeedId,
                                                             @RequestParam("size") int size,
                                                             @RequestParam(value = "isVisible", required = false) Boolean isVisible,
                                                             @RequestParam(value = "isUnVisible", required = false) Boolean isUnVisible,
                                                             @RequestParam(value = "genreNames", required = false) List<String> genreNames,
                                                             @RequestParam(value = "sortCriteria", required = false) SortCriteria sortCriteria) {
        return ResponseEntity
                .status(OK)
                // ToDo: isVisible -> isPublic으로 수정
                .body(feedService.getUserFeeds(visitor, userId, lastFeedId, size, isVisible, isUnVisible, genreNames,
                        sortCriteria));
    }

    @GetMapping("/{userId}/preferences/genres")
    public ResponseEntity<UserGenrePreferencesGetResponse> getUserGenrePreferences(
            @AuthenticationPrincipal User visitor,
            @PathVariable("userId") Long ownerId) {
        return ResponseEntity
                .status(OK)
                .body(userNovelService.getUserGenrePreferences(visitor, ownerId));
    }

    @GetMapping("/{userId}/preferences/attractive-points")
    public ResponseEntity<UserTasteAttractivePointPreferencesAndKeywordsGetResponse>
    getUserAttractivePointsAndKeywords(@AuthenticationPrincipal User visitor,
                                       @PathVariable("userId") Long ownerId) {
        return ResponseEntity
                .status(OK)
                .body(userNovelService.getUserAttractivePointsAndKeywords(visitor, ownerId));
    }

    @GetMapping("/{userId}/user-novel-stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserNovelCountGetResponse> getUserNovelStatistics(@PathVariable("userId") Long userId) {
        return ResponseEntity
                .status(OK)
                .body(userNovelService.getUserNovelStatistics(userId));
    }

    @GetMapping("/info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserInfoGetResponse> getUserInfo(@AuthenticationPrincipal User user) {
        return ResponseEntity
                .status(OK)
                .body(userService.getUserInfo(user));
    }

    @PutMapping("/info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> editMyInfo(@AuthenticationPrincipal User user,
                                           @Valid @RequestBody EditMyInfoRequest editMyInfoRequest) {
        userService.editMyInfo(user, editMyInfoRequest);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @PostMapping("/push-settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> registerPushSetting(@AuthenticationPrincipal User user,
                                                    @Valid @RequestBody PushSettingRequest pushSettingRequest) {
        userService.registerPushSetting(user, pushSettingRequest.isPushEnabled());
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @GetMapping("/push-settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PushSettingGetResponse> getPushSettingValue(@AuthenticationPrincipal User user) {
        return ResponseEntity
                .status(OK)
                .body(userService.getPushSettingValue(user));
    }

    @GetMapping("/terms-settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TermsSettingGetResponse> getTermsSettingValue(@AuthenticationPrincipal User user) {
        return ResponseEntity
                .status(OK)
                .body(userService.getTermsSettingValue(user));
    }

    @PatchMapping("/terms-settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateTermsSetting(@AuthenticationPrincipal User user,
                                                   @Valid @RequestBody TermsSettingRequest termsSettingRequest) {
        userService.updateTermsSetting(user, termsSettingRequest.serviceAgreed(), termsSettingRequest.privacyAgreed(),
                termsSettingRequest.marketingAgreed());
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }
}
