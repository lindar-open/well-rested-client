package com.lindar.wellrested;

import com.lindar.wellrested.json.GsonJsonMapper;
import com.lindar.wellrested.model.PHEntry;
import com.lindar.wellrested.util.BasicExclusionStrategy;
import com.lindar.wellrested.vo.WellRestedResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(TestEnvironment.class)
public class PostRequestTest {
    private final WellRestedRequestBuilder builder = new WellRestedRequestBuilder();

    @BeforeAll
    public static void setupConnections(){
        stubFor(post(urlEqualTo("/posttest/first")).withRequestBody(containing("{\"Input\": \"True\"}")).willReturn(aResponse().withHeader("Content-Type", "application/json").withBody( "{ \"Test\" : \"Successful\" }")));
        stubFor(post(urlEqualTo("/posttest/second")).withRequestBody(containing("{\"userId\":1,\"id\":1,\"title\":\"Test Title\",\"body\":\"Test Body\"}")).willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(201).withBody( "{ \"Test\" : \"Partly Successful\" }")));
        stubFor(post(urlEqualTo("/posttest/second")).withRequestBody(containing("{\"userId\":1,\"id\":1,\"body\":\"Test Body\"}")).willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(200).withBody( "{ \"Test\" : \"Successful\" }")));
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
