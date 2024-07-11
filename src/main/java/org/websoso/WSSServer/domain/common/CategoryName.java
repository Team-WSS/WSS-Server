package org.websoso.WSSServer.domain.common;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    private final String label;

    private static final Map<String, CategoryName> BY_LABEL =
            Stream.of(values()).collect(Collectors.toMap(CategoryName::getLabel, e -> e));

    public static CategoryName of(String label) {
        return BY_LABEL.get(label);
    }

}
