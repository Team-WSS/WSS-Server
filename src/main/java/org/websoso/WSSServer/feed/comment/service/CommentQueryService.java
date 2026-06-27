package org.websoso.WSSServer.feed.comment.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.feed.comment.controller.dto.CommentGetResponse;
import org.websoso.WSSServer.feed.comment.repository.CommentQueryRepository;
import org.websoso.WSSServer.feed.comment.repository.projection.CommentInfoRow;

@Service
@RequiredArgsConstructor
public class CommentQueryService {

    private final CommentQueryRepository commentQueryRepository;

    @Transactional(readOnly = true)
    public List<CommentGetResponse> findCommentRows(Long feedId, Long userId, List<Long> blockedUserIds) {
        return commentQueryRepository.findCommentInfoRows(feedId, userId, blockedUserIds).stream()
                .map(CommentInfoRow::toResponse)
                .toList();
    }
}
