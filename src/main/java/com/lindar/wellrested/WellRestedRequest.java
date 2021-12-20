package com.lindar.wellrested;

import com.lindar.wellrested.json.GsonJsonMapper;
import com.lindar.wellrested.json.JsonMapper;
import com.lindar.wellrested.util.WellRestedUtil;
import com.lindar.wellrested.vo.WellRestedResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class WellRestedRequest {
    public static int DEFAULT_TIMEOUT = 10000;

    private static final JsonMapper DEFAULT_JSON_MAPPER = new GsonJsonMapper.Builder().build(); // use the builder so some defaults are set
    private static final HttpClient internalStatelessHttpClient;

    private final URI          uri;
    private final Credentials  credentials;
    private final HttpHost     proxy;
    private       List<Header> globalHeaders;
    private final boolean      disableCookiesForAuthRequests;
    private final Integer      connectionTimeout;
    private final Integer      socketTimeout;
    private final JsonMapper   jsonMapper;


    static {
        internalStatelessHttpClient = HttpClientBuilder.create().disableCookieManagement().build();
        //.disableAuthCaching()
    }

    WellRestedRequest(URI uri, Credentials credentials, HttpHost proxy,
                      List<Header> globalHeaders, boolean disableCookiesForAuthRequests,
                      Integer connectionTimeout, Integer socketTimeout, JsonMapper jsonMapper) {
        this.uri = uri;
        this.credentials = credentials;
        this.proxy = proxy;
        this.globalHeaders = globalHeaders;
        this.disableCookiesForAuthRequests = disableCookiesForAuthRequests;
        this.connectionTimeout = connectionTimeout;
        this.socketTimeout = socketTimeout;

        if (jsonMapper == null) {
            this.jsonMapper = DEFAULT_JSON_MAPPER;
        } else {
            this.jsonMapper = jsonMapper;
        }
    }

    public static WellRestedRequestBuilder builder() {
        return new WellRestedRequestBuilder();
    }

    /**
     * Use this method to add some global headers to the WellRestedRequest object.
     * These headers are going to be added on every request you make. <br/>
     * A good use for this method is setting a global authentication header or a content type header.
     */
    public WellRestedRequest globalHeaders(List<Header> globalHeaders) {
        this.globalHeaders = globalHeaders;
        return this;
    }

    /**
     * Use this method to add some global headers to the WellRestedRequest object.
     * These headers are going to be added on every request you make. <br/>
     * A good use for this method is setting a global authentication header or a content type header.
     */
    public WellRestedRequest globalHeaders(Map<String, String> globalHeaders) {
        this.globalHeaders = WellRestedUtil.buildHeaders(globalHeaders);
        return this;
    }

    /**
     * Use this method to add one more global header.
     * These headers are going to be added on every request you make. <br/>
     * A good use for this method is setting a global authentication header or a content type header.
     */
    public WellRestedRequest addGlobalHeader(String name, String value) {
        if (this.globalHeaders == null) {
            this.globalHeaders = new ArrayList<>();
        }
        this.globalHeaders.add(new BasicHeader(name, value));
        return this;
    }

    /**
     * Use this method to clear all global headers
     */
    public WellRestedRequest clearGlobalHeaders() {
        if (this.globalHeaders != null) {
            this.globalHeaders.clear();
        }
        return this;
    }

    //********************* GET *******************************************************************/

    /**
     * Allows you to configure and submit GET requests
     */
    public GetRequest get() {
        return new GetRequest();
    }

    public class GetRequest implements RequestResource, HeadersSupport {
        private List<Header> headers;

        @Override
        public GetRequest headers(List<Header> headers) {
            this.headers = headers;
            return this;
        }

        @Override
        public WellRestedResponse submit() {
            return submitRequest(Request.Get(uri), null, headers);
        }
    }


    //********************* POST *******************************************************************/

    /**
     * Allows you to configure and submit POST requests
     */
    public PostRequest post() {
        return new PostRequest();
    }

    public class PostRequest implements RequestResource, HeadersSupport, HttpEntitySupport {
        private List<Header> headers;
        private HttpEntity   httpEntity;

        /**
         * Serialise a java object into JSON String and add it to the request body
         */
        public <T> PostRequest jsonContent(T object) {
            return jsonContent(jsonMapper.writeValue(object));
        }

        @Override
        public PostRequest headers(List<Header> headers) {
            this.headers = headers;
            return this;
        }

        @Override
        public PostRequest httpEntity(HttpEntity httpEntity) {
            this.httpEntity = httpEntity;
            return this;
        }

        @Override
        public WellRestedResponse submit() {
            return submitRequest(Request.Post(uri), httpEntity, headers);
        }
    }

    //********************* PUT *******************************************************************/

    /**
     * Allows you to configure and submit PUT requests
     */
    public PutRequest put() {
        return new PutRequest();
    }

    public class PutRequest implements RequestResource, HeadersSupport, HttpEntitySupport {
        private List<Header> headers;
        private HttpEntity   httpEntity;

        /**
         * Serialise a java object into JSON String and add it to the request body
         */
        public <T> PutRequest jsonContent(T object) {
            return jsonContent(jsonMapper.writeValue(object));
        }

        @Override
        public PutRequest headers(List<Header> headers) {
            this.headers = headers;
            return this;
        }

        @Override
        public PutRequest httpEntity(HttpEntity httpEntity) {
            this.httpEntity = httpEntity;
            return this;
        }

        @Override
        public WellRestedResponse submit() {
            return submitRequest(Request.Put(uri), httpEntity, headers);
        }
    }


    //********************* DELETE *******************************************************************/

    /**
     * Allows you to configure and submit DELETE requests
     */
    public DeleteRequest delete() {
        return new DeleteRequest();
    }

    public class DeleteRequest implements RequestResource, HeadersSupport, HttpEntitySupport {
        private List<Header> headers;
        private HttpEntity   httpEntity;

        /**
         * Serialise a java object into JSON String and add it to the request body
         */
        public <T> DeleteRequest jsonContent(T object) {
            return jsonContent(jsonMapper.writeValue(object));
        }

        @Override
        public DeleteRequest headers(List<Header> headers) {
            this.headers = headers;
            return this;
        }

        @Override
        public DeleteRequest httpEntity(HttpEntity httpEntity) {
            this.httpEntity = httpEntity;
            return this;
        }

        @Override
        public WellRestedResponse submit() {
            return submitRequest(Request.Delete(uri), httpEntity, headers);
        }
    }


    //******************** GENERAL *******************************************************************/

    public WellRestedResponse submitRequest(Request request, HttpEntity httpEntity, List<Header> headers) {
        try {
            if (httpEntity != null) {
                request.body(httpEntity);
            }
            if (headers != null && !headers.isEmpty()) {
                headers.forEach(request::addHeader);
            }
            if (this.globalHeaders != null && !this.globalHeaders.isEmpty()) {
                this.globalHeaders.forEach(request::addHeader);
            }
            if (this.proxy != null) {
                request.viaProxy(proxy);
            }
            setRequestTimeout(request);
            HttpResponse httpResponse;
            if (credentials != null) {
                Executor executor;
                if (disableCookiesForAuthRequests) {
                    executor = Executor.newInstance(internalStatelessHttpClient);
                    executor.clearCookies();
                    executor.clearAuth();
                } else {
                    executor = Executor.newInstance();
                }

                if (uri.getPort() > 0) {
                    executor.authPreemptive(uri.getHost() + ":" + uri.getPort());
                } else {
                    executor.authPreemptive(uri.getHost());
                }

                executor.auth(credentials);
                httpResponse = executor.execute(request).returnResponse();
            } else {
                httpResponse = request.execute().returnResponse();
            }
            return WellRestedUtil.buildWellRestedResponse(httpResponse, uri.toString(), jsonMapper);
        } catch (ConnectTimeoutException cte) {
            log.error("Connection timeout for request: {}", request.toString(), cte);
            return WellRestedUtil.buildConnectionTimeoutWellRestedResponse(uri.toString(), jsonMapper);
        } catch (SocketTimeoutException ste) {
            log.error("Socket timeout for request: {}", request.toString(), ste);
            return WellRestedUtil.buildSocketTimeoutWellRestedResponse(uri.toString(), jsonMapper);
        } catch (IOException ex) {
            log.error("Error occurred after executing the request to: {}", uri.toString(), ex);
        }
        return WellRestedUtil.buildErrorWellRestedResponse(uri.toString(), jsonMapper);
    }

    private void setRequestTimeout(Request request) {
        if (socketTimeout != null) {
            request.socketTimeout(socketTimeout);
        } else {
            request.socketTimeout(DEFAULT_TIMEOUT);
        }

        if (connectionTimeout != null) {
            request.connectTimeout(connectionTimeout);
        } else {
            request.connectTimeout(DEFAULT_TIMEOUT);
        }
    }
}