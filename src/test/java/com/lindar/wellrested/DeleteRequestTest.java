package com.lindar.wellrested;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.lindar.wellrested.WellRestedRequestBuilder;
import com.lindar.wellrested.vo.WellRestedResponse;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

public class DeleteRequestTest {

    WellRestedRequestBuilder builder = new WellRestedRequestBuilder();

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(8089);

    @Before
    public void setupConnection(){
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
    public void testSimpleDelete(){
        stubFor(delete(urlEqualTo("/deletetest/first")).atPriority(5).willReturn(aResponse().withStatus(200).withBody("{ \"Test\" : \"Successful\" }")));

        WellRestedResponse response = builder.url("http://localhost:8089/deletetest/first").build().delete().submit();

        String responseRef = "{ \"Test\" : \"Successful\" }";

        assertEquals(200, response.getStatusCode());
        assertEquals(responseRef, response.getServerResponse());
    }

    @Test
    public void testNullDelete(){
        stubFor(delete(urlEqualTo("/deletetest/first")).atPriority(5).willReturn(aResponse().withStatus(200).withBody("{ \"Test\" : \"Successful\" }")));
        stubFor(delete(urlMatching("/deletetest/.*")).atPriority(10).willReturn(aResponse().withStatus(404).withBody("{ \"Test\" : \"Unsuccessful\" }")));


        WellRestedResponse response = builder.url("http://localhost:8089/deletetest/second").build().delete().submit();

        String responseRef = "{ \"Test\" : \"Unsuccessful\" }";

        assertEquals(404, response.getStatusCode());
        assertEquals(responseRef, response.getServerResponse());
    }

    @Test
    public void testDeleteHeaders(){
        stubFor(delete(urlEqualTo("/deletetest/first")).atPriority(0).withHeader("Accept", matching("application/json")).willReturn(aResponse().withStatus(200).withBody("{ \"Test\" : \"Successful\" }")));

        BasicHeader header1 = new BasicHeader("Accept", "application/json");
        List<Header> headers = new ArrayList<>();
        headers.add(header1);

        WellRestedResponse response = builder.url("http://localhost:8089/deletetest/first").build().delete().headers(headers).submit();

        verify(deleteRequestedFor(urlMatching("/deletetest/first")).withHeader("Accept", matching("application/json")));
    }

}
