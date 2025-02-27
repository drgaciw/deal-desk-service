package com.aciworldwide.dealdesk.mapper;

import org.mapstruct.Qualifier;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class MapStructDateTimeConverter {

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface CurrentTimestamp {}

    @CurrentTimestamp
    public ZonedDateTime getCurrentTimestamp() {
        return ZonedDateTime.now(ZoneId.systemDefault());
    }
}