package com.wss.websoso.user;

import com.wss.websoso.userAvatar.UserAvatar;
import com.wss.websoso.userNovel.UserNovel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_nickname", length = 10, nullable = false)
    private String userNickname;

    // 정규 표현식 적용 및 nullable 추가해야함
    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "user_rep_avatar_id", nullable = false)
    private Long userRepAvatarId;

    @Column(name = "user_written_memo_count", columnDefinition = "bigint default 0", nullable = false)
    private Long userWrittenMemoCount;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<UserAvatar> userAvatars = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<UserNovel> userNovels = new ArrayList<>();

    public int getMemoCount() {
        int memoCount = 0;
        for (UserNovel userNovel : getUserNovels()) {
            memoCount += userNovel.getMemos().size();
        }
        return memoCount;
    }

    public void updateUserNickname(String newUserNickname) {
        this.userNickname = newUserNickname;
    }

    public void updateUserRepAvatar(Long avatarId) {
        this.userRepAvatarId = avatarId;
    }

    public void updateUserWrittenMemoCount() {
        this.userWrittenMemoCount++;
    }
}
