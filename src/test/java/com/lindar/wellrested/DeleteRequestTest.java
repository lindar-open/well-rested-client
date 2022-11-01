package com.lindar.wellrested;

import com.lindar.wellrested.vo.WellRestedResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(TestEnvironment.class)
public class DeleteRequestTest {
    private final WellRestedRequestBuilder builder = new WellRestedRequestBuilder();

    @Test
    public void testSimpleDelete() {
        stubFor(delete(urlEqualTo("/deletetest/first")).atPriority(5).willReturn(aResponse().withStatus(200).withBody("{ \"Test\" : \"Successful\" }")));

        WellRestedResponse response = builder.url("http://localhost:8089/deletetest/first").build().delete().submit();

        String responseRef = "{ \"Test\" : \"Successful\" }";

        assertEquals(200, response.getStatusCode());
        assertEquals(responseRef, response.getServerResponse());
    }

    @Test
    public void testContentDelete() {
        stubFor(delete(urlEqualTo("/deletetest/content")).withRequestBody(containing("{\"Input\": \"Yes\"}")).atPriority(5)
                                                         .willReturn(aResponse().withStatus(200).withBody("{ \"Test\" : \"Very Successful\" }")));

        WellRestedResponse response = builder.url("http://localhost:8089/deletetest/content").build().delete().jsonContent("{\"Input\": \"Yes\"}").submit();

        String responseRef = "{ \"Test\" : \"Very Successful\" }";

        assertEquals(200, response.getStatusCode());
        assertEquals(responseRef, response.getServerResponse());
    }

    @Test
    public void testNullDelete() {
        stubFor(delete(urlEqualTo("/deletetest/first")).atPriority(5).willReturn(aResponse().withStatus(200).withBody("{ \"Test\" : \"Successful\" }")));
        stubFor(delete(urlMatching("/deletetest/.*")).atPriority(10).willReturn(aResponse().withStatus(404).withBody("{ \"Test\" : \"Unsuccessful\" }")));


        WellRestedResponse response = builder.url("http://localhost:8089/deletetest/second").build().delete().submit();

        String responseRef = "{ \"Test\" : \"Unsuccessful\" }";

        assertEquals(404, response.getStatusCode());
        assertEquals(responseRef, response.getServerResponse());
    }

    @Test
    public void testDeleteHeaders() {
        stubFor(delete(urlEqualTo("/deletetest/first")).atPriority(0).withHeader("Accept", matching("application/json"))
                                                       .willReturn(aResponse().withStatus(200).withBody("{ \"Test\" : \"Successful\" }")));

        BasicHeader header1 = new BasicHeader("Accept", "application/json");
        List<Header> headers = new ArrayList<>();
        headers.add(header1);

        WellRestedResponse response = builder.url("http://localhost:8089/deletetest/first").build().delete().headers(headers).submit();

        verify(deleteRequestedFor(urlMatching("/deletetest/first")).withHeader("Accept", matching("application/json")));
    }

}
