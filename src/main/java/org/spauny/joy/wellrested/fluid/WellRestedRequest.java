package org.spauny.joy.wellrested.fluid;

import java.io.IOException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.spauny.joy.wellrested.vo.ResponseVO;

/**
 *
 * @author iulian.dafinoiu
 */
@Slf4j
public class WellRestedRequest {

    private final String url;
    
    private final String username;
    
    private final String password;

    private WellRestedRequest(String url) {
        this.url = url;
        this.username = StringUtils.EMPTY;
        this.password = StringUtils.EMPTY;
    }
    
    private WellRestedRequest(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public static WellRestedRequest build(final String url) {
        return new WellRestedRequest(url);
    }
    
    public static WellRestedRequest build(final String url, final String username, final String password) {
        return new WellRestedRequest(url, username, password);
    }

    public Optional<ResponseVO> get() {
        try {
            HttpResponse httpResponse;
            if (StringUtils.isNotBlank(this.username) && StringUtils.isNotBlank(this.password)) {
                Executor executor = Executor.newInstance().auth(username, password);
                httpResponse = executor.execute(Request.Get(url)).returnResponse();
            } else {
                httpResponse = Request.Get(url).execute().returnResponse();
            }
            return Optional.of(buildResponseVOFromHttpResponse(httpResponse));
        } catch (IOException ex) {
            log.error("Error occured after executing the GET request: ", ex);
        }
        return Optional.empty();
    }
    
    public Optional<ResponseVO> post() {
        try {
            HttpResponse httpResponse;
            if (StringUtils.isNotBlank(this.username) && StringUtils.isNotBlank(this.password)) {
                Executor executor = Executor.newInstance().auth(new UsernamePasswordCredentials(username, password));
                httpResponse = executor.execute(Request.Post(url)).returnResponse();
            } else {
                httpResponse = Request.Post(url).execute().returnResponse();
            }
            return Optional.of(buildResponseVOFromHttpResponse(httpResponse));
        } catch (IOException ex) {
            log.error("Error occured after executing the GET request: ", ex);
        }
        return Optional.empty();
    }

    private ResponseVO buildResponseVOFromHttpResponse(HttpResponse httpResponse) throws IOException {
        String responseContent = EntityUtils.toString(httpResponse.getEntity());
//        String responseContent = response.returnContent().asString();
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        ResponseVO responseVO = new ResponseVO();
        responseVO.setCurrentURI(url);
        responseVO.setServerResponse(responseContent);
        responseVO.setStatusCode(statusCode);
        return responseVO;
    }
}
