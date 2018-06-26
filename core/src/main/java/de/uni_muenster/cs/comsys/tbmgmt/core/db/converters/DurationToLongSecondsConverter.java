package de.uni_muenster.cs.comsys.tbmgmt.core.db.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.Duration;

/**
 Created by matthias on 22.03.15.
 */
@Converter
public class DurationToLongSecondsConverter implements AttributeConverter<Duration, Long> {
    @Override
    public Long convertToDatabaseColumn(final Duration attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getSeconds();
    }

    @Override
    public Duration convertToEntityAttribute(final Long dbData) {
        if (dbData == null) {
            return null;
        }
        return Duration.ofSeconds(dbData);
    }
}
