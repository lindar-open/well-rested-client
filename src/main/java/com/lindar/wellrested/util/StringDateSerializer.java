package com.lindar.wellrested.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * This class serializes all date to a string using the provided default format or the default one when nothing is provided.
 * The default format is: <b>yyyy-MM-dd'T'HH:mm:ss</b>
 * <br/>
 * Note: If you'd like a different format please use the one argument constructor and pass a custom date format.
 * <br/>
 * Note: If you'd like your date object to be serialized to Long, please use {@link com.lindar.wellrested.util.LongDateSerializer}
 */
@Slf4j
@NoArgsConstructor
public class StringDateSerializer implements JsonSerializer<Date> {

    private static final String DEF_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private String dateFormat = StringUtils.EMPTY;
    
    public StringDateSerializer(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    public JsonElement serialize(Date src, Type arg1, JsonSerializationContext arg2) throws JsonParseException {
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat(StringUtils.defaultIfBlank(dateFormat, DEF_DATE_FORMAT));
        return new JsonPrimitive(formatter.format(src));
    }
}
