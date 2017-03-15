package com.lindar.wellrested.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StringDateSerializer implements JsonSerializer<Date> {

    private static final String DEF_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    @Override
    public JsonElement serialize(Date src, Type arg1, JsonSerializationContext arg2) throws JsonParseException {
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat(DEF_DATE_FORMAT);
        return new JsonPrimitive(formatter.format(src));
    }
}
