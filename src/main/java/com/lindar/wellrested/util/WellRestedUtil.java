package com.lindar.wellrested.util;

import com.lindar.wellrested.json.JsonMapper;
import com.lindar.wellrested.vo.WellRestedResponse;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public final class WellRestedUtil {

    public static WellRestedResponse buildWellRestedResponse(ClassicHttpResponse httpResponse, JsonMapper jsonMapper) {
        return buildWellRestedResponse(httpResponse, StringUtils.EMPTY, jsonMapper);
    }

    public static WellRestedResponse buildWellRestedResponse(ClassicHttpResponse httpResponse, String url, JsonMapper jsonMapper) {
        try {
            WellRestedResponse wellRestedResponse = new WellRestedResponse(jsonMapper);
            wellRestedResponse.setCurrentURI(url);

            if (httpResponse.getHeaders() != null) {
                wellRestedResponse.setResponseHeaders(createHeaderMap(httpResponse.getHeaders()));
            }

            if (httpResponse.getEntity() != null) {
                String responseContent = httpResponse.getEntity() != null
                                         ? EntityUtils.toString(httpResponse.getEntity())
                                         : StringUtils.EMPTY;
                wellRestedResponse.setServerResponse(responseContent);
            }
            int statusCode = httpResponse.getCode();
            wellRestedResponse.setStatusCode(statusCode);
            return wellRestedResponse;
        } catch (IOException | ParseException ex) {
            log.error("Error occurred while building the WellRestedResponse: ", ex);
        }
        return buildErrorWellRestedResponse(url, jsonMapper);
    }

    public static WellRestedResponse buildErrorWellRestedResponse(String url, JsonMapper jsonMapper) {
        WellRestedResponse wellRestedResponse = new WellRestedResponse(jsonMapper);
        wellRestedResponse.setCurrentURI(url);
        wellRestedResponse.setServerResponse(StringUtils.EMPTY);
        wellRestedResponse.setStatusCode(500);
        return wellRestedResponse;
    }

    public static WellRestedResponse buildSocketTimeoutWellRestedResponse(String url, JsonMapper jsonMapper) {
        return buildTimeoutWellRestedResponse(url, true, false, jsonMapper);
    }

    public static WellRestedResponse buildConnectionTimeoutWellRestedResponse(String url, JsonMapper jsonMapper) {
        return buildTimeoutWellRestedResponse(url, false, true, jsonMapper);
    }

    private static WellRestedResponse buildTimeoutWellRestedResponse(String url, boolean socketTimeout, boolean connectionTimeout, JsonMapper jsonMapper) {
        WellRestedResponse wellRestedResponse = new WellRestedResponse(jsonMapper);
        wellRestedResponse.setCurrentURI(url);
        wellRestedResponse.setServerResponse(StringUtils.EMPTY);
        wellRestedResponse.setStatusCode(-1);
        wellRestedResponse.setSocketTimeout(socketTimeout);
        wellRestedResponse.setConnectionTimeout(connectionTimeout);
        return wellRestedResponse;
    }

    public static void fillStatusCodeForResponse(WellRestedResponse response, HttpResponse httpResponse) {
        int responseStatusCode = httpResponse.getCode();
        if (responseStatusCode != 200) {
            log.warn("Server problem detected, response code: " + responseStatusCode);
        }
        response.setStatusCode(responseStatusCode);
    }

    public static boolean validateProxy(String host, int port) {
        if (StringUtils.isBlank(host) || port < 1) {
            throw new IllegalArgumentException("Please provide a valid host and port");
        } else {
            return true;
        }
    }

    public static List<Header> buildHeaders(Map<String, String> headerMap) {
        if (headerMap == null) {
            return new ArrayList<>();
        }
        return headerMap.entrySet().stream()
                        .map(entry -> new BasicHeader(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());
    }

    private Map<String, String> createHeaderMap(Header[] headers) {
        return Arrays.stream(headers)
                     .collect(Collectors
                                      .toMap(NameValuePair::getName, NameValuePair::getValue, (v1, v2) -> {
                                          // duplicate key found, return second value
                                          return v2;
                                      }));
    }
}
