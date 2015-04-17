package net.spauny.joy.wellrested.vo;

import java.io.Serializable;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class ResponseVO implements Serializable {
    private static final long serialVersionUID = 51255400364556607L;

    private String serverResponse = StringUtils.EMPTY;
    private int statusCode;
}
