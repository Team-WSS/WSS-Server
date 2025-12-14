package org.websoso.WSSServer.user.domain;

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
import org.websoso.WSSServer.domain.GenrePreference;
import org.websoso.WSSServer.domain.ReadNotification;
import org.websoso.WSSServer.feed.domain.ReportedComment;
import org.websoso.WSSServer.feed.domain.ReportedFeed;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.common.entity.BaseEntity;
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

    // TODO: 우선 연관 관계를 직접 맺지 않게 수정
    @Column(nullable = false)
    private Long avatarProfileId;

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

    // TODO: DTO에 의존적인 DDD 로직 (기중)
    @Deprecated
    public void updateUserProfile(UpdateMyProfileRequest updateMyProfileRequest) {
        if (updateMyProfileRequest.avatarId() != null) {
            this.avatarId = updateMyProfileRequest.avatarId().byteValue();
            this.avatarProfileId = updateMyProfileRequest.getMappedAvatarId();
        }
        if (updateMyProfileRequest.nickname() != null) {
            this.nickname = updateMyProfileRequest.nickname();
        }
        if (updateMyProfileRequest.intro() != null) {
            this.intro = updateMyProfileRequest.intro();
        }
    }

    public void updateUserProfile(Long avatarProfileId, String nickname, String intro) {
        if (avatarProfileId != null) {
            this.avatarProfileId = avatarProfileId;
        }
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (intro != null) {
            this.intro = intro;
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

    // TODO: 유저 객체 생성시, 기본 값이 바로 박혀있으면 확장성에서 불리하다고 생각 (기중)
    // DEFAULT 값이 하드코딩되어 있음 -> private static final로 분리하는게 좋지 않을까 생각
    // 컬럼 매핑에 DEFAULT가 선언되어 있어서 중복이라고 생각할 수 있지만, 2차 적으로 어플리케이션에도 있는게 컨텍스트 파악하는데 유리하다고 생각
    private User(String socialId, String nickname, String email) {
        this.intro = "안녕하세요";
        this.gender = M;
        this.birth = Year.now();
        this.avatarId = 1;
        this.avatarProfileId = 1L;
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

    // TODO: 소셜 로그인 가입 처럼 이러한 메서드에서 기본 값 설정하는게 더 유리할 것으로 판단 (기중)
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
