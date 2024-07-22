package org.websoso.WSSServer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Year;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.websoso.WSSServer.domain.common.Gender;
import org.websoso.WSSServer.domain.common.Role;
import org.websoso.WSSServer.dto.user.UserBasicInfo;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long userId;

    @Column(unique = true, columnDefinition = "varchar(10)", nullable = false)
    private String nickname;
    //TODO 일부 특수문자 제외, 앞뒤 공백 불가능

    @Column(columnDefinition = "varchar(60) default '안녕하세요'", nullable = false)
    private String intro;

    @Column(columnDefinition = "varchar(320)", nullable = false)
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'USER'")
    private Role role;

    public void updateProfileStatus(Boolean profileStatus) {
        this.isProfilePublic = profileStatus;
    }

    public void updateUserInfo(String nickname, Gender gender, Year birth) {
        this.nickname = nickname;
        this.gender = gender;
        this.birth = birth;
    }

    public UserBasicInfo getUserBasicInfo(String avatarImage) {
        return UserBasicInfo.of(this.getUserId(), this.getNickname(), avatarImage);
    }

}
