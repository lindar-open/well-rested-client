package org.spauny.joy.wellrested.request;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.Credentials;
import org.apache.http.entity.ContentType;
import org.spauny.joy.wellrested.util.WellRestedUtil;
import org.spauny.joy.wellrested.vo.ResponseVO;

/**
 * This class is Deprecated. Please use WellRestedRequest instead!
 * @author iulian.dafinoiu
 */
@Slf4j
@Deprecated
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
        return WellRestedUtil.validateProxy(host, port);
    }
    
    public abstract ResponseVO processGetRequest();
    public abstract ResponseVO processGetRequest(List<Header> headers);
    public abstract ResponseVO processGetRequestWithCredentials(List<Header> headers, Credentials credentials);
    
    public abstract ResponseVO processPostRequest(HttpEntity entity);
    public abstract ResponseVO processPostRequest(HttpEntity entity, List<Header> headers);
    
    public abstract ResponseVO processPostRequest(List<NameValuePair> formParams);

    public abstract ResponseVO processPostRequest(List<NameValuePair> formParams, Map<String, String> headers);
    
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
        return WellRestedUtil.buildResponseVO(httpResponse);
    }

    protected void fillStatusCodeForResponse(ResponseVO response, HttpResponse httpResponse) {
        WellRestedUtil.fillStatusCodeForResponse(response, httpResponse);
    }
    
    protected List<Header> createHttpHeadersFromMap(Map<String, String> headers) {
        return WellRestedUtil.createHttpHeadersFromMap(headers);
    }

}
