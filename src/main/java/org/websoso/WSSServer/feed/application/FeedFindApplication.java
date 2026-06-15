package org.websoso.WSSServer.feed.application;

import java.util.*;
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
import org.websoso.WSSServer.feed.service.FeedImageService;
import org.websoso.WSSServer.feed.service.FeedLikeService;
import org.websoso.WSSServer.feed.service.FeedQueryService;
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
    private final FeedQueryService feedQueryService;
    private final NovelServiceImpl novelServiceImpl;
    private final AvatarService avatarService;
    private final FeedLikeService feedLikeService;
    private final BlockService blockService;
    private final LibraryService libraryService;

    @Transactional(readOnly = true)
    public FeedGetResponse getFeedById(User user, Long feedId) {

        // 접근 가능한 피드인지 체크 및 피드 불러오기
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

        // FeedInfo에 필요한 정보들을 JOIN 및 서브 쿼리로 불러오기
        List<Feed> visibleFeeds = feeds.getContent().stream().toList();
        List<FeedInfo> feedInfos = feedQueryService.findFeedInfoRows(visibleFeeds, userIdOrNull);

        return FeedsGetResponse.of(feeds.hasNext(), feedInfos);
    }

    @Transactional(readOnly = true)
    public PopularFeedsGetResponse getPopularFeeds(User user, int size) {

        // 사용자의 차단 목록 조회
        List<Long> blockedUserIds = Optional.ofNullable(user)
                .map(User::getUserId)
                .map(blockService::findBlockRelationUserIds)
                .orElseGet(Collections::emptyList);

        return PopularFeedsGetResponse.of(feedQueryService.findPopularFeedRows(blockedUserIds, size));
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
        List<FeedInfo> feedInfos = feedQueryService.findFeedInfoRows(visibleFeeds, userIdOrNull);

        return NovelGetResponseFeedTab.of(feeds.hasNext(), feedInfos);
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

        List<UserFeedGetResponse> userFeedGetResponseList = feedQueryService.findUserFeedRows(visibleFeeds, visitorId);

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

}
