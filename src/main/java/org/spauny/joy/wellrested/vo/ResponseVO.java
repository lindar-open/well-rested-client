package org.spauny.joy.wellrested.vo;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
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
    
    public <T> T castJsonResponse(Class<T> objClass) {
        Gson gson = new Gson();
        return gson.fromJson(this.serverResponse, objClass);
    }
    
    public <T> List<T> castJsonResponseToList(TypeToken<List<T>> typeToken) {
        Gson gson = new Gson();
        return gson.fromJson(this.serverResponse, typeToken.getType());
    }
    
    public <T, K> List<T> castJsonResponseToList(Class<K> deserialisedObjClas, JsonDeserializer<K> deserializer, TypeToken<List<T>> typeToken) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(deserialisedObjClas, deserializer);
        Gson gson = gsonBuilder.create();
        return gson.fromJson(this.serverResponse, typeToken.getType());
    }
}
