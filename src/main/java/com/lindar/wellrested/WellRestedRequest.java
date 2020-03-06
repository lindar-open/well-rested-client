package com.lindar.wellrested;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.lindar.wellrested.util.BasicExclusionStrategy;
import com.lindar.wellrested.util.WellRestedUtil;
import com.lindar.wellrested.vo.WellRestedResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class WellRestedRequest {
    public static final int DEFAULT_TIMEOUT = 10000;

    private static final HttpClient internalStatelessHttpClient;

    private URI         uri;
    private Credentials credentials;
    private HttpHost    proxy;

    private JsonSerializer<Date>   dateSerializer;
    private JsonDeserializer<Date> dateDeserializer;
    private String                 dateFormat;
    private ExclusionStrategy      exclusionStrategy;
    private List<String>           excludedFieldNames;
    private Set<String>            excludedClassNames;
    private List<Header>           globalHeaders;
    private GsonCustomiser         gsonCustomiser;
    private boolean                disableCookiesForAuthRequests;
    private Integer                connectionTimeout;
    private Integer                socketTimeout;


    static {
        internalStatelessHttpClient = HttpClientBuilder.create().disableCookieManagement().build();
        //.disableAuthCaching()
    }

    WellRestedRequest(URI uri, Credentials credentials, HttpHost proxy, JsonSerializer<Date> dateSerializer, JsonDeserializer<Date> dateDeserializer,
                      String dateFormat, ExclusionStrategy exclusionStrategy, List<String> excludedFieldNames, Set<String> excludedClassNames,
                      List<Header> globalHeaders, GsonCustomiser gsonCustomiser, boolean disableCookiesForAuthRequests,
                      Integer connectionTimeout, Integer socketTimeout) {
        this.uri = uri;
        this.credentials = credentials;
        this.proxy = proxy;
        this.dateSerializer = dateSerializer;
        this.dateDeserializer = dateDeserializer;
        this.dateFormat = dateFormat;
        this.exclusionStrategy = exclusionStrategy;
        this.excludedFieldNames = excludedFieldNames;
        this.excludedClassNames = excludedClassNames;
        this.globalHeaders = globalHeaders;
        this.gsonCustomiser = gsonCustomiser;
        this.disableCookiesForAuthRequests = disableCookiesForAuthRequests;
        this.connectionTimeout = connectionTimeout;
        this.socketTimeout = socketTimeout;
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

        @Override public GetRequest headers(List<Header> headers) {
            this.headers = headers;
            return this;
        }

        @Override public WellRestedResponse submit() {
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
            return jsonContent(buildGson().toJson(object));
        }

        @Override public PostRequest headers(List<Header> headers) {
            this.headers = headers;
            return this;
        }

        @Override public PostRequest httpEntity(HttpEntity httpEntity) {
            this.httpEntity = httpEntity;
            return this;
        }

        @Override public WellRestedResponse submit() {
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
            return jsonContent(buildGson().toJson(object));
        }

        @Override public PutRequest headers(List<Header> headers) {
            this.headers = headers;
            return this;
        }

        @Override public PutRequest httpEntity(HttpEntity httpEntity) {
            this.httpEntity = httpEntity;
            return this;
        }

        @Override public WellRestedResponse submit() {
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
            return jsonContent(buildGson().toJson(object));
        }

        @Override public DeleteRequest headers(List<Header> headers) {
            this.headers = headers;
            return this;
        }

        @Override public DeleteRequest httpEntity(HttpEntity httpEntity) {
            this.httpEntity = httpEntity;
            return this;
        }

        @Override public WellRestedResponse submit() {
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
            return WellRestedUtil.buildWellRestedResponse(httpResponse, uri.toString());
        } catch (ConnectTimeoutException cte) {
            log.error("Connection timeout for request: {}", request.toString(), cte);
            return WellRestedUtil.buildConnectionTimeoutWellRestedResponse(uri.toString());
        } catch (SocketTimeoutException ste) {
            log.error("Socket timeout for request: {}", request.toString(), ste);
            return WellRestedUtil.buildSocketTimeoutWellRestedResponse(uri.toString());
        } catch (IOException ex) {
            log.error("Error occurred after executing the request to: {}", uri.toString(), ex);
        }
        return WellRestedUtil.buildErrorWellRestedResponse(uri.toString());
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

    private Gson buildGson() {
        GsonBuilder gsonBuilder = Converters.registerAll(new GsonBuilder());
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

        if (this.gsonCustomiser != null) {
            this.gsonCustomiser.customise(gsonBuilder);
        }
        return gsonBuilder.create();
    }
}
