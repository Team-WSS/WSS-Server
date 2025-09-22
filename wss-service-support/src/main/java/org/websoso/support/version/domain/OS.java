package org.websoso.support.version.domain;

import static org.websoso.support.version.exception.CustomMinimumVersionError.OS_NOT_FOUND;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.support.version.exception.CustomMinimumVersionException;

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
