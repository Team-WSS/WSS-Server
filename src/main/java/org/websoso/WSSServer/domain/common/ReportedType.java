package org.websoso.WSSServer.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportedType {

    SPOILER("spoiler", "스포일러"),
    IMPERTINENCE("impertinence", "부적절한 표현");

    private final String label;
    private final String description;

}
