package org.websoso.WSSServer.collection.controller.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CollectionRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @DisplayName("유효한 생성 요청은 검증을 통과한다")
    @Test
    void acceptsValidCreateRequest() {
        assertThat(validate(createRequest("이름", "설명", true, List.of(1L), 1L))).isEmpty();
    }

    @DisplayName("생성 요청은 공개 여부를 생략할 수 있다")
    @Test
    void allowsOmittedIsPublicOnCreate() {
        assertThat(validate(createRequest("이름", "설명", null, List.of(1L), 1L))).isEmpty();
    }

    @DisplayName("수정 요청은 공개 여부를 생략할 수 없다")
    @Test
    void requiresIsPublicOnUpdate() {
        assertThat(validate(updateRequest("이름", "설명", null, List.of(1L), 1L)))
                .containsExactly("공개 여부는 null일 수 없습니다.");
    }

    @DisplayName("컬렉션 이름이 없거나 공백뿐이면 검증에 실패한다")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    void rejectsBlankName(String name) {
        assertThat(validate(createRequest(name, "설명", true, List.of(1L), 1L)))
                .contains("컬렉션 이름은 비어 있거나, 공백일 수 없습니다.");
    }

    @DisplayName("컬렉션 이름은 공백을 포함해 20자까지 허용한다")
    @Test
    void acceptsNameUpToTwentyCharacters() {
        assertThat(validate(createRequest("가".repeat(19) + " ", "설명", true, List.of(1L), 1L))).isEmpty();
    }

    @DisplayName("컬렉션 이름이 20자를 초과하면 검증에 실패한다")
    @Test
    void rejectsTooLongName() {
        assertThat(validate(createRequest("가".repeat(21), "설명", true, List.of(1L), 1L)))
                .containsExactly("컬렉션 이름은 20자를 초과할 수 없습니다.");
    }

    @DisplayName("컬렉션 설명은 선택이며 60자까지 허용한다")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "설명"})
    void acceptsOptionalDescription(String description) {
        assertThat(validate(createRequest("이름", description, true, List.of(1L), 1L))).isEmpty();
    }

    @DisplayName("컬렉션 설명이 60자를 초과하면 검증에 실패한다")
    @Test
    void rejectsTooLongDescription() {
        assertThat(validate(createRequest("이름", "가".repeat(61), true, List.of(1L), 1L)))
                .containsExactly("컬렉션 설명은 60자를 초과할 수 없습니다.");
    }

    @DisplayName("작품 목록이 null이면 검증에 실패한다")
    @Test
    void rejectsNullNovelIds() {
        assertThat(validate(createRequest("이름", "설명", true, null, 1L)))
                .containsExactly("컬렉션에 포함할 작품 목록은 null일 수 없습니다.");
    }

    @DisplayName("작품 목록에 null이 섞이면 검증에 실패한다")
    @Test
    void rejectsNullNovelIdElement() {
        assertThat(validate(createRequest("이름", "설명", true, Arrays.asList(1L, null), 1L)))
                .containsExactly("컬렉션에 포함할 작품 ID는 null일 수 없습니다.");
    }

    @DisplayName("대표 작품이 null이면 검증에 실패한다")
    @Test
    void rejectsNullRepresentativeNovelId() {
        assertThat(validate(createRequest("이름", "설명", true, List.of(1L), null)))
                .containsExactly("대표 작품은 null일 수 없습니다.");
    }

    @DisplayName("작품 수와 중복, 대표 작품 포함 여부는 DTO가 아닌 컬렉션 도메인이 검증한다")
    @Test
    void leavesNovelPolicyToDomain() {
        assertThat(validate(createRequest("이름", "설명", true, List.of(), 1L))).isEmpty();
        assertThat(validate(createRequest("이름", "설명", true, List.of(1L, 1L), 1L))).isEmpty();
        assertThat(validate(createRequest("이름", "설명", true, List.of(1L), 999L))).isEmpty();
    }

    private CollectionCreateRequest createRequest(String name, String description, Boolean isPublic,
                                                  List<Long> novelIds, Long representativeNovelId) {
        return new CollectionCreateRequest(name, description, isPublic, novelIds, representativeNovelId);
    }

    private CollectionUpdateRequest updateRequest(String name, String description, Boolean isPublic,
                                                  List<Long> novelIds, Long representativeNovelId) {
        return new CollectionUpdateRequest(name, description, isPublic, novelIds, representativeNovelId);
    }

    private <T> List<String> validate(T request) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();
    }
}
