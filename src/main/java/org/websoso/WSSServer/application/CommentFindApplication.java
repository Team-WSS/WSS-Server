package org.websoso.WSSServer.application;

import static org.websoso.WSSServer.exception.error.CustomAvatarError.AVATAR_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;

import java.util.AbstractMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.user.repository.AvatarProfileRepository;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.dto.comment.CommentGetResponse;
import org.websoso.WSSServer.dto.comment.CommentsGetResponse;
import org.websoso.WSSServer.dto.user.UserBasicInfo;
import org.websoso.WSSServer.exception.exception.CustomAvatarException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.service.FeedServiceImpl;
import org.websoso.WSSServer.user.repository.BlockRepository;
import org.websoso.WSSServer.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CommentFindApplication {

    private final FeedServiceImpl feedServiceImpl;

    //ToDo : 의존성 제거 필요 부분
    private final UserRepository userRepository;
    private final BlockRepository blockRepository;
    private final AvatarProfileRepository avatarProfileRepository;

    @Transactional(readOnly = true)
    public CommentsGetResponse getComments(User user, Long feedId) {
        Feed feed = feedServiceImpl.getFeedOrException(feedId);
        List<CommentGetResponse> responses = feed.getComments().stream()
                .map(comment -> new AbstractMap.SimpleEntry<>(comment, userRepository.findById(comment.getUserId())
                        .orElseThrow(
                                () -> new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"))))
                .map(entry -> CommentGetResponse.of(getUserBasicInfo(entry.getValue()), entry.getKey(),
                        isUserCommentOwner(entry.getValue(), user), entry.getKey().getIsSpoiler(),
                        isBlocked(user, entry.getValue()), entry.getKey().getIsHidden())).toList();

        return CommentsGetResponse.of(responses);
    }

    private Boolean isUserCommentOwner(User createdUser, User user) {
        return createdUser.equals(user);
    }

    private Boolean isBlocked(User user, User createdFeedUser) {
        return blockRepository.existsByBlockingIdAndBlockedId(user.getUserId(), createdFeedUser.getUserId());
    }

    private UserBasicInfo getUserBasicInfo(User user) {
        return user.getUserBasicInfo(
                avatarProfileRepository.findById(user.getAvatarProfileId()).orElseThrow(() ->
                                new CustomAvatarException(AVATAR_NOT_FOUND, "avatar with the given id was not found"))
                        .getAvatarProfileImage());
    }
}
