package com.lindar.wellrested;

import com.lindar.wellrested.util.WellRestedUtil;
import org.apache.http.Header;

import java.util.List;
import java.util.Map;

interface HeadersSupport {

    RequestResource headers(List<Header> headers);

    default RequestResource headers(Map<String, String> headers) {
        return headers(WellRestedUtil.buildHeaders(headers));
    }
}
