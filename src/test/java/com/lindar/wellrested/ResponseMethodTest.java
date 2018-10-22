package com.lindar.wellrested;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.lindar.wellrested.vo.WellRestedResponse;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertTrue;

public class ResponseMethodTest {

    private WellRestedRequestBuilder builder = new WellRestedRequestBuilder();

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(8089);

    @Before
    public void setupTests(){
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
    public void testClassCastMethod(){
        stubFor(get(urlMatching("/test/first")).atPriority(0).willReturn(aResponse().withStatus(200).withBody("{\"userId\":1,\"id\":1,\"title\":\"testTitle\",\"body\":\"testBody\"}")));

        builder.url("http://localhost:8089/test/first");

        WellRestedResponse response1 = builder.build().get().submit();

        PHEntry data = new PHEntry();
        data.setBody("testBody");
        data.setId(1);
        data.setUserId(1);
        data.setTitle("testTitle");

        WellRestedResponse.JsonResponseMapper mapper = response1.fromJson();

        PHEntry returnData = mapper.castTo(PHEntry.class);

        Gson g = new Gson();

        System.out.println(g.toJson(returnData));

        assertTrue(data.equals(returnData));
    }

}
