package org.websoso.WSSServer.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GenreName {

    RF("romanceFantasy"),
    RO("romance"),
    FA("fantasy"),
    MF("modernFantasy"),
    DR("drama"),
    LN("lightNovel"),
    WU("wuxia"),
    MY("mystery"),
    BL("BL");

    private final String label;

}
