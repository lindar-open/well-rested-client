package com.lindar.wellrested.vo;

import com.google.gson.reflect.TypeToken;
import com.lindar.wellrested.json.JsonMapper;
import com.lindar.wellrested.util.type.CollectionWrapperType;
import com.lindar.wellrested.util.type.ResultWrapperType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@ToString
@EqualsAndHashCode
@Slf4j
public class WellRestedResponse implements Serializable {
    private static final long serialVersionUID = 51255400364556607L;

    private @Getter @Setter String              serverResponse = StringUtils.EMPTY;
    private @Getter @Setter int                 statusCode;
    private @Getter @Setter String              currentURI;
    private @Setter         List<String>        dateFormats;
    private @Getter @Setter Map<String, String> responseHeaders;
    private @Getter @Setter boolean             socketTimeout;
    private @Getter @Setter boolean             connectionTimeout;
    private @Getter @Setter boolean             connectionRequestTimeout;
    private final           JsonMapper          jsonMapper;

    public WellRestedResponse(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    private JsonResponseMapper fromJson = new JsonResponseMapper();

    /**
     * Allows you to manage JSON responses and map them to Java objects
     */
    public JsonResponseMapper fromJson() {
        return this.fromJson;
    }

    public class JsonResponseMapper {
        public <T> T castTo(Class<T> objClass) {
            return castTo((Type) objClass);
        }

        public <T> T castTo(TypeToken<T> typeToken) {
            return castTo(typeToken.getType());
        }

        public <T> T castTo(Type type) {
            return jsonMapper.readValue(serverResponse, type);
        }

        public <T> List<T> castToList(Class<T> objClass) {
            Type typeOfT = TypeToken.getParameterized(List.class, objClass).getType();
            return jsonMapper.readValue(serverResponse, typeOfT);
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
                return jsonMapper.readValue(serverResponse, type);
            } catch (Exception ex) {
                log.info("Error casting response to Result | {}", ex);
            }
            return ResultBuilder.failedCastingResult();
        }
    }

    /**
     * This method verifies that the server response is not blank (to allow casting) and that the status code is lower
     * than 300 but not -1 (which would imply a client timeout); Please note that a response might still be valid even when a server response is blank but in that case
     * you don't need to cast anymore and you don't need to call this method. When in doubt please write your own
     * validation method.
     */
    public boolean isValid() {
        return StringUtils.isNotBlank(serverResponse) && statusCode < 300 && statusCode != -1;
    }

    /**
     * This method verifies if the response was a client timeout
     * (the connection was closed by the client either because it took too long to connect or it took too long to get the data)
     * <br/>
     * You can use the {@code isSocketTimeout} or {@code isConnectionTimeout} flags to differentiate between the 2 types of timeout
     */
    public boolean isClientTimeout() {
        return statusCode == -1 && (socketTimeout || connectionTimeout);
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
