package com.lindar.wellrested;

import com.lindar.wellrested.vo.WellRestedResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(TestEnvironment.class)
public class RequestBuilderTest {
    private final WellRestedRequestBuilder builder = new WellRestedRequestBuilder();

    @BeforeAll
    public static void setupTests() {
        stubFor(get(urlMatching("/tests/first")).atPriority(0).willReturn(aResponse().withStatus(200).withBody("First Test: Success")));
        stubFor(get(urlMatching("/tests/.*")).atPriority(10).willReturn(aResponse().withStatus(404).withBody("Nothing to GET here.")));
    }

    @Test
    public void testURIMethods() {
        WellRestedResponse response1 = builder.url("http://localhost:8089/tests/first").build().get().submit();

        WellRestedResponse response2 = builder.uri(URI.create("http://localhost:8089/tests/first")).build().get().submit();

        assertEquals(200, response1.getStatusCode());
        assertEquals(200, response2.getStatusCode());
        assertEquals(response1.getServerResponse(), response2.getServerResponse());
        assertEquals(response1.getCurrentURI(), response2.getCurrentURI());
    }
}
