package com.lindar.wellrested.json;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.lindar.wellrested.GsonCustomiser;
import com.lindar.wellrested.util.BasicExclusionStrategy;
import com.lindar.wellrested.util.DateDeserializer;
import com.lindar.wellrested.util.StringDateSerializer;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class GsonJsonMapper implements JsonMapper {
    private final Gson gson;

    public GsonJsonMapper() {
        this(new Gson());
    }

    public GsonJsonMapper(Gson gson) {
        this.gson = gson;
    }

    @Override public <T> T readValue(String value, Type valueType) {
        return gson.fromJson(value, valueType);
    }

    @Override
    public String writeValue(Object value) {
        return gson.toJson(value);
    }


    public static class Builder {
        private JsonSerializer<Date>   dateSerializer   = new StringDateSerializer();
        private JsonDeserializer<Date> dateDeserializer = new DateDeserializer();
        private String                 dateFormat;
        private ExclusionStrategy      exclusionStrategy;
        private List<String>           excludedFieldNames;
        private Set<String>            excludedClassNames;
        private GsonCustomiser         gsonCustomiser;

        public Builder gsonCustomiser(GsonCustomiser gsonCustomiser) {
            this.gsonCustomiser = gsonCustomiser;
            return this;
        }

        /**
         * Use this method to override the default dateSerializer.
         * The default one is {@link com.lindar.wellrested.util.StringDateSerializer}
         * <br/>
         * NOTE: Well Rested Client provides 2 serializers which can be passed as parameters for this method: <br/>
         * - {@link com.lindar.wellrested.util.StringDateSerializer} <br/>
         * - {@link com.lindar.wellrested.util.LongDateSerializer} <br/>
         * If neither satisfies your requirements, please write your own.
         */
        public Builder dateSerializer(JsonSerializer<Date> dateSerializer) {
            this.dateSerializer = dateSerializer;
            return this;
        }

        /**
         * Use this method to override the default dateSerializer.
         * The default one is {@link com.lindar.wellrested.util.DateDeserializer}
         * <br/>
         * If the default one doesn't satisfy your requirements, please write your own.
         */
        public Builder dateDeserializer(JsonDeserializer<Date> dateDeserializer) {
            this.dateDeserializer = dateDeserializer;
            return this;
        }

        /**
         * Use this method to provide a date format that will be used when doing both the serialization and deserialization. <br/>
         * If you require different formats for serialization and deserialization, please use the <b>setDateSerializer</b> and <b>setDateDeserializer</b> methods. <br/>
         * By default this class uses {@link com.lindar.wellrested.util.StringDateSerializer} and {@link com.lindar.wellrested.util.DateDeserializer} <br/>
         * <b>PLEASE NOTE:</b> By setting a dateFormat, you <b>override</b> any other serializer and deserializer!
         */
        public Builder dateFormat(String dateFormat) {
            this.dateFormat = dateFormat;
            return this;
        }

        /**
         * Use this method to provide a Gson serialisation exclusion strategy.
         * If you just want to exclude some field names or a class you can use the excludeFields and excludeClasses methods easier.
         * Keep in mind that this exclusion strategy overrides the other ones
         */
        public Builder exclusionStrategy(ExclusionStrategy exclusionStrategy) {
            this.exclusionStrategy = exclusionStrategy;
            return this;
        }

        /**
         * Use this method to exclude some field names from the Gson serialisation process
         */
        public Builder excludeFields(List<String> fieldNames) {
            this.excludedFieldNames = fieldNames;
            return this;
        }

        /**
         * Use this method to exclude some class names from the Gson serialisation process
         */
        public Builder excludeClasses(Set<String> classNames) {
            this.excludedClassNames = classNames;
            return this;
        }

        public GsonJsonMapper build() {
            GsonBuilder gsonBuilder = Converters.registerAll(new GsonBuilder());
            if (StringUtils.isBlank(this.dateFormat)) {
                gsonBuilder.registerTypeAdapter(Date.class, this.dateSerializer);
                gsonBuilder.registerTypeAdapter(Date.class, this.dateDeserializer);
            } else {
                gsonBuilder.setDateFormat(this.dateFormat);
            }

            if (this.exclusionStrategy != null) {
                gsonBuilder.setExclusionStrategies(exclusionStrategy);
            } else if (this.excludedFieldNames != null || this.excludedClassNames != null) {
                gsonBuilder.setExclusionStrategies(new BasicExclusionStrategy(excludedClassNames, excludedFieldNames));
            }

            if (this.gsonCustomiser != null) {
                this.gsonCustomiser.customise(gsonBuilder);
            }
            return new GsonJsonMapper(gsonBuilder.create());
        }
    }
}
