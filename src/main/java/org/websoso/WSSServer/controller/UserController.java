package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
import org.websoso.WSSServer.dto.feed.UserFeedsGetResponse;
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

    @GetMapping("/nickname/check")
    public ResponseEntity<NicknameValidation> checkNicknameAvailability(Principal principal,
                                                                        @RequestParam("nickname")
                                                                        @NicknameConstraint String nickname) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        return ResponseEntity
                .status(OK)
                .body(userService.isNicknameAvailable(user, nickname));
    }

    @GetMapping("/info")
    public ResponseEntity<UserInfoGetResponse> getUserInfo(Principal principal) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        return ResponseEntity
                .status(OK)
                .body(userService.getUserInfo(user));
    }

    @GetMapping("/profile-status")
    public ResponseEntity<ProfileStatusResponse> getProfileStatus(Principal principal) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        return ResponseEntity
                .status(OK)
                .body(userService.getProfileStatus(user));
    }

    @PatchMapping("/profile-status")
    public ResponseEntity<Void> editProfileStatus(Principal principal,
                                                  @Valid @RequestBody EditProfileStatusRequest editProfileStatusRequest) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        userService.editProfileStatus(user, editProfileStatusRequest);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody String userId) {
        LoginResponse response = userService.login(Long.valueOf(userId));
        return ResponseEntity
                .status(OK)
                .body(response);
    }

    @GetMapping("/my-profile")
    public ResponseEntity<MyProfileResponse> getMyProfileInfo(Principal principal) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        return ResponseEntity
                .status(OK)
                .body(userService.getMyProfileInfo(user));
    }

    @PatchMapping("/my-profile")
    public ResponseEntity<Void> updateMyProfileInfo(Principal principal,
                                                    @RequestBody @Valid UpdateMyProfileRequest updateMyProfileRequest) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        userService.updateMyProfileInfo(user, updateMyProfileRequest);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<ProfileGetResponse> getProfileInfo(Principal principal,
                                                             @PathVariable("userId") Long userId) {
        User user = principal == null
                ? null
                : userService.getUserOrException(Long.valueOf(principal.getName()));
        return ResponseEntity
                .status(OK)
                .body(userService.getProfileInfo(user, userId));
    }

    @PostMapping("/profile")
    public ResponseEntity<Void> registerUserInfo(Principal principal,
                                                 @Valid @RequestBody RegisterUserInfoRequest registerUserInfoRequest) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        userService.registerUserInfo(user, registerUserInfoRequest);
        return ResponseEntity
                .status(CREATED)
                .build();
    }

    @GetMapping("/{userId}/user-novel-stats")
    public ResponseEntity<UserNovelCountGetResponse> getUserNovelStatistics(@PathVariable("userId") Long userId) {
        return ResponseEntity
                .status(OK)
                .body(userNovelService.getUserNovelStatistics(userId));
    }

    @GetMapping("/{userId}/novels")
    public ResponseEntity<UserNovelAndNovelsGetResponse> getUserNovelsAndNovels(Principal principal,
                                                                                @PathVariable("userId") Long userId,
                                                                                @RequestParam("readStatus") String readStatus,
                                                                                @RequestParam("lastUserNovelId") Long lastUserNovelId,
                                                                                @RequestParam("size") int size,
                                                                                @RequestParam("sortType") String sortType) {
        User visitor = principal == null
                ? null
                : userService.getUserOrException(Long.valueOf(principal.getName()));
        return ResponseEntity
                .status(OK)
                .body(userNovelService.getUserNovelsAndNovels(
                        visitor, userId, readStatus, lastUserNovelId, size, sortType));
    }

    @GetMapping("/{userId}/feeds")
    public ResponseEntity<UserFeedsGetResponse> getUserFeeds(Principal principal,
                                                             @PathVariable("userId") Long userId,
                                                             @RequestParam("lastFeedId") Long lastFeedId,
                                                             @RequestParam("size") int size) {
        User visitor = principal == null
                ? null
                : userService.getUserOrException(Long.valueOf(principal.getName()));
        return ResponseEntity
                .status(OK)
                .body(feedService.getUserFeeds(visitor, userId, lastFeedId, size));
    }

    @GetMapping("/{userId}/preferences/genres")
    public ResponseEntity<UserGenrePreferencesGetResponse> getUserGenrePreferences(Principal principal,
                                                                                   @PathVariable("userId") Long ownerId) {
        User visitor = principal == null
                ? null
                : userService.getUserOrException(Long.valueOf(principal.getName()));
        return ResponseEntity
                .status(OK)
                .body(userNovelService.getUserGenrePreferences(visitor, ownerId));
    }

    @GetMapping("/{userId}/preferences/attractive-points")
    public ResponseEntity<UserTasteAttractivePointPreferencesAndKeywordsGetResponse>
    getUserAttractivePointsAndKeywords(Principal principal,
                                       @PathVariable("userId") Long ownerId) {
        User visitor = principal == null
                ? null
                : userService.getUserOrException(Long.valueOf(principal.getName()));
        return ResponseEntity
                .status(OK)
                .body(userNovelService.getUserAttractivePointsAndKeywords(visitor, ownerId));
    }

    @PutMapping("/info")
    public ResponseEntity<Void> editMyInfo(Principal principal,
                                           @Valid @RequestBody EditMyInfoRequest editMyInfoRequest) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        userService.editMyInfo(user, editMyInfoRequest);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserIdAndNicknameResponse> getUserIdAndNicknameAndGender(Principal principal) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        return ResponseEntity
                .status(OK)
                .body(userService.getUserIdAndNicknameAndGender(user));
    }

    @PostMapping("/fcm-token")
    public ResponseEntity<Void> registerFCMToken(Principal principal,
                                                 @Valid @RequestBody FCMTokenRequest fcmTokenRequest) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        userService.registerFCMToken(user, fcmTokenRequest);
        return ResponseEntity
                .status(OK)
                .build();
    }

    @GetMapping("/push-settings")
    public ResponseEntity<PushSettingGetResponse> getPushSettingValue(Principal principal) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        return ResponseEntity
                .status(OK)
                .body(userService.getPushSettingValue(user));
    }
}
