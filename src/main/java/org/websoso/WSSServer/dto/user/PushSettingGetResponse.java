package org.websoso.WSSServer.dto.user;

public record PushSettingGetResponse(
        Boolean isPushEnabled
) {

    public static PushSettingGetResponse of(Boolean isPushEnabled) {
        return new PushSettingGetResponse(isPushEnabled);
    }
}
