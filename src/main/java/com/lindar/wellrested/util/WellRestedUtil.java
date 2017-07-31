package com.lindar.wellrested.util;

import com.lindar.wellrested.vo.WellRestedResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public final class WellRestedUtil {

    public static WellRestedResponse buildWellRestedResponse(HttpResponse httpResponse) {
        return buildWellRestedResponse(httpResponse, StringUtils.EMPTY);
    }

    public static WellRestedResponse buildWellRestedResponse(HttpResponse httpResponse, String url) {
        try {
            WellRestedResponse wellRestedResponse = new WellRestedResponse();
            wellRestedResponse.setCurrentURI(url);

            if (httpResponse.getEntity() != null) {
                String responseContent = httpResponse.getEntity() != null 
                        ? EntityUtils.toString(httpResponse.getEntity()) 
                        : StringUtils.EMPTY;
                wellRestedResponse.setServerResponse(responseContent);
            }
            
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            wellRestedResponse.setStatusCode(statusCode);
            return wellRestedResponse;
        } catch (IOException | ParseException ex) {
            log.error("Error occured while building the WellRestedResponse: ", ex);
        }
        return buildErrorWellRestedResponse(url);
    }

    public static WellRestedResponse buildErrorWellRestedResponse(String url) {
        WellRestedResponse wellRestedResponse = new WellRestedResponse();
        wellRestedResponse.setCurrentURI(url);
        wellRestedResponse.setServerResponse(StringUtils.EMPTY);
        wellRestedResponse.setStatusCode(500);
        return wellRestedResponse;
    }

    public static void fillStatusCodeForResponse(WellRestedResponse response, HttpResponse httpResponse) {
        int responseStatusCode = httpResponse.getStatusLine().getStatusCode();
        if (responseStatusCode != 200) {
            log.warn("Server problem detected, response code: " + responseStatusCode);
        }
        response.setStatusCode(responseStatusCode);
    }

    public static List<Header> createHttpHeadersFromMap(Map<String, String> headers) {
        return headers.entrySet().stream().map(entry -> new BasicHeader(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    public static boolean validateProxy(String host, int port) {
        if (StringUtils.isBlank(host) || port < 1) {
            throw new IllegalArgumentException("Please provide a valid host and port");
        } else {
            return true;
        }
    }

    private WellRestedUtil() {
    }
}
