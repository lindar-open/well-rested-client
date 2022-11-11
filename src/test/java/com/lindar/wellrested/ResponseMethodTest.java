package com.lindar.wellrested;

import com.google.gson.Gson;
import com.lindar.wellrested.model.PHEntry;
import com.lindar.wellrested.vo.WellRestedResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TestEnvironment.class)
public class ResponseMethodTest {
    private final WellRestedRequestBuilder builder = new WellRestedRequestBuilder();

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
