package com.lindar.wellrested.vo;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import com.lindar.wellrested.util.DateDeserializer;
import com.lindar.wellrested.util.type.CollectionWrapperType;
import com.lindar.wellrested.util.type.ResultWrapperType;
import com.lindar.wellrested.xml.WellRestedXMLUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@ToString
@EqualsAndHashCode
@Slf4j
public class WellRestedResponse implements Serializable {

    private static final long serialVersionUID = 51255400364556607L;

    @Getter @Setter
    private String serverResponse = StringUtils.EMPTY;

    @Getter @Setter
    private int statusCode;

    @Getter @Setter
    private String currentURI;


    @Setter
    private List<String> dateFormats;

    public WellRestedResponse setDateFormat(String format) {
        this.dateFormats = Collections.singletonList(format);
        return this;
    }


    private XmlResponseMapper fromXml = new XmlResponseMapper();

    /**
     * Allows you to manage XML responses and map them to Java objects
     */
    public XmlResponseMapper fromXml() {
        return this.fromXml;
    }

    public class XmlResponseMapper {
        public <T> T castTo(Class<T> objClass) {
            return WellRestedXMLUtil.fromStringToObject(serverResponse, objClass);
        }
    }



    private JsonResponseMapper fromJson = new JsonResponseMapper();

    /**
     * Allows you to manage JSON responses and map them to Java objects
     */
    public JsonResponseMapper fromJson() {
        return this.fromJson;
    }

    public class JsonResponseMapper {
        private Class<?> deserializedObjClass;
        private JsonDeserializer<?> deserializer;

        public JsonResponseMapper setDateFormats(List<String> formats) {
            dateFormats = formats;
            return this;
        }

        public <K> JsonResponseMapper registerDeserializer(Class<K> deserializedObjClass, JsonDeserializer<K> deserializer) {
            this.deserializedObjClass = deserializedObjClass;
            this.deserializer = deserializer;
            return this;
        }

        /**
         * Maps a json string to a Java object. If no custom date formats are set, the default date format is: yyyy-MM-dd'T'HH:mm:ssz
         */
        public <T> T castTo(Class<T> objClass) {
            return gsonBuilder().create().fromJson(serverResponse, objClass);
        }

        public <T> List<T> castToList(TypeToken<List<T>> typeToken) {
            GsonBuilder gsonBuilder = gsonBuilder();
            if (deserializedObjClass != null && deserializer != null) {
                gsonBuilder.registerTypeHierarchyAdapter(deserializedObjClass, deserializer);
            }
            return gsonBuilder.create().fromJson(serverResponse, typeToken.getType());
        }
    }


    private ResultResponseMapper fromResult = new ResultResponseMapper();

    /**
     * NOTE: Use this method only if your json response is of type: {@link Result}.
     * Otherwise use {@link WellRestedResponse#fromJson()}
     */
    public ResultResponseMapper fromResult() {
        return this.fromResult;
    }

    public class ResultResponseMapper {
        @Setter
        private Type type;

        public ResultResponseMapper setDateFormats(List<String> formats) {
            dateFormats = formats;
            return this;
        }

        /**
         * This method allows you to set a {@link ParameterizedType} within another ParameterizedType for cases when
         * your Result object contains another object inside (the wrapper) with a collection of the inner type.
         * <br/> In this case the structure would be something like this: Result->T->List<U> .
         * If your collections is even deeper than this, you'll have to build your own ParameterizedType
         */
        public <T, U> ResultResponseMapper registerDeepCollectionType(Class<T> wrapper, Class<U> inner) {
            this.type = new ResultWrapperType(new CollectionWrapperType(wrapper, inner));
            return this;
        }

        public <T> Result<T> cast() {
            this.type = Result.class;
            return castTo((Type) null);
        }

        public <T> Result<T> castTo(TypeToken<Result<T>> typeToken) {
            return castTo(typeToken.getType());
        }

        public <T> Result<T> castTo(Type type) {
            try {
                if (type == null) {
                    type = this.type;
                }
                return gsonBuilder().create().fromJson(serverResponse, type);
            } catch (Exception ex) {
                log.info("Error casting response to Result | {}", ex);
            }
            return ResultFactory.failed("Error casting response to a Result object");
        }
    }

    private GsonBuilder gsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        DateDeserializer dateDeserializer;
        if (dateFormats != null && !dateFormats.isEmpty()) {
            dateDeserializer = new DateDeserializer(dateFormats);
        } else {
            dateDeserializer = new DateDeserializer();
        }
        gsonBuilder.registerTypeAdapter(Date.class, dateDeserializer);
        return gsonBuilder;
    }

    /**
     * This method verifies that the server response is not blank (to allow casting) and that the status code is lower
     * than 300; Please note that a response might still be valid even when a server response is blank but in that case
     * you don't need to cast anymore and you don't need to call this method. When in doubt please write your own
     * validation method.
     *
     * @return
     */
    public boolean isValid() {
        return StringUtils.isNotBlank(serverResponse) && statusCode < 300;
    }

    /**
     * This method verifies if the underlying server response can be cast to: {@link com.lindar.wellrested.vo.Result}
     */
    public boolean isResult() {
        if (StringUtils.isNotBlank(serverResponse)) {
            try {
                return fromJson.castTo(Result.class) != null;
            } catch (Exception e) {
                log.warn("isResult: exception occurred - {}", e);
            }
        }
        return false;
    }
}