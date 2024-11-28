package org.websoso.WSSServer.domain.common;

import static org.websoso.WSSServer.exception.error.CustomMinimumVersionError.OS_NOT_FOUND;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.exception.CustomMinimumVersionException;

@Getter
@AllArgsConstructor
public enum OS {

    ANDROID("android"),
    IOS("ios");

    private final String label;

    public static OS fromLabel(String osLabel) {
        return Arrays.stream(OS.values())
                .filter(os -> os.label.equalsIgnoreCase(osLabel))
                .findFirst()
                .orElseThrow(() -> new CustomMinimumVersionException(OS_NOT_FOUND,
                        "the requested OS value is invalid or not supported"));
    }
}
