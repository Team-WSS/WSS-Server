package org.websoso.WSSServer.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KeywordCategory {
    WORLDVIEW("세계관"),
    MATERIAL("소재"),
    CHARACTER("캐릭터"),
    RELATIONSHIP("관계"),
    VIBE("분위기");

    private final String name;
}
