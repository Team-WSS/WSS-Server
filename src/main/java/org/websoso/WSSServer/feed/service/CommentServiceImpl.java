package org.websoso.WSSServer.feed.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.comment.CommentCreateRequest;
import org.websoso.WSSServer.feed.domain.Comment;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.repository.CommentRepository;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl {

    private final CommentRepository commentRepository;

    public void createComment(User user, Feed feed, CommentCreateRequest request) {
        commentRepository.save(Comment.create(user.getUserId(), feed, request.commentContent()));
    }
}
