package org.websoso.WSSServer.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CategoryName {
    RF("romanceFantasy"),
    RO("romance"),
    FA("fantasy"),
    MF("modernFantasy"),
    DR("drama"),
    LN("lightNovel"),
    WU("wuxia"),
    MY("mystery"),
    BL("BL"),
    ETC("etc");

    private final String value;
    
}
