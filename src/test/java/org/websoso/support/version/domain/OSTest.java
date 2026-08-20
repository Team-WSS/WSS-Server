package org.websoso.support.version.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.websoso.support.version.exception.CustomMinimumVersionException;

class OSTest {

    @DisplayName("문자열을 통해 OS를 찾을 수 있다.")
    @Test
    void returnsMinimumVersionIfOsExists() {
        // given
        String androidLabel = "android";
        String iosLabel = "ios";

        // when
        OS android = OS.fromLabel(androidLabel);
        OS ios = OS.fromLabel(iosLabel);

        // then
        assertThat(android).isEqualTo(OS.ANDROID);
        assertThat(ios).isEqualTo(OS.IOS);
    }

    @DisplayName("잘못된 문자열로 OS를 찾으려 하면 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {"", "windows", "linux"})
    void throwsExceptionIfOsDoesNotExist(String invalidLabel) {
        // when & then
        assertThatThrownBy(() -> OS.fromLabel(invalidLabel))
                .isInstanceOf(CustomMinimumVersionException.class);
    }
}
