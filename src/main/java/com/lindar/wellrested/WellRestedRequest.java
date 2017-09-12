package com.lindar.wellrested;

import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.lindar.wellrested.util.BasicExclusionStrategy;
import com.lindar.wellrested.util.DateDeserializer;
import com.lindar.wellrested.util.StringDateSerializer;
import com.lindar.wellrested.util.WellRestedUtil;
import com.lindar.wellrested.vo.WellRestedResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class WellRestedRequest {

    private final URI uri;
    private final Credentials credentials;
    private final HttpHost proxy;
    
    private JsonSerializer<Date> dateSerializer = new StringDateSerializer();
    private JsonDeserializer<Date> dateDeserializer = new DateDeserializer();
    private String dateFormat = StringUtils.EMPTY;

    private ExclusionStrategy exclusionStrategy;
    private List<String> excludedFieldNames;
    private Set<String> excludedClassNames;

    private WellRestedRequest(URI uri) {
        this.uri = uri;
        this.credentials = null;
        this.proxy = null;
    }

    private WellRestedRequest(URI url, Credentials credentials) {
        this.uri = url;
        this.credentials = credentials;
        this.proxy = null;
    }

    private WellRestedRequest(URI url, Credentials credentials, HttpHost proxy) {
        this.uri = url;
        this.credentials = credentials;
        this.proxy = proxy;
    }
    
    public static WellRestedRequest build(final String url) {
        return new WellRestedRequest(URI.create(url));
    }

    public static WellRestedRequest build(final URI uri) {
        return new WellRestedRequest(uri);
    }
    
    public static WellRestedRequest build(final String url, final String username, final String password) {
        Credentials creds = new UsernamePasswordCredentials(username, password);
        return new WellRestedRequest(URI.create(url), creds);
    }

    public static WellRestedRequest build(final URI uri, final String username, final String password) {
        Credentials creds = new UsernamePasswordCredentials(username, password);
        return new WellRestedRequest(uri, creds);
    }
    
    public static WellRestedRequest build(final String url, final Credentials credentials) {
        return new WellRestedRequest(URI.create(url), credentials);
    }

    public static WellRestedRequest build(final URI uri, final Credentials credentials) {
        return new WellRestedRequest(uri, credentials);
    }

    public static WellRestedRequest buildWithProxy(final URI uri, HttpHost proxy) {
        return new WellRestedRequest(uri);
    }
    
    /**
     * Helper method for building a WellRestedRequest object with Proxy. Please provide the proxy host, port and scheme
     * (http or https).
     *
     * @param uri
     * @param proxyHost
     * @param proxyPort
     * @param scheme
     * @return
     */
    public static WellRestedRequest buildWithProxy(final URI uri, String proxyHost, int proxyPort, String scheme) {
        HttpHost proxy = new HttpHost(proxyHost, proxyPort, scheme);
        return new WellRestedRequest(uri, null, proxy);
    }

    /**
     * Helper method for building a WellRestedRequest object with Proxy and Credentials. Please provide the proxy host,
     * port and scheme (http or https).
     *
     * @param uri
     * @param username
     * @param password
     * @param proxyHost
     * @param proxyPort
     * @param scheme
     * @return
     */
    public static WellRestedRequest buildWithProxy(final URI uri, final String username, final String password, String proxyHost, int proxyPort, String scheme) {
        Credentials creds = new UsernamePasswordCredentials(username, password);
        HttpHost proxy = new HttpHost(proxyHost, proxyPort, scheme);
        return new WellRestedRequest(uri, creds, proxy);
    }

    public static WellRestedRequest buildWithProxy(final URI uri, final Credentials credentials, final HttpHost proxy) {
        return new WellRestedRequest(uri, credentials, proxy);
    }
    
    
    /**
     * Use this method to override the default dateSerializer. 
     * The default one is {@link com.lindar.wellrested.util.StringDateSerializer}
     * <br/>
     * NOTE: Well Rested Client provides 2 serializers which can be passed as parameters for this method: <br/>
     * - {@link com.lindar.wellrested.util.StringDateSerializer} <br/>
     * - {@link com.lindar.wellrested.util.LongDateSerializer} <br/>
     * If neither satisfies your requirements, please write your own.
     * @param dateSerializer
     * @return
     */
    public WellRestedRequest setDateSerializer(JsonSerializer<Date> dateSerializer) {
        this.dateSerializer = dateSerializer;
        return this;
    }
    
    /**
     * Use this method to override the default dateSerializer. 
     * The default one is {@link com.lindar.wellrested.util.DateDeserializer}
     * <br/>
     * If the default one doesn't satisfy your requirements, please write your own.
     * @param dateDeserializer
     * @return
     */
    public WellRestedRequest setDateDeserializer(JsonDeserializer<Date> dateDeserializer) {
        this.dateDeserializer = dateDeserializer;
        return this;
    }
    
    /**
     * Use this method to provide a date format that will be used when doing both the serialization and deserialization. <br/>
     * If you require different formats for serialization and deserialization, please use the <b>setDateSerializer</b> and <b>setDateDeserializer</b> methods. <br/>
     * By default this class uses {@link com.lindar.wellrested.util.StringDateSerializer} and {@link com.lindar.wellrested.util.DateDeserializer} <br/>
     * <b>PLEASE NOTE:</b> By setting a dateFormat, you <b>override</b> any other serializer and deserializer!
     * @param dateFormat
     * @return
     */
    public WellRestedRequest setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }

    /**
     * Use this method to provide a Gson serialisation exclusion strategy.
     * If you just want to exlude some field names or a class you can use the excludeFields and excludeClasses methods easier.
     * Keep in mind that this exclusion strategy overrides the other ones
     * @param exclusionStrategy
     * @return
     */
    public WellRestedRequest setExclusionStrategy(ExclusionStrategy exclusionStrategy) {
        this.exclusionStrategy = exclusionStrategy;
        return this;
    }

    /**
     * Use this method to exclude some field names from the Gson serialisation process
     * @param fieldNames
     * @return
     */
    public WellRestedRequest excludeFields(List<String> fieldNames) {
        this.excludedFieldNames = fieldNames;
        return this;
    }

    /**
     * Use this method to exclude some class names from the Gson serialisation process
     * @param classNames
     * @return
     */
    public WellRestedRequest excludeClasses(Set<String> classNames) {
        this.excludedClassNames = classNames;
        return this;
    }
    

    /**
     * ****************** GET ******************************************************************
     */
    public WellRestedResponse get() {
        return get(new ArrayList<>(0));
    }

    public WellRestedResponse get(Map<String, String> headers) {
        return submitRequest(Request.Get(uri), null, buildHeaders(headers));
    }

    public WellRestedResponse get(List<Header> headers) {
        return submitRequest(Request.Get(uri), null, headers);
    }

    /**
     * ****************** POST ******************************************************************
     */

    /**
     * Standard method to make a POST request without a body
     *
     * @return
     */
    public WellRestedResponse post() {
        return submitRequest(Request.Post(uri), null, null);
    }


    /**
     * Convenient method to POST a json or xml directly (by setting the content type that you want) without having to
     * work with HttpEntities
     *
     * @param content
     * @param contentType
     * @return
     */
    public WellRestedResponse post(String content, ContentType contentType) {
        return post(content, contentType, null);
    }

    /**
     * Convenient method to POST a json or xml directly (by setting the content type that you want) directly and a map
     * of headers (key-value) without having to work with HttpEntities or Http Headers
     *
     * @param content
     * @param contentType
     * @param headers
     * @return
     */
    public WellRestedResponse post(String content, ContentType contentType, Map<String, String> headers) {
        HttpEntity httpEntity = new StringEntity(content, contentType);
        if (headers != null && !headers.isEmpty()) {
            return post(httpEntity, buildHeaders(headers));
        }
        return post(httpEntity);
    }

    /**
     * Convenient method to POST a list of FORM name-value pairs without having to work with HttpEntities
     *
     * @param formParams
     * @return
     */
    public WellRestedResponse post(List<NameValuePair> formParams) {
        return post(formParams, new HashMap<>(0));
    }

    /**
     * Convenient method to POST a map of FORM name-value pairs without having to work with HttpEntities and Http
     * Headers
     *
     * @param formParams
     * @param headers
     * @return
     */
    public WellRestedResponse post(Map<String, String> formParams, Map<String, String> headers) {
        HttpEntity httpEntity;
        try {
            List<NameValuePair> formParamsList = formParams.entrySet().stream().map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue())).collect(Collectors.toList());
            httpEntity = new UrlEncodedFormEntity(formParamsList);
        } catch (UnsupportedEncodingException ex) {
            log.error("post: ", ex);
            return new WellRestedResponse();
        }
        if (headers != null && !headers.isEmpty()) {
            return post(httpEntity, buildHeaders(headers));
        }
        return post(httpEntity);
    }

    /**
     * Convenient method to POST a list of FORM name-value pairs without having to work with HttpEntities and Http
     * Headers
     *
     * @param formParams
     * @param headers
     * @return
     */
    public WellRestedResponse post(List<NameValuePair> formParams, Map<String, String> headers) {
        HttpEntity httpEntity;
        try {
            httpEntity = new UrlEncodedFormEntity(formParams);
        } catch (UnsupportedEncodingException ex) {
            log.error("post: ", ex);
            return new WellRestedResponse();
        }
        if (headers != null && !headers.isEmpty()) {
            return post(httpEntity, buildHeaders(headers));
        }
        return post(httpEntity);
    }

    /**
     * Convenient method to post a json string directly without having to work with HttpEntities
     *
     * @param json
     * @return
     */
    public WellRestedResponse post(String json) {
        return post(json, new HashMap<>(0));
    }

    /**
     * Convenient method to POST a json string directly and a map of headers (key-value) without having to work with
     * HttpEntities or Http Headers
     *
     * @param json
     * @param headers
     * @return
     */
    public WellRestedResponse post(String json, Map<String, String> headers) {
        HttpEntity httpEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        if (headers != null && !headers.isEmpty()) {
            return post(httpEntity, buildHeaders(headers));
        }
        return post(httpEntity);
    }

    /**
     * Convenient method to POST an object directly that will be converted into json without having to work with
     * HttpEntities
     *
     * @param <T>
     * @param object
     * @return
     */
    public <T> WellRestedResponse post(T object) {
        return post(object, null);
    }

    /**
     * Convenient method to POST an object directly (that will be converted into json) and a map of headers (key-value)
     * without having to work with HttpEntities or Http Headers
     *
     * @param <T> object to be converted into JSON and posted
     * @param object
     * @param headers
     * @return
     */
    public <T> WellRestedResponse post(T object, Map<String, String> headers) {
        Gson gson = buildGson();
        ContentType contentType = ContentType.APPLICATION_JSON;
        HttpEntity httpEntity = new StringEntity(gson.toJson(object), contentType);
        if (headers != null && !headers.isEmpty()) {
            return post(httpEntity, buildHeaders(headers));
        }
        return post(httpEntity);
    }

    /**
     * Standard method to POST an HttpEntity
     *
     * @param entity
     * @return
     */
    public WellRestedResponse post(HttpEntity entity) {
        return post(entity, new ArrayList<>(0));
    }

    /**
     * Standard method to POST an HttpEntity and a list of headers
     *
     * @param httpEntity
     * @param headers
     * @return
     */
    public WellRestedResponse post(HttpEntity httpEntity, List<Header> headers) {
        return submitRequest(Request.Post(uri), httpEntity, headers);
    }


    /**
     * Standard method to make a POST request without a body. You can still send a Map of headers though
     *
     * @param headers
     * @return
     */
    public WellRestedResponse post(Map<String, String> headers) {
        return submitRequest(Request.Post(uri), null, buildHeaders(headers));
    }

    /**
     * Standard method to POST a File. Uses ContentType.MULTIPART_FORM_DATA as default content type for posting files
     *
     * @param file
     * @return
     */
    public WellRestedResponse post(File file) {
        return post(file, ContentType.MULTIPART_FORM_DATA, null);
    }

    /**
     * Standard method to POST a File. Please specify the correct content type
     *
     * @param file
     * @param contentType
     * @return
     */
    public WellRestedResponse post(File file, ContentType contentType) {
        return post(file, contentType, null);
    }

    /**
     * Standard method to POST a File. Please specify the correct content type
     *
     * @param file
     * @param contentType
     * @param headers
     * @return
     */
    public WellRestedResponse post(File file, ContentType contentType, List<Header> headers) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", file, contentType, file.getName());
        HttpEntity multipart = builder.build();
        return submitRequest(Request.Post(uri), multipart, headers);
    }


    /**
     * ************************** PUT ***********************************
     */


    /**
     * Standard method to make a POST request without a body
     *
     * @return
     */
    public WellRestedResponse put() {
        return submitRequest(Request.Put(uri), null, null);
    }

    /**
     * Convenient method to PUT a json or xml directly (by setting the content type that you want) without having to
     * work with HttpEntities
     *
     * @param content
     * @param contentType
     * @return
     */
    public WellRestedResponse put(String content, ContentType contentType) {
        return put(content, contentType, null);
    }

    /**
     * Convenient method to PUT a json or xml directly (by setting the content type that you want) directly and a map of
     * headers (key-value) without having to work with HttpEntities or Http Headers
     *
     * @param content
     * @param contentType
     * @param headers
     * @return
     */
    public WellRestedResponse put(String content, ContentType contentType, Map<String, String> headers) {
        HttpEntity httpEntity = new StringEntity(content, contentType);
        if (headers != null && !headers.isEmpty()) {
            return put(httpEntity, buildHeaders(headers));
        }
        return put(httpEntity);
    }

    /**
     * Convenient method to PUT a list of FORM name-value pairs without having to work with HttpEntities
     *
     * @param formParams
     * @return
     */
    public WellRestedResponse put(List<NameValuePair> formParams) {
        return put(formParams, new HashMap<>(0));
    }

    /**
     * Convenient method to PUT a list of FORM name-value pairs without having to work with HttpEntities and Http
     * Headers
     *
     * @param formParams
     * @param headers
     * @return
     */
    public WellRestedResponse put(List<NameValuePair> formParams, Map<String, String> headers) {
        HttpEntity httpEntity;
        try {
            httpEntity = new UrlEncodedFormEntity(formParams);
        } catch (UnsupportedEncodingException ex) {
            log.error("put: ", ex);
            return new WellRestedResponse();
        }
        if (headers != null && !headers.isEmpty()) {
            return put(httpEntity, buildHeaders(headers));
        }
        return put(httpEntity);
    }

    /**
     * Convenient method to PUT a json string directly without having to work with HttpEntities
     *
     * @param json
     * @return
     */
    public WellRestedResponse put(String json) {
        return put(json, new HashMap<>(0));
    }

    /**
     * Convenient method to PUT a json string directly and a map of headers (key-value) without having to work with
     * HttpEntities or Http Headers
     *
     * @param json
     * @param headers
     * @return
     */
    public WellRestedResponse put(String json, Map<String, String> headers) {
        HttpEntity httpEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        if (headers != null && !headers.isEmpty()) {
            return put(httpEntity, buildHeaders(headers));
        }
        return put(httpEntity);
    }

    /**
     * Convenient method to PUT an object directly that will be converted into json without having to work with
     * HttpEntities
     *
     * @param <T>
     * @param object
     * @return
     */
    public <T> WellRestedResponse put(T object) {
        return put(object, null);
    }

    /**
     * Convenient method to PUT an object directly (that will be converted into json) and a map of headers (key-value)
     * without having to work with HttpEntities or Http Headers
     *
     * @param <T> object to be converted into JSON and posted
     * @param object
     * @param headers
     * @return
     */
    public <T> WellRestedResponse put(T object, Map<String, String> headers) {
        Gson gson = buildGson();
        ContentType contentType = ContentType.APPLICATION_JSON;
        HttpEntity httpEntity = new StringEntity(gson.toJson(object), contentType);
        if (headers != null && !headers.isEmpty()) {
            return put(httpEntity, buildHeaders(headers));
        }
        return put(httpEntity);
    }

    /**
     * Standard method to PUT an HttpEntity
     *
     * @param entity
     * @return
     */
    public WellRestedResponse put(HttpEntity entity) {
        return put(entity, new ArrayList<>(0));
    }

    /**
     * Standard method to PUT an HttpEntity and a list of headers
     *
     * @param httpEntity
     * @param headers
     * @return
     */
    public WellRestedResponse put(HttpEntity httpEntity, List<Header> headers) {
        return submitRequest(Request.Put(uri), httpEntity, headers);
    }

    /**
     * ****************** DELETE ******************************************************************
     */
    public WellRestedResponse delete() {
        return delete((List<Header>) null);
    }

    public WellRestedResponse delete(List<Header> headers) {
        return submitRequest(Request.Delete(uri), null, headers);
    }


    public WellRestedResponse delete(Map<String, String> headers) {
        return submitRequest(Request.Delete(uri), null, buildHeaders(headers));
    }

    /**
     * Standard method to DELETE an HttpEntity
     *
     * @param entity
     * @return
     */
    public WellRestedResponse delete(HttpEntity entity) {
        return delete(entity, (List<Header>) null);
    }

    public WellRestedResponse delete(HttpEntity httpEntity, List<Header> headers) {
        return submitRequest(Request.Delete(uri), httpEntity, headers);
    }

    /**
     * Convenient method to PUT an object directly (that will be converted into json) without having to work with HttpEntities
     *
     * @param <T> object to be converted into JSON and posted
     * @return
     */
    public <T> WellRestedResponse delete(T object) {
        return delete(object, null);
    }

    /**
     * Convenient method to PUT an object directly (that will be converted into json) and a map of headers (key-value)
     * without having to work with HttpEntities or Http Headers
     *
     * @param <T> object to be converted into JSON and posted
     * @param headers
     * @return
     */
    public <T> WellRestedResponse delete(T object, Map<String, String> headers) {
        Gson gson = buildGson();
        ContentType contentType = ContentType.APPLICATION_JSON;
        HttpEntity httpEntity = new StringEntity(gson.toJson(object), contentType);
        return submitRequest(Request.Delete(uri), httpEntity, buildHeaders(headers));
    }

    /**
     * ****************** GENERAL ******************************************************************
     */
    public WellRestedResponse submitRequest(Request request, HttpEntity httpEntity, List<Header> headers) {
        try {

            if (httpEntity != null) {
                request.body(httpEntity);
            }

            if (headers != null && !headers.isEmpty()) {
                headers.forEach(request::addHeader);
            }

            if (this.proxy != null) {
                request.viaProxy(proxy);
            }

            HttpResponse httpResponse;
            if (credentials != null) {
                Executor executor = Executor.newInstance().auth(credentials);
                httpResponse = executor.execute(request).returnResponse();
            } else {
                httpResponse = request.execute().returnResponse();
            }
            return WellRestedUtil.buildWellRestedResponse(httpResponse, uri.toString());
        } catch (IOException ex) {
            log.error("Error occurred after executing the GET request: ", ex);
        }
        return WellRestedUtil.buildErrorWellRestedResponse(uri.toString());
    }

    private List<Header> buildHeaders(Map<String, String> headerMap) {
        if (headerMap == null) {
            return new ArrayList<>();
        }
        return headerMap.entrySet().stream()
                .map(entry -> new BasicHeader(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
    
    private Gson buildGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
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

        return gsonBuilder.create();
    }

}
