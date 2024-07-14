package org.websoso.WSSServer.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReadStatus {
    WATCHING("보는 중"),
    WATCHED("봤어요"),
    QUIT("하차");

    private final String label;
}
