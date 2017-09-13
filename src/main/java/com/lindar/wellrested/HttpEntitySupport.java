package com.lindar.wellrested;

import com.lindar.wellrested.xml.WellRestedXMLUtil;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

interface HttpEntitySupport {

    <T extends RequestResource> T httpEntity(HttpEntity httpEntity);

    default <T extends RequestResource> T jsonContent(String json) {
        return httpEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
    }

    default <T extends RequestResource> T xmlContent(String xml) {
        return httpEntity(new StringEntity(xml, ContentType.APPLICATION_XML));
    }

    /** Serialise a java object into XML String and add it to the request body.
     * NOTE: keep in mind that date serializers and deserializers and all exclusion strategies are available only for JSON content  */
    default <T extends RequestResource, U> T xmlContent(U object) {
        return xmlContent(WellRestedXMLUtil.fromObjectToString(object));
    }

    default <T extends RequestResource> T content(String content, ContentType contentType) {
        return httpEntity(new StringEntity(content, contentType));
    }

    default <T extends RequestResource> T formParams(List<NameValuePair> formParams) {
        HttpEntity httpEntity = null;
        try {
            httpEntity = new UrlEncodedFormEntity(formParams);
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return httpEntity(httpEntity);
    }

    default <T extends RequestResource> T formParams(Map<String, String> formParams) {
        return formParams(formParams.entrySet().stream()
                .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList()));
    }

    /** Add a file to the request body. Default Content type is MULTIPART_FORM_DATA */
    default <T extends RequestResource> T file(File file) {
        return file(file, ContentType.MULTIPART_FORM_DATA);
    }

    default <T extends RequestResource> T file(File file, ContentType contentType) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", file, contentType, file.getName());
        return httpEntity(builder.build());
    }
}
