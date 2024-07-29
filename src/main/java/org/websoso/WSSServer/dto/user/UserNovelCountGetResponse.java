package org.websoso.WSSServer.dto.user;


public record UserNovelCountGetResponse(
        Integer interestNovelCount,
        Integer watchingNovelCount,
        Integer watchedNovelCount,
        Integer quitNovelCount) {
}
