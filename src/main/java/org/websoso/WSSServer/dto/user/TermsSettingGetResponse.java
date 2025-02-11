package org.websoso.WSSServer.dto.user;

public record TermsSettingGetResponse(
        Boolean serviceAgreed,
        Boolean privacyAgreed,
        Boolean marketingAgreed
) {
    public static TermsSettingGetResponse of(Boolean serviceAgreed, Boolean privacyAgreed, Boolean marketingAgreed) {
        return new TermsSettingGetResponse(
                serviceAgreed,
                privacyAgreed,
                marketingAgreed
        );
    }
}
