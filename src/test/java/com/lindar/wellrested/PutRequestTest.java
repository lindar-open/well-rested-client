package com.lindar.wellrested;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.lindar.wellrested.vo.WellRestedResponse;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

public class PutRequestTest {

    WellRestedRequestBuilder builder = new WellRestedRequestBuilder();

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(8089);

    @Before
    public void setupConnections(){
        builder = new WellRestedRequestBuilder();
    }

    @BeforeClass
    public static void waitToStart(){
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


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
