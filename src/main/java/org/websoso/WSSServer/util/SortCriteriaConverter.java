package org.websoso.WSSServer.util;

import org.springframework.core.convert.converter.Converter;
import org.websoso.WSSServer.domain.common.SortCriteria;

public class SortCriteriaConverter implements Converter<String, SortCriteria> {

    @Override
    public SortCriteria convert(final String sortCriteria) {
        return SortCriteria.create(sortCriteria.toUpperCase());
    }
}
