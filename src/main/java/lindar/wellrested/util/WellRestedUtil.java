package lindar.wellrested.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import lindar.wellrested.vo.ResponseVO;

/**
 *
 * @author iulian
 */
@Slf4j
public final class WellRestedUtil {

    public static ResponseVO legacyBuildResponseVO(HttpResponse httpResponse) {
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

    public static ResponseVO buildResponseVO(HttpResponse httpResponse) {
        return buildResponseVO(httpResponse, StringUtils.EMPTY);
    }

    public static ResponseVO buildResponseVO(HttpResponse httpResponse, String url) {
        try {
            ResponseVO responseVO = new ResponseVO();
            responseVO.setCurrentURI(url);

            if (httpResponse.getEntity() != null) {
                String responseContent = EntityUtils.toString(httpResponse.getEntity());
                responseVO.setServerResponse(responseContent);
            }
            
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            responseVO.setStatusCode(statusCode);
            return responseVO;
        } catch (IOException | ParseException ex) {
            log.error("Error occured while building the ResponseVO: ", ex);
        }
        return buildErrorResponseVO(url);
    }

    public static ResponseVO buildErrorResponseVO(String url) {
        ResponseVO responseVO = new ResponseVO();
        responseVO.setCurrentURI(url);
        responseVO.setServerResponse(StringUtils.EMPTY);
        responseVO.setStatusCode(500);
        return responseVO;
    }

    public static void fillStatusCodeForResponse(ResponseVO response, HttpResponse httpResponse) {
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
