package org.websoso.WSSServer.dto.user;

public record ConsentSettingGetResponse(
        Boolean isTermsAgreed,
        Boolean isPrivacyConsented,
        Boolean isMarketingConsented
) {
    public static ConsentSettingGetResponse of(Boolean isTermsAgreed, Boolean isPrivacyConsented,
                                               Boolean isMarketingConsented) {
        return new ConsentSettingGetResponse(
                isTermsAgreed,
                isPrivacyConsented,
                isMarketingConsented
        );
    }
}
