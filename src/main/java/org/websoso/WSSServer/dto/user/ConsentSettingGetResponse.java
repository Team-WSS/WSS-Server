package org.websoso.WSSServer.dto.user;

public record ConsentSettingGetResponse(
        Boolean serviceAgreed,
        Boolean privacyAgreed,
        Boolean marketingAgreed
) {
    public static ConsentSettingGetResponse of(Boolean serviceAgreed, Boolean privacyAgreed, Boolean marketingAgreed) {
        return new ConsentSettingGetResponse(
                serviceAgreed,
                privacyAgreed,
                marketingAgreed
        );
    }
}
