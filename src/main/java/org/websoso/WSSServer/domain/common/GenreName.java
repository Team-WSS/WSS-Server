package org.websoso.WSSServer.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GenreName {

    RF("romanceFantasy", "로판"),
    RO("romance", "로맨스"),
    FA("fantasy", "판타지"),
    MF("modernFantasy", "현판"),
    DR("drama", "드라마"),
    LN("lightNovel", "라노벨"),
    WU("wuxia", "무협"),
    MY("mystery", "미스터리"),
    BL("BL", "BL");

    private final String label;
    private final String koreanLabel;

}
