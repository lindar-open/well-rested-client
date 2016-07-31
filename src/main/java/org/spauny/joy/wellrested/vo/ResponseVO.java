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
     * *  Use this method to deserialize a json string into an object. 
     * This method uses the default date format: yyyy-MM-dd'T'HH:mm:ssz
     * @param <T>
     * @param objClass
     * @return
     */
    public <T> T castJsonResponse(Class<T> objClass) {
        return castJsonResponse(objClass, null);
    }

    /**
     *  Use this method to deserialize a json string into an object containing date fields using the provided date format.
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
}
