package com.lindar.wellrested;

import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.File;
import java.nio.charset.Charset;
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

    default <T extends RequestResource> T content(String content, ContentType contentType) {
        return httpEntity(new StringEntity(content, contentType));
    }

    default <T extends RequestResource> T formParams(List<NameValuePair> formParams, Charset charset) {
        HttpEntity httpEntity;
        httpEntity = new UrlEncodedFormEntity(formParams, charset);
        return httpEntity(httpEntity);
    }

    default <T extends RequestResource> T formParams(List<NameValuePair> formParams) {
        return formParams(formParams, Charset.forName("UTF-8"));
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
