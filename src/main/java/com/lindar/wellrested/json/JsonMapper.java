package com.lindar.wellrested.json;

import java.lang.reflect.Type;

public interface JsonMapper {
    <T> T readValue(String value, Type valueType);
    String writeValue(Object value);
}
