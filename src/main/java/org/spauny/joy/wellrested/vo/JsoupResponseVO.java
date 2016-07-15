package org.spauny.joy.wellrested.vo;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;

/**
 *
 * @author iulian
 */
@Data
public class JsoupResponseVO {
    private String serverResponse = StringUtils.EMPTY;
    private int statusCode;
    private String currentURI;
    private Document document;
}
