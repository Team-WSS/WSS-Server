package org.websoso.WSSServer.feed.application;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.common.SortCriteria;
import org.websoso.WSSServer.dto.feed.UserFeedGetResponse;
import org.websoso.WSSServer.dto.feed.UserFeedsGetResponse;
import org.websoso.WSSServer.dto.novel.NovelGetResponseFeedTab;
import org.websoso.WSSServer.dto.popularFeed.PopularFeedGetResponse;
import org.websoso.WSSServer.feed.service.FeedImageService;
import org.websoso.WSSServer.feed.service.FeedLikeService;
import org.websoso.WSSServer.novel.service.GenreServiceImpl;
import org.websoso.WSSServer.user.domain.AvatarProfile;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.common.FeedGetOption;
import org.websoso.WSSServer.dto.feed.FeedGetResponse;
import org.websoso.WSSServer.dto.feed.FeedInfo;
import org.websoso.WSSServer.dto.feed.FeedsGetResponse;
import org.websoso.WSSServer.dto.feed.InterestFeedGetResponse;
import org.websoso.WSSServer.dto.feed.InterestFeedsGetResponse;
import org.websoso.WSSServer.dto.popularFeed.PopularFeedsGetResponse;
import org.websoso.WSSServer.dto.user.UserBasicInfo;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.domain.PopularFeed;
import org.websoso.WSSServer.feed.service.CommentServiceImpl;
import org.websoso.WSSServer.feed.service.FeedServiceImpl;
import org.websoso.WSSServer.library.service.LibraryService;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.AvatarService;
import org.websoso.WSSServer.user.service.BlockService;
import org.websoso.WSSServer.user.service.UserService;

@Service
@RequiredArgsConstructor
public class FeedFindApplication {

    private static final int DEFAULT_PAGE_NUMBER = 0;

    private final GenreServiceImpl genreService;
    private final UserService userService;
    private final FeedServiceImpl feedServiceImpl;
    private final FeedImageService feedImageService;
    private final NovelServiceImpl novelServiceImpl;
    private final AvatarService avatarService;
    private final FeedLikeService feedLikeService;
    private final CommentServiceImpl commentServiceImpl;
    private final BlockService blockService;
    private final LibraryService libraryService;

    @Transactional(readOnly = true)
    public FeedGetResponse getFeedById(User user, Long feedId) {

        Feed feed = feedServiceImpl.getAccessFeedOrException(feedId, user.getUserId());

        // 서로 차단 관계인지 체크한다.
        blockService.validateNotBlocked(user.getUserId(), feed.getWriterId());

        // 피드 작성자의 프로필 이미지를 불러온다.
        AvatarProfile avatarProfile = avatarService.getAvatarProfileOrException(feed.getUser().getAvatarProfileId());
        String avatarImageUrl = avatarProfile.getAvatarProfileImage();

        // 피드 작성자의 사용자 정보 전달 객체 생성
        UserBasicInfo feedUserBasicInfo = UserBasicInfo.of(feed.getUser().getUserId(), feed.getUser().getNickname(), avatarImageUrl);

        // 피드에 연결된 소설 정보 가져오기
        Novel novel = getLinkedNovelOrNull(feed.getNovelId());

        // 사용자가 현재 피드에 좋아요를 했는지 여부 체크
        boolean isLiked = feedLikeService.isUserLikedFeed(user.getUserId(), feed);

        // 피드가 본인 피드인지 체크
        boolean isMyFeed = feed.isMine(user.getUserId());

        return FeedGetResponse.of(feed, feedUserBasicInfo, novel, isLiked, isMyFeed);
    }

    @Transactional(readOnly = true)
    public FeedsGetResponse getFeeds(User user, Long lastFeedId, int size, FeedGetOption feedGetOption) {

        // 로그인 유저 여부 확인
        Long userIdOrNull = Optional.ofNullable(user).map(User::getUserId).orElse(null);

        // 사용자의 선호하는 장르 확인
        List<Genre> genres = user == null ? null : genreService.findUserPreferenceGenres(user);

        // 피드 불러오기
        Slice<Feed> feeds = feedServiceImpl.findFeedsByCategoryLabel(lastFeedId, userIdOrNull, PageRequest.of(DEFAULT_PAGE_NUMBER, size), feedGetOption, genres);

        // TODO: feed -> feed.isVisibleTo(userIdOrNull) 해당 필터링 로직은 필요 없음
        List<Feed> visibleFeeds = feeds.getContent().stream().filter(feed -> feed.isVisibleTo(userIdOrNull))
                .toList();

        return FeedsGetResponse.of(feeds.hasNext(), createFeedInfos(visibleFeeds, user));
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

    @Transactional(readOnly = true)
    public InterestFeedsGetResponse getInterestFeeds(User user) {
        List<Novel> interestNovels = libraryService.getInterestNovels(user);

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
        Map<Long, AvatarProfile> avatarMap = avatarService.findAllByIds(new ArrayList<>(avatarProfileIds)).stream()
                .collect(Collectors.toMap(AvatarProfile::getAvatarProfileId, avatar -> avatar));

        List<InterestFeedGetResponse> interestFeedGetResponses = interestFeeds.stream()
                .filter(feed -> feed.isVisibleTo(user.getUserId())).map(feed -> {
                    Novel novel = novelMap.get(feed.getNovelId());
                    AvatarProfile avatar = avatarMap.get(feed.getUser().getAvatarProfileId());
                    return InterestFeedGetResponse.of(novel, feed.getUser(), feed, avatar);
                }).toList();
        return InterestFeedsGetResponse.of(interestFeedGetResponses, "");
    }

    @Transactional(readOnly = true)
    public NovelGetResponseFeedTab getFeedsByNovel(User user, Long novelId, Long lastFeedId, int size) {

        // 있는 웹소설인지 체크
        novelServiceImpl.getNovelOrException(novelId);

        Long userIdOrNull = Optional.ofNullable(user).map(User::getUserId).orElse(null);

        Slice<Feed> feeds = feedServiceImpl.findFeedsByNovel(userIdOrNull, novelId, lastFeedId, size);

        List<Feed> visibleFeeds = feeds.getContent();

        return NovelGetResponseFeedTab.of(feeds.hasNext(), createFeedInfos(visibleFeeds, user));
    }

    @Transactional(readOnly = true)
    public UserFeedsGetResponse getUserFeeds(User visitor, Long ownerId, Long lastFeedId, int size, Boolean isVisible,
                                             Boolean isUnVisible, List<String> genreNames, SortCriteria sortCriteria) {

        User owner = userService.getUserOrException(ownerId);

        Long visitorId = Optional.ofNullable(visitor).map(User::getUserId).orElse(null);

        userService.validateProfileAccessible(owner, visitorId);

        boolean includeEtc = genreNames != null && genreNames.contains("etc");
        List<String> filteredGenreNames = genreNames == null
                ? null
                : genreNames.stream().filter(name -> !name.equals("etc")).collect(Collectors.toList());
        List<Genre> genres = genreService.getGenresOrException(filteredGenreNames);

        List<Feed> visibleFeeds = feedServiceImpl.getViewableUserFeed(owner, lastFeedId, size, isVisible,
                isUnVisible, sortCriteria, genres, visitorId, includeEtc);

        List<Long> novelIds = visibleFeeds.stream().map(Feed::getNovelId).filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 소설 ID에 해당하는 소설 정보들 전부 불러오기
        List<Novel> novels = novelServiceImpl.getNovelsWithGenresByIds(novelIds);

        //
        Map<Long, Novel> novelMap = novels.stream()
                .collect(Collectors.toMap(Novel::getNovelId, Function.identity()));

        List<UserFeedGetResponse> userFeedGetResponseList = createUserFeedResponses(visibleFeeds, novelMap, visitorId);

        // TODO Slice의 hasNext()로 판단하도록 수정
        Boolean isLoadable = visibleFeeds.size() == size;

        long feedsCount = feedServiceImpl.getViewableUserFeedCount(owner, isVisible, isUnVisible, genres, visitorId, includeEtc);

        return UserFeedsGetResponse.of(isLoadable, feedsCount, userFeedGetResponseList);

    }

    private Novel getLinkedNovelOrNull(Long linkedNovelId) {
        if (linkedNovelId == null) {
            return null;
        }
        return novelServiceImpl.getNovelOrException(linkedNovelId);
    }

    private Boolean isUserFeedOwner(User createdUser, User user) {
        return createdUser.equals(user);
    }

    private List<FeedInfo> createFeedInfos(List<Feed> feeds, User user) {
        FeedInfoContext context = getFeedInfoContext(feeds, user);

        return feeds.stream()
                .map(feed -> {
                    Long feedId = feed.getFeedId();
                    UserBasicInfo userBasicInfo = context.userBasicInfoMap().get(feed.getUser().getUserId());
                    Novel novel = context.novelMap().get(feed.getNovelId());
                    boolean isLiked = context.likedFeedIds().contains(feedId);
                    boolean isMyFeed = user != null && isUserFeedOwner(feed.getUser(), user);
                    String thumbnailUrl = context.thumbnailUrlMap().get(feedId);
                    Integer imageCount = context.imageCountMap().getOrDefault(feedId, 0);
                    Integer likeCount = context.likeCountMap().getOrDefault(feedId, 0);
                    Integer commentCount = context.commentCountMap().getOrDefault(feedId, 0);

                    return FeedInfo.of(
                            feed,
                            userBasicInfo,
                            novel,
                            isLiked,
                            isMyFeed,
                            thumbnailUrl,
                            imageCount,
                            user,
                            likeCount,
                            commentCount
                    );
                })
                .toList();
    }

    private List<PopularFeedGetResponse> mapToPopularFeedGetResponseList(
            List<PopularFeed> popularFeeds,
            Map<Long, Novel> novelMap
    ) {
        return popularFeeds.stream()
                .map(popularFeed -> {
                    Novel novel = novelMap.get(popularFeed.getFeed().getNovelId());

                    return PopularFeedGetResponse.of(
                            popularFeed,
                            novel == null ? null : novel.getTitle(),
                            novel == null ? null : novel.getNovelImage(),
                            novel == null ? null : novel.getFirstGenreName()
                    );
                })
                .toList();
    }

    private FeedInfoContext getFeedInfoContext(List<Feed> feeds, User user) {
        List<Long> feedIds = feeds.stream()
                .map(Feed::getFeedId)
                .toList();
        List<Long> novelIds = feeds.stream()
                .map(Feed::getNovelId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<Long> avatarProfileIds = feeds.stream()
                .map(feed -> feed.getUser().getAvatarProfileId())
                .distinct()
                .toList();

        Map<Long, String> avatarImageMap = avatarService.findAllByIds(avatarProfileIds).stream()
                .collect(Collectors.toMap(AvatarProfile::getAvatarProfileId, AvatarProfile::getAvatarProfileImage));
        Map<Long, UserBasicInfo> userBasicInfoMap = feeds.stream()
                .map(Feed::getUser)
                .collect(Collectors.toMap(
                        User::getUserId,
                        feedUser -> feedUser.getUserBasicInfo(avatarImageMap.get(feedUser.getAvatarProfileId())),
                        (first, second) -> first
                ));

        Map<Long, Novel> novelMap = novelServiceImpl.getNovelsWithGenresByIds(novelIds).stream()
                .collect(Collectors.toMap(Novel::getNovelId, Function.identity()));
        Set<Long> likedFeedIds = new HashSet<>(feedLikeService.findLikedFeedIds(
                Optional.ofNullable(user).map(User::getUserId).orElse(null),
                feedIds
        ));
        Map<Long, Integer> likeCountMap = feedLikeService.countByFeedIds(feedIds);
        Map<Long, Integer> commentCountMap = commentServiceImpl.countByFeedIds(feedIds);
        Map<Long, String> thumbnailUrlMap = feedImageService.getThumbnailUrlMap(feedIds);
        Map<Long, Integer> imageCountMap = feedImageService.getImageCountMap(feedIds);

        return new FeedInfoContext(
                userBasicInfoMap,
                novelMap,
                likedFeedIds,
                likeCountMap,
                commentCountMap,
                thumbnailUrlMap,
                imageCountMap
        );
    }

    private List<UserFeedGetResponse> createUserFeedResponses(
            List<Feed> feeds,
            Map<Long, Novel> novelMap,
            Long visitorId
    ) {
        List<Long> feedIds = feeds.stream()
                .map(Feed::getFeedId)
                .toList();
        Map<Long, List<Long>> likerUserIdsMap = feedLikeService.findLikerUserIdsByFeedIds(feedIds);
        Map<Long, Integer> likeCountMap = feedLikeService.countByFeedIds(feedIds);
        Map<Long, Integer> commentCountMap = commentServiceImpl.countByFeedIds(feedIds);
        Map<Long, String> thumbnailUrlMap = feedImageService.getThumbnailUrlMap(feedIds);
        Map<Long, Integer> imageCountMap = feedImageService.getImageCountMap(feedIds);

        return feeds.stream()
                .map(feed -> {
                    Long feedId = feed.getFeedId();

                    return UserFeedGetResponse.of(
                            feed,
                            novelMap.get(feed.getNovelId()),
                            visitorId,
                            thumbnailUrlMap.get(feedId),
                            imageCountMap.getOrDefault(feedId, 0),
                            likerUserIdsMap.getOrDefault(feedId, Collections.emptyList()),
                            likeCountMap.getOrDefault(feedId, 0),
                            commentCountMap.getOrDefault(feedId, 0)
                    );
                })
                .toList();
    }

    private record FeedInfoContext(
            Map<Long, UserBasicInfo> userBasicInfoMap,
            Map<Long, Novel> novelMap,
            Set<Long> likedFeedIds,
            Map<Long, Integer> likeCountMap,
            Map<Long, Integer> commentCountMap,
            Map<Long, String> thumbnailUrlMap,
            Map<Long, Integer> imageCountMap
    ) {
    }

}
