package net.spauny.joy.wellrested.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import net.spauny.joy.wellrested.vo.ResponseVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;

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
}
