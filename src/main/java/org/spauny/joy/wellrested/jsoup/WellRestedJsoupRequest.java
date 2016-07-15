package org.spauny.joy.wellrested.jsoup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.spauny.joy.wellrested.vo.JsoupResponseVO;

/**
 *
 * @author iulian
 */
@Slf4j
public class WellRestedJsoupRequest {
    private final String url;
    private final int REQUEST_TIMEOUT = 300000;
    
    private int requestTimeout;

    private WellRestedJsoupRequest(String url) {
        this.url = url;
        this.requestTimeout = REQUEST_TIMEOUT;
    }

    public static WellRestedJsoupRequest build(final String url) {
        return new WellRestedJsoupRequest(url);
    }
    
    public WellRestedJsoupRequest setTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }

    public Optional<JsoupResponseVO> get() {
        return get(new HashMap<>());
    }
    
    public Optional<JsoupResponseVO> get(Map<String, String> requestData) {
        System.getProperties().setProperty("javax.net.debug", "all");
        Document doc;
        try {
            Connection.Response response;
            response = Jsoup
                    .connect(url)
                    .method(Connection.Method.GET)
                    .timeout(requestTimeout)
                    .maxBodySize(0)
                    .data(requestData)
                    .execute();
            doc = response.parse();
            int statusCode = response.statusCode();
            String responseMsg = response.statusMessage();
            
            JsoupResponseVO jsoupResponseVO = new JsoupResponseVO();
            jsoupResponseVO.setCurrentURI(url);
            jsoupResponseVO.setDocument(doc);
            jsoupResponseVO.setServerResponse(responseMsg);
            jsoupResponseVO.setStatusCode(statusCode);
            
            return Optional.of(jsoupResponseVO);
        } catch (IOException e) {
            log.error("Jsoup call failed for url: {}", this.url, e);
        }
        return Optional.empty();
    }
}
