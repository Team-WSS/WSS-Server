package org.websoso.support.version.dto;

import org.websoso.support.version.domain.OS;
import org.websoso.support.version.domain.Version;

public record UpdateMinimumVersionCommand(
        OS os,
        Version version
) {
    public static UpdateMinimumVersionCommand of(String os, String version) {
        return new UpdateMinimumVersionCommand(
                OS.fromLabel(os),
                Version.of(version)
        );
    }
}
