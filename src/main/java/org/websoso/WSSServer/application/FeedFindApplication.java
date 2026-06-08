package org.websoso.WSSServer.application;

import static org.websoso.WSSServer.exception.error.CustomAvatarError.AVATAR_NOT_FOUND;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.dto.popularFeed.PopularFeedGetResponse;
import org.websoso.WSSServer.user.domain.AvatarProfile;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.GenrePreference;
import org.websoso.WSSServer.domain.common.FeedGetOption;
import org.websoso.WSSServer.dto.feed.FeedGetResponse;
import org.websoso.WSSServer.dto.feed.FeedInfo;
import org.websoso.WSSServer.dto.feed.FeedsGetResponse;
import org.websoso.WSSServer.dto.feed.InterestFeedGetResponse;
import org.websoso.WSSServer.dto.feed.InterestFeedsGetResponse;
import org.websoso.WSSServer.dto.popularFeed.PopularFeedsGetResponse;
import org.websoso.WSSServer.dto.user.UserBasicInfo;
import org.websoso.WSSServer.exception.exception.CustomAvatarException;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.domain.FeedImage;
import org.websoso.WSSServer.feed.domain.PopularFeed;
import org.websoso.WSSServer.feed.repository.LikeRepository;
import org.websoso.WSSServer.feed.service.FeedServiceImpl;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.repository.UserNovelRepository;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;
import org.websoso.WSSServer.user.repository.AvatarProfileRepository;
import org.websoso.WSSServer.repository.GenrePreferenceRepository;
import org.websoso.WSSServer.user.domain.User;

@Service
@RequiredArgsConstructor
public class FeedFindApplication {

    private final FeedServiceImpl feedServiceImpl;
    private final NovelServiceImpl novelServiceImpl;

    private static final int DEFAULT_PAGE_NUMBER = 0;

    //ToDo : 의존성 제거 필요 부분
    private final AvatarProfileRepository avatarRepository;
    private final LikeRepository likeRepository;
    private final GenrePreferenceRepository genrePreferenceRepository;
    private final UserNovelRepository userNovelRepository;

    @Transactional(readOnly = true)
    public FeedGetResponse getFeedById(User user, Long feedId) {
        Feed feed = feedServiceImpl.getFeedOrException(feedId);
        UserBasicInfo feedUserBasicInfo = getUserBasicInfo(feed.getUser());
        Novel novel = getLinkedNovelOrNull(feed.getNovelId());
        Boolean isLiked = isUserLikedFeed(user, feed);
        Boolean isMyFeed = isUserFeedOwner(feed.getUser(), user);

        return FeedGetResponse.of(feed, feedUserBasicInfo, novel, isLiked, isMyFeed);
    }

    private UserBasicInfo getUserBasicInfo(User user) {
        return user.getUserBasicInfo(
                avatarRepository.findById(user.getAvatarProfileId()).orElseThrow(() ->
                                new CustomAvatarException(AVATAR_NOT_FOUND, "avatar with the given id was not found"))
                        .getAvatarProfileImage());
    }

    private Novel getLinkedNovelOrNull(Long linkedNovelId) {
        if (linkedNovelId == null) {
            return null;
        }
        return novelServiceImpl.getNovelOrException(linkedNovelId);
    }

    private Boolean isUserLikedFeed(User user, Feed feed) {
        return likeRepository.existsByUserIdAndFeed(user.getUserId(), feed);
    }

    private Boolean isUserFeedOwner(User createdUser, User user) {
        return createdUser.equals(user);
    }

    @Transactional(readOnly = true)
    public FeedsGetResponse getFeeds(User user, Long lastFeedId, int size,
                                     FeedGetOption feedGetOption) {
        Long userIdOrNull = Optional.ofNullable(user).map(User::getUserId).orElse(null);

        List<Genre> genres = getPreferenceGenres(user);

        Slice<Feed> feeds = findFeedsByCategoryLabel(lastFeedId, userIdOrNull,
                PageRequest.of(DEFAULT_PAGE_NUMBER, size), feedGetOption, genres);

        // TODO: feed -> feed.isVisibleTo(userIdOrNull) 해당 필터링 로직은 필요 없음
        List<FeedInfo> feedGetResponses = feeds.getContent().stream().filter(feed -> feed.isVisibleTo(userIdOrNull))
                .map(feed -> createFeedInfo(feed, user)).toList();

        return FeedsGetResponse.of(feeds.hasNext(), feedGetResponses);
    }

    private List<Genre> getPreferenceGenres(User user) {
        if (user == null) {
            return null;
        }
        return genrePreferenceRepository.findByUser(user).stream().map(GenrePreference::getGenre).toList();
    }

    private Slice<Feed> findFeedsByCategoryLabel(Long lastFeedId, Long userId, PageRequest pageRequest,
                                                 FeedGetOption feedGetOption, List<Genre> genres) {
        return feedServiceImpl.findFeedsByCategoryLabel(lastFeedId, userId, pageRequest, feedGetOption,
                genres);
    }

    private FeedInfo createFeedInfo(Feed feed, User user) {
        UserBasicInfo userBasicInfo = getUserBasicInfo(feed.getUser());
        Novel novel = getLinkedNovelOrNull(feed.getNovelId());
        Boolean isLiked = user != null && isUserLikedFeed(user, feed);
        Boolean isMyFeed = user != null && isUserFeedOwner(feed.getUser(), user);
        Integer imageCount = feedServiceImpl.countByFeedId(feed.getFeedId());
        Optional<FeedImage> thumbnailImage = feedServiceImpl.findThumbnailFeedImageByFeedId(feed.getFeedId());
        String thumbnailUrl = thumbnailImage.map(FeedImage::getUrl).orElse(null);

        return FeedInfo.of(feed, userBasicInfo, novel, isLiked, isMyFeed, thumbnailUrl, imageCount, user);
    }

    @Transactional(readOnly = true)
    public PopularFeedsGetResponse getPopularFeeds(User user, int size) {
        List<PopularFeed> popularFeeds = Optional.ofNullable(user)
                .map(u -> feedServiceImpl.findPopularFeedsWithUser(u.getUserId(), size))
                .orElseGet(() -> feedServiceImpl.findPopularFeedsWithoutUser(size));

        // TODO: PopularFeeds에 이런 메서드들이 더 많으면 일급 함수 객체 만들어도 괜찮을듯
        List<Long> novelIds = popularFeeds.stream()
                .map(f -> f.getFeed().getNovelId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Novel> novelMap = novelServiceImpl.getNovelsWithGenresByIds(novelIds)
                .stream()
                .collect(Collectors.toMap(
                        Novel::getNovelId,
                        Function.identity()
                ));

        List<PopularFeedGetResponse> popularFeedGetResponses = mapToPopularFeedGetResponseList(popularFeeds, novelMap);

        return PopularFeedsGetResponse.of(popularFeedGetResponses);
    }

    private static List<PopularFeedGetResponse> mapToPopularFeedGetResponseList(
            List<PopularFeed> popularFeeds,
            Map<Long, Novel> novelMap
    ) {
        return popularFeeds.stream()
                .map(popularFeed -> {
                    Novel novel = novelMap.get(popularFeed.getFeed().getNovelId());

                    return PopularFeedGetResponse.of(
                            popularFeed,
                            novel == null ? null : novel.getNovelImage(),
                            novel == null ? null : novel.getFirstGenreName()
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public InterestFeedsGetResponse getInterestFeeds(User user) {
        List<Novel> interestNovels = userNovelRepository.findByUserAndIsInterestTrue(user).stream()
                .map(UserNovel::getNovel).toList();

        if (interestNovels.isEmpty()) {
            return InterestFeedsGetResponse.of(Collections.emptyList(), "NO_INTEREST_NOVELS");
        }

        Map<Long, Novel> novelMap = interestNovels.stream()
                .collect(Collectors.toMap(Novel::getNovelId, novel -> novel));
        List<Long> interestNovelIds = new ArrayList<>(novelMap.keySet());

        List<Feed> interestFeeds = feedServiceImpl.findInterestFeeds(interestNovelIds);

        if (interestFeeds.isEmpty()) {
            return InterestFeedsGetResponse.of(Collections.emptyList(), "NO_ASSOCIATED_FEEDS");
        }

        Set<Long> avatarProfileIds = interestFeeds.stream().map(feed -> feed.getUser().getAvatarProfileId())
                .collect(Collectors.toSet());
        Map<Long, AvatarProfile> avatarMap = avatarRepository.findAllById(avatarProfileIds).stream()
                .collect(Collectors.toMap(AvatarProfile::getAvatarProfileId, avatar -> avatar));

        List<InterestFeedGetResponse> interestFeedGetResponses = interestFeeds.stream()
                .filter(feed -> feed.isVisibleTo(user.getUserId())).map(feed -> {
                    Novel novel = novelMap.get(feed.getNovelId());
                    AvatarProfile avatar = avatarMap.get(feed.getUser().getAvatarProfileId());
                    return InterestFeedGetResponse.of(novel, feed.getUser(), feed, avatar);
                }).toList();
        return InterestFeedsGetResponse.of(interestFeedGetResponses, "");
    }
}
