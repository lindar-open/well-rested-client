package org.spauny.joy.wellrested.request;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.spauny.joy.wellrested.vo.ResponseVO;

/**
 *
 * @author iulian.dafinoiu
 */
@Slf4j
public class HttpsRequestProcessor extends AbstractRequestProcessor {

    private boolean trustAll = false;

    public HttpsRequestProcessor(String url) {
        super(url);
    }

    public HttpsRequestProcessor(String url, boolean trustAll) {
        super(url);
        this.trustAll = trustAll;
    }

    public HttpsRequestProcessor(String url, String proxyHost, int proxyPort) {
        super(url, proxyHost, proxyPort);
    }

    public HttpsRequestProcessor(String url, String proxyHost, int proxyPort, ProxyValidator conditionalProxyValidator) {
        super(url, proxyHost, proxyPort, conditionalProxyValidator);
    }

    @Override
    public ResponseVO processGetRequest() {
        return processGetRequest(Lists.newArrayList());
    }

    @Override
    public ResponseVO processGetRequest(List<Header> headers) {
        return processGetRequestWithCredentials(headers, null);
    }

    @Override
    public ResponseVO processGetRequestWithCredentials(List<Header> headers, Credentials credentials) {
        URI uri = URI.create(this.url);
        HttpGet httpGet = new HttpGet(uri);
        log.info("Request to be sent: " + this.url);
        if (credentials != null) {
            return executeRequestWithAuthentication(uri, httpGet, credentials);
        }
        return executeDefaultRequest(httpGet, headers);
    }

    @Override
    public ResponseVO processPostRequest(HttpEntity entity) {
        return processPostRequest(entity, Lists.newArrayList());
    }

    @Override
    public ResponseVO processPostRequest(HttpEntity entity, List<Header> headers) {
        return processPostRequestWithCredentials(entity, headers, null);
    }

    @Override
    public ResponseVO processPostRequest(String content, ContentType contentType) {
        return processPostRequest(content, contentType, null);
    }

    @Override
    public ResponseVO processPostRequest(String content, ContentType contentType, Map<String, String> headers) {
        HttpEntity httpEntity = new StringEntity(content, contentType);
        if (headers != null && !headers.isEmpty()) {
            return processPostRequest(httpEntity, createHttpHeadersFromMap(headers));
        }
        return processPostRequest(httpEntity);
    }

    @Override
    public ResponseVO processPostRequest(String json) {
        return processPostRequest(json, new HashMap<>(0));
    }

    @Override
    public ResponseVO processPostRequest(String json, Map<String, String> headers) {
        HttpEntity httpEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        if (headers != null && !headers.isEmpty()) {
            return processPostRequest(httpEntity, createHttpHeadersFromMap(headers));
        }
        return processPostRequest(httpEntity);
    }

    @Override
    public <T> ResponseVO processPostRequest(T object) {
        return processPostRequest(object, null);
    }

    @Override
    public <T> ResponseVO processPostRequest(T object, Map<String, String> headers) {
        Gson gson = new Gson();
        ContentType contentType = ContentType.APPLICATION_JSON;
        HttpEntity httpEntity = new StringEntity(gson.toJson(object), contentType);
        if (headers != null && !headers.isEmpty()) {
            return processPostRequest(httpEntity, createHttpHeadersFromMap(headers));
        }
        return processPostRequest(httpEntity);
    }

    @Override
    public ResponseVO processPostRequestWithCredentials(HttpEntity entity, List<Header> headers, Credentials credentials) {
        URI uri = URI.create(this.url);
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setEntity(entity);
        log.info("Request to be sent: " + this.url);
        if (credentials != null) {
            return executeRequestWithAuthentication(uri, httpPost, credentials);
        }
        return executeDefaultRequest(httpPost, headers);
    }

    private ResponseVO executeRequestWithAuthentication(URI uri, HttpUriRequest httpRequest, Credentials credentials) {
        HttpHost host = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(uri.getHost(), uri.getPort()), credentials);

        AuthCache authCache = new BasicAuthCache();

        BasicScheme basicAuth = new BasicScheme();
        authCache.put(host, basicAuth);

        HttpClientContext localContext = HttpClientContext.create();
        localContext.setAuthCache(authCache);

        HttpClientBuilder cb = HttpClientBuilder.create();

        if (this.trustAll) {
            cb.setSSLContext(createTrustManagerAndReturnSslContext());
        } else {
            SSLContextBuilder sslcb = new SSLContextBuilder();
            cb.setDefaultCredentialsProvider(credsProvider);
            try {
                sslcb.loadTrustMaterial(KeyStore.getInstance(KeyStore.getDefaultType()), new TrustSelfSignedStrategy());
                cb.setSSLContext(sslcb.build());
            } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException ex) {
                log.error("executeRequestWithAuthentication: ", ex);
            }
        }

        //CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        HttpClient httpClient = cb.build();
        return executeRequest(() -> httpClient.execute(host, httpRequest, localContext));
    }

    private ResponseVO executeDefaultRequest(HttpUriRequest httpRequest, List<Header> headers) {
        HttpClientBuilder cb = HttpClientBuilder.create();

        if (this.trustAll) {
            cb.setSSLContext(createTrustManagerAndReturnSslContext());
        } else {
            SSLContextBuilder sslcb = new SSLContextBuilder();
            try {
                sslcb.loadTrustMaterial(KeyStore.getInstance(KeyStore.getDefaultType()), new TrustSelfSignedStrategy());
                cb.setSSLContext(sslcb.build());
            } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException ex) {
                log.error("executeRequestWithAuthentication: ", ex);
            }
        }

        HttpClient httpClient = cb.build();
        
        HttpClientContext context = HttpClientContext.create();

        httpRequest.addHeader("User-Agent", USER_AGENT);

        headers.stream().forEach((header) -> {
            httpRequest.addHeader(header);
        });
        ResponseVO responseVO = executeRequest(() -> httpClient.execute(httpRequest, context));
        List<URI> redirectURIs = context.getRedirectLocations();
        if (redirectURIs != null && !redirectURIs.isEmpty()) {
            URI finalURI = redirectURIs.get(redirectURIs.size() - 1);
            responseVO.setCurrentURI(finalURI.toString());
        }
        return responseVO;
    }

    private ResponseVO executeRequest(HttpClientExecutor httpClientExecutor) {
        try {
            return buildResponseVO(httpClientExecutor.executeClient());
        } catch (Exception ex) {
            Logger.getLogger(HttpRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ResponseVO();
    }

    private SSLContext createTrustManagerAndReturnSslContext() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] xcs, String string) throws CertificateException {
                    return;
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] xcs, String string) throws CertificateException {
                    return;
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }};

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            log.error("createTrustManagerAndReturnSslContext: ", ex);
        }
        return null;
    }

}
