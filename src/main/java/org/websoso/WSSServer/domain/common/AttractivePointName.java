package org.websoso.WSSServer.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AttractivePointName {
    WORLDVIEW("worldview"),
    MATERIAL("material"),
    CHARACTER("character"),
    RELATIONSHIP("relationship"),
    VIBE("vibe");

    private final String label;
}
