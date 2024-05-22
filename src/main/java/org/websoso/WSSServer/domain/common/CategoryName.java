package org.websoso.WSSServer.domain.common;

import lombok.Getter;

@Getter
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

    CategoryName(String value) {
        this.value = value;
    }

}
