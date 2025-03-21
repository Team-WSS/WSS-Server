package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomFeedError.ALREADY_LIKED;
import static org.websoso.WSSServer.exception.error.CustomFeedError.NOT_LIKED;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.Like;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.exception.exception.CustomFeedException;
import org.websoso.WSSServer.repository.LikeRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

    private final LikeRepository likeRepository;

    public void createLike(User user, Feed feed) {
        if (isUserLikedFeed(user, feed)) {
            throw new CustomFeedException(ALREADY_LIKED, "user already liked that feed");
        }
        likeRepository.save(Like.create(user.getUserId(), feed));
    }

    public void deleteLike(User user, Feed feed) {
        if (!isUserLikedFeed(user, feed)) {
            throw new CustomFeedException(NOT_LIKED, "user has not liked that feed");
        }
        likeRepository.deleteByUserIdAndFeed(user.getUserId(), feed);
    }

    @Transactional(readOnly = true)
    public boolean isUserLikedFeed(User user, Feed feed) {
        return likeRepository.existsByUserIdAndFeed(user.getUserId(), feed);
    }

}
