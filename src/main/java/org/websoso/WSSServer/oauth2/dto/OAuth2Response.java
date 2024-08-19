package org.websoso.WSSServer.oauth2.dto;

public interface OAuth2Response {

    String getProvider();

    String getProviderId();

    String getName();

    String getEmail();
}
