package lindar.wellrested.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;

@Slf4j
public class DateDeserializer implements JsonDeserializer<Date> {

    private static final List<String> DEF_DATE_FORMATS;

    static {
        DEF_DATE_FORMATS = new ArrayList<>(3);
        DEF_DATE_FORMATS.add("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        DEF_DATE_FORMATS.add("yyyy-MM-dd'T'HH:mm:ss.SSS");
        DEF_DATE_FORMATS.add("yyyy-MM-dd'T'HH:mm:ss");
        DEF_DATE_FORMATS.add("yyyy-MM-dd");
        
    }

    private final List<String> dateFormats;

    public DateDeserializer(List<String> dateFormats) {
        this.dateFormats = dateFormats;
    }

    public DateDeserializer(String dateFormat) {
        this.dateFormats = Arrays.asList(dateFormat);
    }

    public DateDeserializer() {
        this.dateFormats = DEF_DATE_FORMATS;
    }

    @Override
    public Date deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
        String date = element.getAsString();
        
        if (NumberUtils.isDigits(date) && date.length() > 4) {
            return new Date(Long.parseLong(date));
        }
        
        SimpleDateFormat formatter;

        for (String format : dateFormats) {
            try {
                formatter = new SimpleDateFormat(format);
                return formatter.parse(date);
            } catch (ParseException exp) {
                continue;
            }
        }
        log.warn("Failed to parse the document's date variables using any of the registered formats: {}", dateFormats);
        return null;
    }
}
