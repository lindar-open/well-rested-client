package net.spauny.joy.wellrested.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import net.spauny.joy.wellrested.vo.ResponseVO;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.*;

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
            cb.setSslcontext(createTrustManagerAndReturnSslContext());
        } else {
            SSLContextBuilder sslcb = new SSLContextBuilder();
            cb.setDefaultCredentialsProvider(credsProvider);
            try {
                sslcb.loadTrustMaterial(KeyStore.getInstance(KeyStore.getDefaultType()), new TrustSelfSignedStrategy());
                cb.setSslcontext(sslcb.build());
            } catch ( NoSuchAlgorithmException | KeyStoreException | KeyManagementException ex) {
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
            cb.setSslcontext(createTrustManagerAndReturnSslContext());
        } else {
            SSLContextBuilder sslcb = new SSLContextBuilder();
            try {
                sslcb.loadTrustMaterial(KeyStore.getInstance(KeyStore.getDefaultType()), new TrustSelfSignedStrategy());
                cb.setSslcontext(sslcb.build());
            } catch ( NoSuchAlgorithmException | KeyStoreException | KeyManagementException ex) {
                log.error("executeRequestWithAuthentication: ", ex);
            }
        }

        HttpClient httpClient = cb.build();

        //HttpClient httpClient = HttpClientBuilder.create().build();

        httpRequest.addHeader("User-Agent", USER_AGENT);

        headers.stream().forEach((header) -> {
            httpRequest.addHeader(header);
        });
        return executeRequest(() -> httpClient.execute(httpRequest));
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
