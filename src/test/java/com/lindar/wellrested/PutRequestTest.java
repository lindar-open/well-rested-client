package com.lindar.wellrested;

import com.google.gson.Gson;
import com.lindar.wellrested.model.PHEntry;
import com.lindar.wellrested.vo.WellRestedResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(TestEnvironment.class)
public class PutRequestTest {
    private final WellRestedRequestBuilder builder = new WellRestedRequestBuilder();

    @Test
    public void testSimplePut(){
        stubFor(put(urlEqualTo("/puttest/first")).atPriority(0).withRequestBody(containing("{\"Input\": \"True\"}")).willReturn(aResponse().withStatus(200).withBody("{ \"Test\" : \"Successful\" }").withHeader("Content-Type", "application/json")));
        stubFor(put(urlMatching(".*/.*")).atPriority(1).willReturn(aResponse().withStatus(404).withBody("{ \"Test\" : \"Unsuccessful\" }")));

        WellRestedResponse response = builder.url("http://localhost:8089/puttest/first").build().put().jsonContent("{\"Input\": \"True\"}").submit();

        String responseRef = "{ \"Test\" : \"Successful\" }";

        assertEquals(200, response.getStatusCode());
        assertEquals(responseRef, response.getServerResponse());
    }

    @Test
    public void testNullPut(){
        stubFor(put(urlEqualTo("/puttest/first")).atPriority(0).withRequestBody(containing("{\"Input\": \"True\"}")).willReturn(aResponse().withStatus(200).withBody("{ \"Test\" : \"Successful\" }").withHeader("Content-Type", "application/json")));
        stubFor(put(urlMatching(".*/.*")).atPriority(1).willReturn(aResponse().withStatus(404).withBody("{ \"Test\" : \"Unsuccessful\" }")));

        WellRestedResponse response = builder.url("http://localhost:8089/puttest/last").build().put().jsonContent("{\"Input\": \"True\"}").submit();

        String responseRef = "{ \"Test\" : \"Unsuccessful\" }";

        assertEquals(404, response.getStatusCode());
        assertEquals(responseRef, response.getServerResponse());
    }

    @Test
    public void testPutHeaders(){
        stubFor(put(urlEqualTo("/puttest/first")).atPriority(0).withHeader("Accept", matching("application/json")).withRequestBody(containing("{\"Input\": \"True\"}")).willReturn(aResponse().withStatus(200).withBody("{ \"Test\" : \"Successful\" }").withHeader("Content-Type", "application/json")));

        BasicHeader header1 = new BasicHeader("Accept", "application/json");
        List<Header> headers = new ArrayList<>();
        headers.add(header1);

        WellRestedResponse response = builder.url("http://localhost:8089/puttest/first").build().put().headers(headers).jsonContent("{\"Input\": \"True\"}").submit();

        verify(putRequestedFor(urlMatching("/puttest/first")).withHeader("Accept", matching("application/json")));
    }

    @Test
    public void testPutJsonContent(){
        PHEntry postData = new PHEntry();
        postData.setBody("Test Body");
        postData.setId(1);
        postData.setTitle("Test Title");
        postData.setUserId(1);
        Gson g = new Gson();

        stubFor(put(urlEqualTo("/puttest/first")).atPriority(0).withRequestBody(containing("{\"userId\":1,\"id\":1,\"title\":\"Test Title\",\"body\":\"Test Body\"}")).willReturn(aResponse().withStatus(200).withBody("{ \"Test\" : \"Successful\" }").withHeader("Content-Type", "application/json")));

        WellRestedResponse response = builder.url("http://localhost:8089/puttest/first").build().put().jsonContent(postData).submit();

        verify(putRequestedFor(urlMatching("/puttest/first")).withRequestBody(containing("{\"userId\":1,\"id\":1,\"title\":\"Test Title\",\"body\":\"Test Body\"}")));
    }

}
