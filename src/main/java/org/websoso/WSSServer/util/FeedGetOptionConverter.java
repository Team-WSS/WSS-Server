package org.websoso.WSSServer.util;

import org.springframework.core.convert.converter.Converter;
import org.websoso.WSSServer.domain.common.FeedGetOption;

public class FeedGetOptionConverter implements Converter<String, FeedGetOption> {

    @Override
    public FeedGetOption convert(final String feedGetOption) {
        return FeedGetOption.create(feedGetOption.toUpperCase());
    }
}
