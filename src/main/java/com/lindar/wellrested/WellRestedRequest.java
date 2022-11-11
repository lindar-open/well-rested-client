package com.lindar.wellrested;

import com.lindar.wellrested.json.GsonJsonMapper;
import com.lindar.wellrested.json.JsonMapper;
import com.lindar.wellrested.util.WellRestedUtil;
import com.lindar.wellrested.vo.WellRestedResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.ConnectTimeoutException;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.fluent.Executor;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WellRestedRequest {
    public static int DEFAULT_TIMEOUT = 10000;

    private static final JsonMapper          DEFAULT_JSON_MAPPER = new GsonJsonMapper.Builder().build(); // use the builder so some defaults are set
    private static final CloseableHttpClient INTERNAL_STATELESS_HTTP_CLIENT;

    private final URI                 uri;
    private final Credentials         credentials;
    private final HttpHost            proxy;
    private       List<Header>        globalHeaders;
    private final boolean             disableCookiesForAuthRequests;
    private final Timeout             connectionTimeout;
    private final Timeout             responseTimeout;
    private final JsonMapper          jsonMapper;
    private final CloseableHttpClient client;

    static {
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(200);
        connManager.setDefaultMaxPerRoute(100);

        INTERNAL_STATELESS_HTTP_CLIENT = HttpClientBuilder
                .create()
                .disableCookieManagement()
                .setConnectionManager(connManager)
                .build();
    }

    WellRestedRequest(URI uri, Credentials credentials, HttpHost proxy, List<Header> globalHeaders,
                      boolean disableCookiesForAuthRequests, Integer connectionTimeout, Integer responseTimeout,
                      JsonMapper jsonMapper, CloseableHttpClient client) {
        this.uri = uri;
        this.credentials = credentials;
        this.proxy = proxy;
        this.globalHeaders = globalHeaders;
        this.disableCookiesForAuthRequests = disableCookiesForAuthRequests;
        this.connectionTimeout = connectionTimeout != null ? Timeout.of(connectionTimeout, TimeUnit.MILLISECONDS) : null;
        this.responseTimeout = responseTimeout != null ? Timeout.of(responseTimeout, TimeUnit.MILLISECONDS) : null;

        if (jsonMapper == null) {
            this.jsonMapper = DEFAULT_JSON_MAPPER;
        } else {
            this.jsonMapper = jsonMapper;
        }
        this.client = client;
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
            return submitRequest(Request.get(uri), null, headers);
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
            return submitRequest(Request.post(uri), httpEntity, headers);
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
            return submitRequest(Request.put(uri), httpEntity, headers);
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
            return submitRequest(Request.delete(uri), httpEntity, headers);
        }
    }

    //********************* PATCH *******************************************************************/

    /**
     * Allows you to configure and submit PATCH requests
     */
    public PatchRequest patch() {
        return new PatchRequest();
    }

    public class PatchRequest implements RequestResource, HeadersSupport, HttpEntitySupport {
        private List<Header> headers;
        private HttpEntity   httpEntity;

        /**
         * Serialise a java object into JSON String and add it to the request body
         */
        public <T> PatchRequest jsonContent(T object) {
            return jsonContent(jsonMapper.writeValue(object));
        }

        @Override
        public PatchRequest headers(List<Header> headers) {
            this.headers = headers;
            return this;
        }

        @Override
        public PatchRequest httpEntity(HttpEntity httpEntity) {
            this.httpEntity = httpEntity;
            return this;
        }

        @Override
        public WellRestedResponse submit() {
            return submitRequest(Request.patch(uri), httpEntity, headers);
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
            ClassicHttpResponse httpResponse;

            Executor executor = getExecutor();
            if (credentials != null) {
                if (disableCookiesForAuthRequests) {
                    executor.clearCookies().clearAuth();
                }

                String host = uri.getPort() > 0 ? uri.getHost() + ":" + uri.getPort() : uri.getHost();
                executor.auth(host, credentials);
                executor.authPreemptive(host);
            }
            httpResponse = (ClassicHttpResponse) executor.execute(request).returnResponse();
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

    private Executor getExecutor() {
        if (client != null) {
            return Executor.newInstance(client);
        }

        if (credentials != null && disableCookiesForAuthRequests) {
            return Executor.newInstance(INTERNAL_STATELESS_HTTP_CLIENT);
        }

        return Executor.newInstance();
    }

    private void setRequestTimeout(Request request) {
        if (responseTimeout != null) {
            request.responseTimeout(responseTimeout);
        } else {
            request.responseTimeout(Timeout.of(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS));
        }

        if (connectionTimeout != null) {
            request.connectTimeout(connectionTimeout);
        } else {
            request.connectTimeout(Timeout.of(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS));
        }
    }
}