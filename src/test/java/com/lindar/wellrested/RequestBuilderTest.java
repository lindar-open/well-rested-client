package com.lindar.wellrested;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lindar.wellrested.json.GsonJsonMapper;
import com.lindar.wellrested.util.BasicExclusionStrategy;
import com.lindar.wellrested.util.LongDateSerializer;
import com.lindar.wellrested.vo.WellRestedResponse;
import org.apache.http.Header;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.junit.*;

import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.junit.Assert.assertEquals;

public class RequestBuilderTest {

    private WellRestedRequestBuilder builder = new WellRestedRequestBuilder();

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(8089);

    @Before
    public void setupTests(){
        builder = new WellRestedRequestBuilder();
        stubFor(get(urlMatching("/tests/first")).atPriority(0).willReturn(aResponse().withStatus(200).withBody("First Test: Success")));
        stubFor(get(urlMatching("/tests/.*")).atPriority(10).willReturn(aResponse().withStatus(404).withBody("Nothing to GET here.")));
    }

    @BeforeClass
    public static void waitToStart(){
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    public void cleanUp(){
        builder = null;
    }

    @Test
    public void testURIMethods(){
        WellRestedResponse response1 = builder.url("http://localhost:8089/tests/first").build().get().submit();

        WellRestedResponse response2 = builder.uri(URI.create("http://localhost:8089/tests/first")).build().get().submit();

        assertEquals(200, response1.getStatusCode());
        assertEquals(200, response2.getStatusCode());
        assertEquals(response1.getServerResponse(), response2.getServerResponse());
        assertEquals(response1.getCurrentURI(), response2.getCurrentURI());
    }
}
