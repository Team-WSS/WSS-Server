package org.websoso.support.version.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.websoso.support.version.exception.CustomMinimumVersionException;

class VersionTest {

    @DisplayName("유효한 형식의 버전 문자열이 주어지면, Version 객체를 생성한다.")
    @ParameterizedTest
    @ValueSource(strings = {"0.0.0", "1.2.3", "12.34.56"})
    void CreatesVersionIfFormatIsValid(String validVersionString) {
        // when
        Version version = Version.of(validVersionString);

        // then
        assertThat(version.getValue()).isEqualTo(validVersionString);
    }

    @DisplayName("유효하지 않은 형식의 버전 문자열이 주어지면, 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"1.0", "1.2.3.", ".1.2.3", "a.b.c", "1.a.3", " "})
    void ThrowsExceptionIfFormatIsInvalid(String invalidVersionString) {
        // when & then
        assertThatThrownBy(() -> Version.of(invalidVersionString))
                .isInstanceOf(CustomMinimumVersionException.class)
                .hasMessage("The version must be in the 'major.minor.patch' format");
    }
}
