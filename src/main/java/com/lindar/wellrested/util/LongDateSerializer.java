package com.lindar.wellrested.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LongDateSerializer implements JsonSerializer<Date> {

    @Override
    public JsonElement serialize(Date src, Type arg1, JsonSerializationContext arg2) throws JsonParseException {
        return src == null ? null : new JsonPrimitive(src.getTime());
    }
}
