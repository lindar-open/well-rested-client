package com.lindar.wellrested;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.lindar.wellrested.json.GsonJsonMapper;
import com.lindar.wellrested.util.BasicExclusionStrategy;
import com.lindar.wellrested.vo.WellRestedResponse;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

public class PostRequestTest {

    WellRestedRequestBuilder builder = new WellRestedRequestBuilder();
    Gson g = new Gson();

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(8089);

    @Before
    public void setupConnections(){
        builder = new WellRestedRequestBuilder();

        stubFor(post(urlEqualTo("/posttest/first")).withRequestBody(containing("{\"Input\": \"True\"}")).willReturn(aResponse().withHeader("Content-Type", "application/json").withBody( "{ \"Test\" : \"Successful\" }")));
        stubFor(post(urlEqualTo("/posttest/second")).withRequestBody(containing("{\"userId\":1,\"id\":1,\"title\":\"Test Title\",\"body\":\"Test Body\"}")).willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(201).withBody( "{ \"Test\" : \"Partly Successful\" }")));
        stubFor(post(urlEqualTo("/posttest/second")).withRequestBody(containing("{\"userId\":1,\"id\":1,\"body\":\"Test Body\"}")).willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(200).withBody( "{ \"Test\" : \"Successful\" }")));
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
    public void testSimplePost(){
        WellRestedResponse response1 = builder.url("http://localhost:8089/posttest/first").build().post().jsonContent("{\"Input\": \"True\"}").submit();

        assertEquals(200, response1.getStatusCode());
        assertEquals("{ \"Test\" : \"Successful\" }", response1.getServerResponse());
    }

    @Test
    public void testResponseHeaders(){
        WellRestedResponse response = builder.url("http://localhost:8089/posttest/first").build().post().jsonContent("{\"Input\": \"True\"}").submit();

       Map<String, String> headers =  response.getResponseHeaders();

       assertEquals(200, response.getStatusCode());
       assertEquals("application/json", headers.get("Content-Type"));
   }

    @Test
    public void testExcludedPost(){
        PHEntry postData = new PHEntry();
        postData.setBody("Test Body");
        postData.setId(1);
        postData.setTitle("Test Title");
        postData.setUserId(1);

        List<String> fieldExclusion = new ArrayList<>();
        fieldExclusion.add("title");
        BasicExclusionStrategy excStrat = new BasicExclusionStrategy(fieldExclusion);

        builder.jsonMapper(new GsonJsonMapper.Builder()
                                   .exclusionStrategy(excStrat)
                                   .build());
        WellRestedResponse response = builder.url("http://localhost:8089/posttest/second").build().post().jsonContent(postData).submit();

        assertEquals(200, response.getStatusCode());
        assertEquals("{ \"Test\" : \"Successful\" }", response.getServerResponse());
   }

    @Test
    public void testPostHeaders(){
       BasicHeader header1 = new BasicHeader("Accept", "application/json");
       List<Header> headers = new ArrayList<>();
       headers.add(header1);

       WellRestedResponse response1 = builder.url("http://localhost:8089/posttest/first").build().post().headers(headers).jsonContent("{\"Input\": \"True\"}").submit();

       verify(postRequestedFor(urlMatching("/posttest/first")).withHeader("Accept", matching("application/json")));
   }


}
