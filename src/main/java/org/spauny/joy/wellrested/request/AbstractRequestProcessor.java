package org.spauny.joy.wellrested.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.Credentials;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.spauny.joy.wellrested.vo.ResponseVO;

/**
 *
 * @author iulian.dafinoiu
 */
@Slf4j
public abstract class AbstractRequestProcessor {
    protected static final String USER_AGENT = "Mozilla/5.0";

    protected final String url;
    protected final boolean proxyEnabled;
    protected final ProxyValidator proxyValidator;
    protected final String proxyHost;
    protected final int proxyPort;

    protected AbstractRequestProcessor(String url) {
        validateUrl(url);
        this.url = url;
        this.proxyEnabled = false;
        this.proxyValidator = null;
        this.proxyHost = StringUtils.EMPTY;
        this.proxyPort = 0;
    }

    protected AbstractRequestProcessor(String url, String proxyHost, int proxyPort) {
        validateUrl(url);
        this.url = url;
        this.proxyEnabled = validateProxy(proxyHost, proxyPort);
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyValidator = null;
    }

    protected AbstractRequestProcessor(String url, String proxyHost, int proxyPort, ProxyValidator conditionalProxyValidator) {
        validateUrl(url);
        this.url = url;
        this.proxyEnabled = validateProxy(proxyHost, proxyPort);
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyValidator = conditionalProxyValidator;
    }

    private void validateUrl(String url) {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("Url must not be blank!");
        }
    }

    private boolean validateProxy(String host, int port) {
        if (StringUtils.isBlank(host) || port < 1) {
            throw new IllegalArgumentException("Please provide a valid host and port");
        } else {
            return true;
        }
    }
    
    public abstract ResponseVO processGetRequest();
    public abstract ResponseVO processGetRequest(List<Header> headers);
    public abstract ResponseVO processGetRequestWithCredentials(List<Header> headers, Credentials credentials);
    
    public abstract ResponseVO processPostRequest(HttpEntity entity);
    public abstract ResponseVO processPostRequest(HttpEntity entity, List<Header> headers);
    
    /**
     * Convenient method to post a json string directly without having to work with HttpEntities 
     * @param json
     * @return
     */
    public abstract ResponseVO processPostRequest(String json);
    
    /**
     * Convenient method to post a json string directly and a map of headers (key-value) without having to work with HttpEntities or Http Headers
     * 
     * @param json
     * @param headers
     * @return
     */
    public abstract ResponseVO processPostRequest(String json, Map<String, String> headers);
    
    /**
     * Convenient method to post a json or xml directly (by setting the content type that you want) without having to work with HttpEntities 
     * @param content
     * @param contentType
     * @return
     */
    public abstract ResponseVO processPostRequest(String content, ContentType contentType);
    
    /**
     * Convenient method to post a json or xml directly (by setting the content type that you want) directly and a map of headers (key-value) without having to work with HttpEntities or Http Headers
     * 
     * @param content
     * @param contentType
     * @param headers
     * @return
     */
    public abstract ResponseVO processPostRequest(String content, ContentType contentType, Map<String, String> headers);
    
    /**
     * Convenient method to post an object directly that will be converted into json without having to work with HttpEntities
     * @param <T>
     * @param object
     * @return
     */
    public abstract <T> ResponseVO processPostRequest(T object);

    /**
     * Convenient method to post an object directly (that will be converted into json) and a map of headers (key-value) without having to work with HttpEntities or Http Headers
     * @param <T> object to be converted into JSON and posted
     * @param headers 
     * @return
     */
    public abstract <T> ResponseVO processPostRequest(T object, Map<String, String> headers);
    
    
    public abstract ResponseVO processPostRequestWithCredentials(HttpEntity entity, List<Header> headers, Credentials credentials);


    protected ResponseVO buildResponseVO(HttpResponse httpResponse) {
        ResponseVO response = new ResponseVO();
        fillStatusCodeForResponse(response, httpResponse);
        InputStream responseContentStream = null;
        try {
            responseContentStream = httpResponse.getEntity().getContent();
        } catch (IOException | IllegalStateException ex) {
            log.error("Error trying to get response content: ", ex);
        }
        if (responseContentStream == null) {
            return response;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseContentStream))) {
            response.setServerResponse(reader.lines().collect(Collectors.joining()));
        } catch (IOException ex) {
            log.error("Error submitting the request or processing the response: ", ex);
        }
        return response;
    }

    protected void fillStatusCodeForResponse(ResponseVO response, HttpResponse httpResponse) {
        int responseStatusCode = httpResponse.getStatusLine().getStatusCode();
        if (responseStatusCode != 200) {
            log.warn("Server problem detected, response code: " + responseStatusCode);
        }
        response.setStatusCode(responseStatusCode);
    }
    
    protected List<Header> createHttpHeadersFromMap(Map<String, String> headers) {
        return headers.entrySet().stream().map(entry -> new BasicHeader(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    public abstract ResponseVO processPostRequest(List<NameValuePair> formParams);

    public abstract ResponseVO processPostRequest(List<NameValuePair> formParams, Map<String, String> headers);
}
