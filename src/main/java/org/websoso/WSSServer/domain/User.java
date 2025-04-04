package org.websoso.WSSServer.domain;

import static jakarta.persistence.CascadeType.ALL;
import static org.websoso.WSSServer.domain.common.Gender.M;
import static org.websoso.WSSServer.domain.common.Role.USER;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.websoso.WSSServer.domain.common.BaseEntity;
import org.websoso.WSSServer.domain.common.Gender;
import org.websoso.WSSServer.domain.common.Role;
import org.websoso.WSSServer.dto.user.EditMyInfoRequest;
import org.websoso.WSSServer.dto.user.RegisterUserInfoRequest;
import org.websoso.WSSServer.dto.user.UpdateMyProfileRequest;
import org.websoso.WSSServer.dto.user.UserBasicInfo;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "UNIQUE_NICKNAME_CONSTRAINT",
                columnNames = "nickname"),
        @UniqueConstraint(
                name = "UNIQUE_SOCIAL_ID_CONSTRAINT",
                columnNames = "social_id")
})
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String socialId;

    @Column(columnDefinition = "varchar(10)", nullable = false)
    private String nickname;
    //TODO 일부 특수문자 제외, 앞뒤 공백 불가능

    @Column(columnDefinition = "varchar(60) default '안녕하세요'", nullable = false)
    private String intro;

    @Column(columnDefinition = "varchar(320)")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private Year birth;
    //TODO 유효성 체크

    @Column(columnDefinition = "tinyint default 1", nullable = false)
    private Byte avatarId;

    @Column(columnDefinition = "Boolean default true", nullable = false)
    private Boolean isProfilePublic;

    @Column(columnDefinition = "Boolean default true", nullable = false)
    private Boolean isPushEnabled;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean serviceAgreed;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean privacyAgreed;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean marketingAgreed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'USER'")
    private Role role;

    @OneToMany(mappedBy = "user", cascade = ALL, orphanRemoval = true)
    private List<GenrePreference> genrePreferences = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = ALL, orphanRemoval = true)
    private List<UserNovel> userNovels = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = ALL, orphanRemoval = true)
    private List<ReportedFeed> reportedFeeds = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = ALL, orphanRemoval = true)
    private List<ReportedComment> reportedComments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = ALL, orphanRemoval = true)
    private List<UserDevice> userDevices = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = ALL, orphanRemoval = true)
    private List<ReadNotification> readNotifications = new ArrayList<>();

    public void updateProfileStatus(Boolean profileStatus) {
        this.isProfilePublic = profileStatus;
    }

    public void updateUserProfile(UpdateMyProfileRequest updateMyProfileRequest) {
        if (updateMyProfileRequest.avatarId() != null) {
            this.avatarId = updateMyProfileRequest.avatarId();
        }
        if (updateMyProfileRequest.nickname() != null) {
            this.nickname = updateMyProfileRequest.nickname();
        }
        if (updateMyProfileRequest.intro() != null) {
            this.intro = updateMyProfileRequest.intro();
        }
    }

    public void updateUserInfo(RegisterUserInfoRequest registerUserInfoRequest) {
        this.nickname = registerUserInfoRequest.nickname();
        this.gender = Gender.valueOf(registerUserInfoRequest.gender());
        this.birth = Year.of(registerUserInfoRequest.birth());
    }

    public UserBasicInfo getUserBasicInfo(String avatarImage) {
        return UserBasicInfo.of(this.getUserId(), this.getNickname(), avatarImage);
    }

    private User(String socialId, String nickname, String email) {
        this.intro = "안녕하세요";
        this.gender = M;
        this.birth = Year.now();
        this.avatarId = 1;
        this.isProfilePublic = true;
        this.isPushEnabled = true;
        this.serviceAgreed = false;
        this.privacyAgreed = false;
        this.marketingAgreed = false;
        this.role = USER;
        this.socialId = socialId;
        this.nickname = nickname;
        this.email = email;
    }

    public static User createBySocial(String socialId, String nickname, String email) {
        return new User(socialId, nickname, email);
    }

    public void editMyInfo(EditMyInfoRequest editMyInfoRequest) {
        this.gender = Gender.valueOf(editMyInfoRequest.gender());
        this.birth = Year.of(editMyInfoRequest.birth());
    }

    public void updatePushSetting(boolean isPushEnabled) {
        this.isPushEnabled = isPushEnabled;
    }

    public void updateTermsSetting(boolean serviceAgreed, boolean privacyAgreed, boolean marketingAgreed) {
        this.serviceAgreed = serviceAgreed;
        this.privacyAgreed = privacyAgreed;
        this.marketingAgreed = marketingAgreed;
    }
}
