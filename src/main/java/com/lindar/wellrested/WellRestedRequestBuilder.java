package com.lindar.wellrested;

import com.google.gson.ExclusionStrategy;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.lindar.wellrested.util.DateDeserializer;
import com.lindar.wellrested.util.StringDateSerializer;
import com.lindar.wellrested.util.WellRestedUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WellRestedRequestBuilder {
    private static final String CONTENT_TYPE_PARAM  = "Content-Type";
    private static final String AUTHORIZATION_PARAM = "Authorization";

    private URI                    uri;
    private Credentials            credentials;
    private HttpHost               proxy;
    private JsonSerializer<Date>   dateSerializer   = new StringDateSerializer();
    private JsonDeserializer<Date> dateDeserializer = new DateDeserializer();
    private String                 dateFormat       = StringUtils.EMPTY;
    private ExclusionStrategy      exclusionStrategy;
    private List<String>           excludedFieldNames;
    private Set<String>            excludedClassNames;
    private List<Header>           globalHeaders;
    private GsonCustomiser         gsonCustomiser;
    private boolean                disableCookiesForAuthRequests;
    private Integer                timeout;
    private Integer                connectionTimeout;
    private Integer                socketTimeout;
    private HttpClient             client;

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
        this.credentials = new UsernamePasswordCredentials(username, password);
        return this;
    }

    public WellRestedRequestBuilder proxy(HttpHost proxy) {
        this.proxy = proxy;
        return this;
    }

    public WellRestedRequestBuilder proxy(String proxyHost, int proxyPort, String scheme) {
        this.proxy = new HttpHost(proxyHost, proxyPort, scheme);
        return this;
    }

    public WellRestedRequestBuilder gsonCustomiser(GsonCustomiser gsonCustomiser) {
        this.gsonCustomiser = gsonCustomiser;
        return this;
    }

    /**
     * Use this method to override the default dateSerializer.
     * The default one is {@link com.lindar.wellrested.util.StringDateSerializer}
     * <br/>
     * NOTE: Well Rested Client provides 2 serializers which can be passed as parameters for this method: <br/>
     * - {@link com.lindar.wellrested.util.StringDateSerializer} <br/>
     * - {@link com.lindar.wellrested.util.LongDateSerializer} <br/>
     * If neither satisfies your requirements, please write your own.
     */
    public WellRestedRequestBuilder dateSerializer(JsonSerializer<Date> dateSerializer) {
        this.dateSerializer = dateSerializer;
        return this;
    }

    /**
     * Use this method to override the default dateSerializer.
     * The default one is {@link com.lindar.wellrested.util.DateDeserializer}
     * <br/>
     * If the default one doesn't satisfy your requirements, please write your own.
     */
    public WellRestedRequestBuilder dateDeserializer(JsonDeserializer<Date> dateDeserializer) {
        this.dateDeserializer = dateDeserializer;
        return this;
    }

    /**
     * Use this method to provide a date format that will be used when doing both the serialization and deserialization. <br/>
     * If you require different formats for serialization and deserialization, please use the <b>setDateSerializer</b> and <b>setDateDeserializer</b> methods. <br/>
     * By default this class uses {@link com.lindar.wellrested.util.StringDateSerializer} and {@link com.lindar.wellrested.util.DateDeserializer} <br/>
     * <b>PLEASE NOTE:</b> By setting a dateFormat, you <b>override</b> any other serializer and deserializer!
     */
    public WellRestedRequestBuilder dateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }

    /**
     * Use this method to provide a Gson serialisation exclusion strategy.
     * If you just want to exclude some field names or a class you can use the excludeFields and excludeClasses methods easier.
     * Keep in mind that this exclusion strategy overrides the other ones
     */
    public WellRestedRequestBuilder exclusionStrategy(ExclusionStrategy exclusionStrategy) {
        this.exclusionStrategy = exclusionStrategy;
        return this;
    }

    /**
     * Use this method to exclude some field names from the Gson serialisation process
     */
    public WellRestedRequestBuilder excludeFields(List<String> fieldNames) {
        this.excludedFieldNames = fieldNames;
        return this;
    }

    /**
     * Use this method to exclude some class names from the Gson serialisation process
     */
    public WellRestedRequestBuilder excludeClasses(Set<String> classNames) {
        this.excludedClassNames = classNames;
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
     * Determines both the socket timeout and connection timeout to this value in milliseconds.
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
        this.socketTimeout = timeout;
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
     * Defines the socket timeout ({@code SO_TIMEOUT}) in milliseconds,
     * which is the timeout for waiting for data  or, put differently,
     * a maximum period inactivity between two consecutive data packets).
     * <p>
     * A timeout value of zero is interpreted as an infinite timeout.
     * A negative value is interpreted as undefined (system default).
     * </p>
     * <p>
     * Default: {@code 5000}
     * </p>
     */
    public WellRestedRequestBuilder socketTimeout(Integer socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }

    public WellRestedRequestBuilder client(HttpClient client) {
        this.client = client;
        return this;
    }

    /**
     * Sets both the socket timeout and connection timeout to infinite (timeout value of zero)
     * See the different properties for more details
     */
    public WellRestedRequestBuilder infiniteTimeout() {
        this.connectionTimeout = 0;
        this.socketTimeout = 0;
        return this;
    }

    /**
     * Sets both the socket timeout and connection timeout to system default (timeout value of -1)
     * See the different properties for more details
     */
    public WellRestedRequestBuilder systemDefaultTimeout() {
        this.connectionTimeout = -1;
        this.socketTimeout = -1;
        return this;
    }

    /**
     * NOTE: keep in mind that date serializers and deserializers and all exclusion strategies are available only for JSON content
     */
    public WellRestedRequest build() {
        return new WellRestedRequest(this.uri, this.credentials, this.proxy, this.dateSerializer, this.dateDeserializer,
                                     this.dateFormat, this.exclusionStrategy, this.excludedFieldNames, this.excludedClassNames,
                                     this.globalHeaders, this.gsonCustomiser, this.disableCookiesForAuthRequests,
                                     this.connectionTimeout, this.socketTimeout, this.client);
    }
}
