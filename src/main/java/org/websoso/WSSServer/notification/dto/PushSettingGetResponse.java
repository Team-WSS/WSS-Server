package org.websoso.WSSServer.notification.dto;

public record PushSettingGetResponse(
        Boolean isPushEnabled
) {

    public static PushSettingGetResponse of(Boolean isPushEnabled) {
        return new PushSettingGetResponse(isPushEnabled);
    }
}
