package lindar.wellrested;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
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
import lindar.wellrested.util.DateDeserializer;
import lindar.wellrested.util.DateSerializer;
import lindar.wellrested.util.WellRestedUtil;
import lindar.wellrested.vo.ResponseVO;

/**
 *
 * @author iulian.dafinoiu
 */
@Slf4j
public class WellRestedRequest {

    private final URI uri;
    private final Credentials credentials;
    private final HttpHost proxy;

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
     * ****************** GET ******************************************************************
     */
    public ResponseVO get() {
        return get(new ArrayList<>(0));
    }

    public ResponseVO get(Map<String, String> headers) {
        return submitRequest(Request.Get(uri), null, buildHeaders(headers));
    }

    public ResponseVO get(List<Header> headers) {
        return submitRequest(Request.Get(uri), null, headers);
    }

    /**
     * ****************** POST ******************************************************************
     */
    /**
     * Convenient method to POST a json or xml directly (by setting the content type that you want) without having to
     * work with HttpEntities
     *
     * @param content
     * @param contentType
     * @return
     */
    public ResponseVO post(String content, ContentType contentType) {
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
    public ResponseVO post(String content, ContentType contentType, Map<String, String> headers) {
        HttpEntity httpEntity = new StringEntity(content, contentType);
        if (headers != null && !headers.isEmpty()) {
            return post(httpEntity, WellRestedUtil.createHttpHeadersFromMap(headers));
        }
        return post(httpEntity);
    }

    /**
     * Convenient method to POST a list of FORM name-value pairs without having to work with HttpEntities
     *
     * @param formParams
     * @return
     */
    public ResponseVO post(List<NameValuePair> formParams) {
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
    public ResponseVO post(Map<String, String> formParams, Map<String, String> headers) {
        HttpEntity httpEntity;
        try {
            List<NameValuePair> formParamsList = formParams.entrySet().stream().map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue())).collect(Collectors.toList());
            httpEntity = new UrlEncodedFormEntity(formParamsList);
        } catch (UnsupportedEncodingException ex) {
            log.error("post: ", ex);
            return new ResponseVO();
        }
        if (headers != null && !headers.isEmpty()) {
            return post(httpEntity, WellRestedUtil.createHttpHeadersFromMap(headers));
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
    public ResponseVO post(List<NameValuePair> formParams, Map<String, String> headers) {
        HttpEntity httpEntity;
        try {
            httpEntity = new UrlEncodedFormEntity(formParams);
        } catch (UnsupportedEncodingException ex) {
            log.error("post: ", ex);
            return new ResponseVO();
        }
        if (headers != null && !headers.isEmpty()) {
            return post(httpEntity, WellRestedUtil.createHttpHeadersFromMap(headers));
        }
        return post(httpEntity);
    }

    /**
     * Convenient method to post a json string directly without having to work with HttpEntities
     *
     * @param json
     * @return
     */
    public ResponseVO post(String json) {
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
    public ResponseVO post(String json, Map<String, String> headers) {
        HttpEntity httpEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        if (headers != null && !headers.isEmpty()) {
            return post(httpEntity, WellRestedUtil.createHttpHeadersFromMap(headers));
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
    public <T> ResponseVO post(T object) {
        return post(object, null);
    }

    /**
     * Convenient method to POST an object directly (that will be converted into json) and a map of headers (key-value)
     * without having to work with HttpEntities or Http Headers
     *
     * @param <T> object to be converted into JSON and posted
     * @param headers
     * @return
     */
    public <T> ResponseVO post(T object, Map<String, String> headers) {
        GsonBuilder gsonBuilder = new GsonBuilder();
//        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        gsonBuilder.registerTypeAdapter(Date.class, new DateDeserializer());
        gsonBuilder.registerTypeAdapter(Date.class, new DateSerializer());
        Gson gson = gsonBuilder.create();
        ContentType contentType = ContentType.APPLICATION_JSON;
        HttpEntity httpEntity = new StringEntity(gson.toJson(object), contentType);
        if (headers != null && !headers.isEmpty()) {
            return post(httpEntity, WellRestedUtil.createHttpHeadersFromMap(headers));
        }
        return post(httpEntity);
    }

    /**
     * Standard method to POST an HttpEntity
     *
     * @param entity
     * @return
     */
    public ResponseVO post(HttpEntity entity) {
        return post(entity, new ArrayList<>(0));
    }

    /**
     * Standard method to POST an HttpEntity and a list of headers
     *
     * @param httpEntity
     * @param headers
     * @return
     */
    public ResponseVO post(HttpEntity httpEntity, List<Header> headers) {
        return submitRequest(Request.Post(uri), httpEntity, headers);
    }

    /**
     * Standard method to make a POST request without a body
     *
     * @return
     */
    public ResponseVO post() {
        return submitRequest(Request.Post(uri), null, null);
    }

    /**
     * Standard method to make a POST request without a body. You can still send a Map of headers though
     *
     * @param headers
     * @return
     */
    public ResponseVO post(Map<String, String> headers) {
        return submitRequest(Request.Post(uri), null, WellRestedUtil.createHttpHeadersFromMap(headers));
    }

    /**
     * Standard method to POST a File. Uses ContentType.MULTIPART_FORM_DATA as default content type for posting files
     *
     * @param file
     * @param contentType
     * @return
     */
    public ResponseVO post(File file) {
        return post(file, ContentType.MULTIPART_FORM_DATA, null);
    }

    /**
     * Standard method to POST a File. Please specify the correct content type
     *
     * @param file
     * @param contentType
     * @return
     */
    public ResponseVO post(File file, ContentType contentType) {
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
    public ResponseVO post(File file, ContentType contentType, List<Header> headers) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", file, contentType, file.getName());
        HttpEntity multipart = builder.build();
        return submitRequest(Request.Post(uri), multipart, headers);
    }

    /**
     * ************************** PUT ***********************************
     */
    /**
     * Convenient method to PUT a json or xml directly (by setting the content type that you want) without having to
     * work with HttpEntities
     *
     * @param content
     * @param contentType
     * @return
     */
    public ResponseVO put(String content, ContentType contentType) {
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
    public ResponseVO put(String content, ContentType contentType, Map<String, String> headers) {
        HttpEntity httpEntity = new StringEntity(content, contentType);
        if (headers != null && !headers.isEmpty()) {
            return put(httpEntity, WellRestedUtil.createHttpHeadersFromMap(headers));
        }
        return put(httpEntity);
    }

    /**
     * Convenient method to PUT a list of FORM name-value pairs without having to work with HttpEntities
     *
     * @param formParams
     * @return
     */
    public ResponseVO put(List<NameValuePair> formParams) {
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
    public ResponseVO put(List<NameValuePair> formParams, Map<String, String> headers) {
        HttpEntity httpEntity;
        try {
            httpEntity = new UrlEncodedFormEntity(formParams);
        } catch (UnsupportedEncodingException ex) {
            log.error("put: ", ex);
            return new ResponseVO();
        }
        if (headers != null && !headers.isEmpty()) {
            return put(httpEntity, WellRestedUtil.createHttpHeadersFromMap(headers));
        }
        return put(httpEntity);
    }

    /**
     * Convenient method to PUT a json string directly without having to work with HttpEntities
     *
     * @param json
     * @return
     */
    public ResponseVO put(String json) {
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
    public ResponseVO put(String json, Map<String, String> headers) {
        HttpEntity httpEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        if (headers != null && !headers.isEmpty()) {
            return put(httpEntity, WellRestedUtil.createHttpHeadersFromMap(headers));
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
    public <T> ResponseVO put(T object) {
        return put(object, null);
    }

    /**
     * Convenient method to PUT an object directly (that will be converted into json) and a map of headers (key-value)
     * without having to work with HttpEntities or Http Headers
     *
     * @param <T> object to be converted into JSON and posted
     * @param headers
     * @return
     */
    public <T> ResponseVO put(T object, Map<String, String> headers) {
        GsonBuilder gsonBuilder = new GsonBuilder();
//        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        gsonBuilder.registerTypeAdapter(Date.class, new DateDeserializer());
        gsonBuilder.registerTypeAdapter(Date.class, new DateSerializer());
        Gson gson = gsonBuilder.create();
        ContentType contentType = ContentType.APPLICATION_JSON;
        HttpEntity httpEntity = new StringEntity(gson.toJson(object), contentType);
        if (headers != null && !headers.isEmpty()) {
            return put(httpEntity, WellRestedUtil.createHttpHeadersFromMap(headers));
        }
        return put(httpEntity);
    }

    /**
     * Standard method to PUT an HttpEntity
     *
     * @param entity
     * @return
     */
    public ResponseVO put(HttpEntity entity) {
        return put(entity, new ArrayList<>(0));
    }

    /**
     * Standard method to PUT an HttpEntity and a list of headers
     *
     * @param httpEntity
     * @param headers
     * @return
     */
    public ResponseVO put(HttpEntity httpEntity, List<Header> headers) {
        return submitRequest(Request.Put(uri), httpEntity, headers);
    }

    /**
     * ****************** DELETE ******************************************************************
     */
    public ResponseVO delete() {
        return delete(null);
    }

    public ResponseVO delete(List<Header> headers) {
        return submitRequest(Request.Delete(uri), null, headers);
    }

    /**
     * ****************** GENERAL ******************************************************************
     */
    public ResponseVO submitRequest(Request request, HttpEntity httpEntity, List<Header> headers) {
        try {

            if (httpEntity != null) {
                request.body(httpEntity);
            }

            if (headers != null && !headers.isEmpty()) {
                headers.forEach(header -> request.addHeader(header));
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
            return WellRestedUtil.buildResponseVO(httpResponse, uri.toString());
        } catch (IOException ex) {
            log.error("Error occured after executing the GET request: ", ex);
        }
        return WellRestedUtil.buildErrorResponseVO(uri.toString());
    }

    private List<Header> buildHeaders(Map<String, String> headerMap) {
        return headerMap.entrySet().stream().map(entry -> new BasicHeader(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

}
