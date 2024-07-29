package org.websoso.WSSServer.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportedType {
    
    SPOILER("spoiler"),
    IMPERTINENCE("Impertinence");

    private final String label;

}
