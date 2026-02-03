package org.websoso.WSSServer.user.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AvatarProfile {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long avatarProfileId;

    @Column(columnDefinition = "varchar(30)", nullable = false)
    private String avatarProfileName;

    @Column(columnDefinition = "varchar(100)", nullable = false)
    private String avatarProfileImage;

    @Column(columnDefinition = "varchar(100)", nullable = false)
    private String avatarCharacterImage;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "avatar_profile_id")
    private List<AvatarProfileLine> avatarLines;

    public boolean isSameAvatarProfile(Long avatarProfileId) {
        return this.avatarProfileId.equals(avatarProfileId);
    }
}
