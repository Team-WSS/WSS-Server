package org.websoso.WSSServer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.Like;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.repository.LikeRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

    private final LikeRepository likeRepository;

    public void createLike(User user, Feed feed) {
        likeRepository.save(Like.create(user.getUserId(), feed));
    }

    public void deleteLike(User user, Feed feed) {
        likeRepository.deleteByUserIdAndFeed(user.getUserId(), feed);
    }

}
