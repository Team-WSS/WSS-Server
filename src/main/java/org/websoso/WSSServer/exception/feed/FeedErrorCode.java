package org.websoso.WSSServer.exception.feed;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.common.IErrorCode;

@Getter
@AllArgsConstructor
public enum FeedErrorCode implements IErrorCode {

    FEED_NOT_FOUND("FEED-001", "해당 ID를 가진 피드를 찾을 수 없습니다.");

    private final String code;
    private final String description;
}
