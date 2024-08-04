package org.websoso.WSSServer.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CategoryName {

    romanceFantasy("로판"),
    romance("로맨스"),
    fantasy("판타지"),
    modernFantasy("현판"),
    drama("드라마"),
    lightNovel("라이트노벨"),
    wuxia("무협"),
    mystery("미스터리"),
    BL("BL"),
    etc("기타");

    private final String label;

}
