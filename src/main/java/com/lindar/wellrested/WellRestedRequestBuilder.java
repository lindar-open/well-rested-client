package com.lindar.wellrested;

import com.lindar.wellrested.json.JsonMapper;
import com.lindar.wellrested.util.WellRestedUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.BasicHeader;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WellRestedRequestBuilder {
    private static final String CONTENT_TYPE_PARAM  = "Content-Type";
    private static final String AUTHORIZATION_PARAM = "Authorization";

    private URI                 uri;
    private Credentials         credentials;
    private HttpHost            proxy;
    private List<Header>        globalHeaders;
    private boolean             disableCookiesForAuthRequests;
    private Integer             connectionTimeout;
    private Integer             responseTimeout;
    private JsonMapper          jsonMapper;
    private CloseableHttpClient httpClient;

    public WellRestedRequestBuilder() {
    }

    public WellRestedRequestBuilder uri(URI uri) {
        this.uri = uri;
        return this;
    }

    public WellRestedRequestBuilder url(String url) {
        this.uri = URI.create(url);
        return this;
    }

    public WellRestedRequestBuilder credentials(Credentials credentials) {
        this.credentials = credentials;
        return this;
    }

    public WellRestedRequestBuilder credentials(String username, String password) {
        this.credentials = new UsernamePasswordCredentials(username, password.toCharArray());
        return this;
    }

    public WellRestedRequestBuilder proxy(HttpHost proxy) {
        this.proxy = proxy;
        return this;
    }

    public WellRestedRequestBuilder proxy(String proxyHost, int proxyPort, String scheme) {
        this.proxy = new HttpHost(scheme, proxyHost, proxyPort);
        return this;
    }

    /**
     * Use this method to add some global headers to the WellRestedRequest object.
     * These headers are going to be added on every request you make. <br/>
     * A good use for this method is setting a global authentication header or a content type header.
     */
    public WellRestedRequestBuilder globalHeaders(List<Header> globalHeaders) {
        this.globalHeaders = globalHeaders;
        return this;
    }

    /**
     * Use this method to add some global headers to the WellRestedRequest object.
     * These headers are going to be added on every request you make. <br/>
     * A good use for this method is setting a global authentication header or a content type header.
     */
    public WellRestedRequestBuilder globalHeaders(Map<String, String> globalHeaders) {
        this.globalHeaders = WellRestedUtil.buildHeaders(globalHeaders);
        return this;
    }

    /**
     * Use this method to add one more global header.
     * These headers are going to be added on every request you make. <br/>
     * A good use for this method is setting a global authentication header or a content type header.
     */
    public WellRestedRequestBuilder addGlobalHeader(String name, String value) {
        if (this.globalHeaders == null) {
            this.globalHeaders = new ArrayList<>();
        }
        this.globalHeaders.add(new BasicHeader(name, value));
        return this;
    }

    public WellRestedRequestBuilder addContentTypeGlobalHeader(String contentType) {
        if (this.globalHeaders == null) {
            this.globalHeaders = new ArrayList<>();
        }
        this.globalHeaders.add(new BasicHeader(CONTENT_TYPE_PARAM, contentType));
        return this;
    }

    public WellRestedRequestBuilder addJsonContentTypeGlobalHeader() {
        if (this.globalHeaders == null) {
            this.globalHeaders = new ArrayList<>();
        }
        this.globalHeaders.add(new BasicHeader(CONTENT_TYPE_PARAM, ContentType.APPLICATION_JSON.toString()));
        return this;
    }

    public WellRestedRequestBuilder addAuthorizationGlobalHeader(String username, String password) {
        if (this.globalHeaders == null) {
            this.globalHeaders = new ArrayList<>();
        }
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
        String authHeader = "Basic " + new String(encodedAuth);
        this.globalHeaders.add(new BasicHeader(AUTHORIZATION_PARAM, authHeader));
        return this;
    }

    /**
     * Use this method to clear all the cookies when doing secure requests (with Credentials).
     * This is helpful in preventing SESSION cookie cache and clash
     */
    public WellRestedRequestBuilder disableCookiesForAuthRequests() {
        this.disableCookiesForAuthRequests = true;
        return this;
    }

    /**
     * Determines both the response timeout and connection timeout to this value in milliseconds.
     * See the different properties for more details
     * <p>
     * A timeout value of zero is interpreted as an infinite timeout.
     * A negative value is interpreted as undefined (system default).
     * </p>
     * <p>
     * Default: {@code 5000}
     * </p>
     */
    public WellRestedRequestBuilder timeout(Integer timeout) {
        this.connectionTimeout = timeout;
        this.responseTimeout = timeout;
        return this;
    }

    /**
     * Determines the timeout in milliseconds until a connection is established.
     * <p>
     * A timeout value of zero is interpreted as an infinite timeout.
     * A negative value is interpreted as undefined (system default).
     * </p>
     * <p>
     * Default: {@code 5000}
     * </p>
     */
    public WellRestedRequestBuilder connectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * Defines the response timeout in milliseconds, this is time that takes to receive a response after sending a request.
     */
    public WellRestedRequestBuilder responseTimeout(Integer responseTimeout) {
        this.responseTimeout = responseTimeout;
        return this;
    }

    /**
     * Sets both the response timeout and connection timeout to infinite (timeout value of zero)
     * See the different properties for more details
     */
    public WellRestedRequestBuilder infiniteTimeout() {
        this.connectionTimeout = 0;
        this.responseTimeout = 0;
        return this;
    }

    /**
     * Sets both the response timeout and connection timeout to system default (timeout value of -1)
     * See the different properties for more details
     */
    public WellRestedRequestBuilder systemDefaultTimeout() {
        this.connectionTimeout = -1;
        this.responseTimeout = -1;
        return this;
    }

    public WellRestedRequestBuilder jsonMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        return this;
    }

    public WellRestedRequestBuilder customHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public WellRestedRequest build() {
        return new WellRestedRequest(this.uri, this.credentials, this.proxy,
                                     this.globalHeaders, this.disableCookiesForAuthRequests,
                                     this.connectionTimeout, this.responseTimeout, this.jsonMapper, this.httpClient);
    }
}