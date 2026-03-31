package org.websoso.WSSServer.feed.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.common.FeedGetOption;
import org.websoso.WSSServer.exception.exception.CustomGenreException;
import org.websoso.WSSServer.feed.repository.FeedImageCustomRepository;
import org.websoso.WSSServer.feed.repository.FeedImageRepository;
import org.websoso.WSSServer.feed.repository.FeedRepository;
import org.websoso.WSSServer.feed.repository.PopularFeedRepository;
import org.websoso.WSSServer.repository.GenreRepository;

@ExtendWith(MockitoExtension.class)
class FeedServiceImplTest {

    @Mock
    FeedRepository feedRepository;
    @Mock
    FeedImageRepository feedImageRepository;
    @Mock
    FeedImageCustomRepository feedImageCustomRepository;
    @Mock
    PopularFeedRepository popularFeedRepository;
    @Mock
    GenreRepository genreRepository;

    @InjectMocks
    FeedServiceImpl feedServiceImpl;

    private PageRequest pageRequest;
    private static final Long LAST_FEED_ID = 0L;
    private static final Long USER_ID = 1L;
    private static final int SIZE = 10;

    @BeforeEach
    void setUp() {
        pageRequest = PageRequest.of(0, SIZE);
    }

    @Test
    void ALL_옵션이면_findFeeds를_호출한다() {
        feedServiceImpl.findFeedsByCategoryLabel(LAST_FEED_ID, USER_ID, pageRequest, FeedGetOption.ALL, null);

        verify(feedRepository).findFeeds(LAST_FEED_ID, USER_ID, pageRequest);
        verify(feedRepository, never()).findRecommendedFeeds(any(), any(), any(), any());
        verify(feedRepository, never()).findFeedsByGenres(any(), anyBoolean(), any(), any(), any());
    }

    @Test
    void RECOMMENDED_옵션이면_findRecommendedFeeds를_유저_선호장르로_호출한다() {
        Genre prefGenre = mock(Genre.class);
        List<Genre> preferenceGenres = List.of(prefGenre);

        feedServiceImpl.findFeedsByCategoryLabel(LAST_FEED_ID, USER_ID, pageRequest,
                FeedGetOption.RECOMMENDED, preferenceGenres);

        verify(feedRepository).findRecommendedFeeds(LAST_FEED_ID, USER_ID, pageRequest, preferenceGenres);
        verify(feedRepository, never()).findFeeds(any(), any(), any());
    }

    @Test
    void feedGetOption이_null이면_ALL로_동작한다() {
        feedServiceImpl.findFeedsByCategoryLabel(LAST_FEED_ID, USER_ID, pageRequest, null, null);

        verify(feedRepository).findFeeds(LAST_FEED_ID, USER_ID, pageRequest);
    }
}