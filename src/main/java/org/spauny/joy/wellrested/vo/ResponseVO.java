package org.spauny.joy.wellrested.vo;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.spauny.joy.wellrested.util.DateDeserializer;
import org.spauny.joy.wellrested.xml.WellRestedXMLUtil;

@Data
public class ResponseVO implements Serializable {

    private static final long serialVersionUID = 51255400364556607L;

    private String serverResponse = StringUtils.EMPTY;
    private int statusCode;
    private String currentURI;

    public <T> T castXmlResponse(Class<T> objClass) {
        return WellRestedXMLUtil.fromStringToObject(serverResponse, objClass);
    }

    /**
     * * Use this method to deserialize a json string into an object. This method uses the default date format:
     * yyyy-MM-dd'T'HH:mm:ssz
     *
     * @param <T>
     * @param objClass
     * @return
     */
    public <T> T castJsonResponse(Class<T> objClass) {
        return castJsonResponse(objClass, null);
    }

    /**
     * Use this method to deserialize a json string into an object containing date fields using the provided date
     * format.
     *
     * @param objClass
     * @param dateFormats
     * @return
     */
    public <T> T castJsonResponse(Class<T> objClass, List<String> dateFormats) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        DateDeserializer dateDeserializer;
        if (dateFormats != null && !dateFormats.isEmpty()) {
            dateDeserializer = new DateDeserializer(dateFormats);
        } else {
            dateDeserializer = new DateDeserializer();
        }
        gsonBuilder.registerTypeAdapter(Date.class, dateDeserializer);
        Gson gson = gsonBuilder.create();
        return gson.fromJson(this.serverResponse, objClass);
    }
    
    public <T> Result<T> castJsonResponseToResult(TypeToken<Result<T>> typeToken) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        DateDeserializer dateDeserializer = new DateDeserializer();
        gsonBuilder.registerTypeAdapter(Date.class, dateDeserializer);
        Gson gson = gsonBuilder.create();
        return gson.fromJson(this.serverResponse, typeToken.getType());
    }

    public <T> List<T> castJsonResponseToList(TypeToken<List<T>> typeToken) {
        return castJsonResponseToList(null, null, typeToken, null);
    }

    public <T, K> List<T> castJsonResponseToList(Class<K> deserialisedObjClas, JsonDeserializer<K> deserializer, TypeToken<List<T>> typeToken) {
        return castJsonResponseToList(deserialisedObjClas, deserializer, typeToken, null);
    }

    public <T, K> List<T> castJsonResponseToList(Class<K> deserialisedObjClas, JsonDeserializer<K> deserializer, TypeToken<List<T>> typeToken, List<String> dateFormats) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        DateDeserializer dateDeserializer;
        if (dateFormats != null && !dateFormats.isEmpty()) {
            dateDeserializer = new DateDeserializer(dateFormats);
        } else {
            dateDeserializer = new DateDeserializer();
        }
        gsonBuilder.registerTypeAdapter(Date.class, dateDeserializer);
        if (deserialisedObjClas != null && deserializer != null) {
            gsonBuilder.registerTypeHierarchyAdapter(deserialisedObjClas, deserializer);
        }
        Gson gson = gsonBuilder.create();
        return gson.fromJson(this.serverResponse, typeToken.getType());
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
     * This method verifies if the underlying server response can be cast to the Result class and then verifies if the success flag is set to true
     *
     * @return
     */
    public boolean isResultValid() {
        if (StringUtils.isNotBlank(serverResponse)) {
            Result<?> result = castJsonResponse(Result.class);
            if (result != null && result.isSuccess()) {
                return true;
            }
        }
        return false;
    }
}
