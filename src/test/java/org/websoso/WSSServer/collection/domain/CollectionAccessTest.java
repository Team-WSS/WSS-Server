package org.websoso.WSSServer.collection.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.PRIVATE_COLLECTION_ACCESS;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.common.exception.ICustomError;

class CollectionAccessTest {

    private static final long COLLECTION_ID = 100L;
    private static final long OWNER_ID = 1L;
    private static final long OTHER_USER_ID = 999L;

    @DisplayName("공개 컬렉션은 누구나 접근할 수 있다")
    @Test
    void allowsAnyoneToAccessPublicCollection() {
        CollectionAccess collection = access(true);

        assertThatCode(() -> collection.validateVisibleTo(OTHER_USER_ID)).doesNotThrowAnyException();
        assertThatCode(() -> collection.validateVisibleTo(null)).doesNotThrowAnyException();
    }

    @DisplayName("소유자는 자신의 비공개 컬렉션에 접근할 수 있다")
    @Test
    void allowsOwnerToAccessOwnPrivateCollection() {
        CollectionAccess collection = access(false);

        assertThat(collection.isOwnedBy(OWNER_ID)).isTrue();
        assertThatCode(() -> collection.validateVisibleTo(OWNER_ID)).doesNotThrowAnyException();
    }

    @DisplayName("다른 사용자의 비공개 컬렉션 접근은 403 컬렉션 도메인 예외로 처리한다")
    @Test
    void rejectsPrivateCollectionAccessByOthers() {
        CollectionAccess collection = access(false);

        assertThat(collection.isOwnedBy(OTHER_USER_ID)).isFalse();
        assertThatThrownBy(() -> collection.validateVisibleTo(OTHER_USER_ID))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .asInstanceOf(InstanceOfAssertFactories.type(ICustomError.class))
                .satisfies(error -> {
                    assertThat(error).isEqualTo(PRIVATE_COLLECTION_ACCESS);
                    assertThat(error.getStatusCode().value()).isEqualTo(403);
                });
    }

    @DisplayName("비로그인 사용자는 비공개 컬렉션에 접근할 수 없다")
    @Test
    void rejectsPrivateCollectionAccessByAnonymous() {
        CollectionAccess collection = access(false);

        assertThat(collection.isOwnedBy(null)).isFalse();
        assertThatThrownBy(() -> collection.validateVisibleTo(null))
                .isInstanceOf(CustomCollectionException.class);
    }

    private CollectionAccess access(boolean isPublic) {
        return new CollectionAccess(COLLECTION_ID, OWNER_ID, isPublic);
    }
}
