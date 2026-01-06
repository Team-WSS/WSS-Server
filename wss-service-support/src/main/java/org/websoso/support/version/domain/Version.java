package org.websoso.support.version.domain;

import static org.websoso.support.version.exception.CustomMinimumVersionError.INVALID_VERSION;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.websoso.support.version.exception.CustomMinimumVersionException;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Version {

    @Column(name = "minimum_version", columnDefinition = "varchar(20)", nullable = false)
    private String value;

    public static Version of(String value) {
        return new Version(value);
    }

    private Version(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        if (value == null || !Pattern.matches("^\\d+\\.\\d+\\.\\d+$", value)) {
            throw new CustomMinimumVersionException(INVALID_VERSION,
                    "The version must be in the 'major.minor.patch' format");
        }
    }

    @Override
    public String toString() {
        return "Version: " + value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Version version = (Version) o;
        return Objects.equals(value, version.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
