package org.websoso.WSSServer.recentsearch.event;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.recentsearch.service.RecentSearchService;

@ExtendWith(MockitoExtension.class)
class RecentSearchEventListenerTest {

    @InjectMocks
    private RecentSearchEventListener listener;

    @Mock
    private RecentSearchService recentSearchService;

    @Nested
    @DisplayName("NovelSearchedEvent 처리")
    class OnNovelSearchedEvent {

        @DisplayName("이벤트를 수신하면 RecentSearchService.add를 호출한다")
        @Test
        void callsAddOnEvent() {
            // given
            NovelSearchedEvent event = new NovelSearchedEvent(1L, "소설 제목");

            // when
            listener.on(event);

            // then
            then(recentSearchService).should().add(1L, "소설 제목");
        }

        @DisplayName("서비스에서 예외가 발생해도 외부로 전파되지 않는다")
        @Test
        void swallowsExceptionFromService() {
            // given
            NovelSearchedEvent event = new NovelSearchedEvent(1L, "소설 제목");
            willThrow(new RuntimeException("DB error")).given(recentSearchService).add(1L, "소설 제목");

            // when & then
            assertThatCode(() -> listener.on(event)).doesNotThrowAnyException();
        }
    }
}
