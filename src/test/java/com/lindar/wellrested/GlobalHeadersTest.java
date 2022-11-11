package com.lindar.wellrested;

import com.lindar.wellrested.vo.WellRestedResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(TestEnvironment.class)
public class GlobalHeadersTest {
    private final WellRestedRequestBuilder builder = new WellRestedRequestBuilder();

    @BeforeAll
    public static void setupTests() {
        stubFor(get(urlMatching("/tests/first")).atPriority(0).willReturn(aResponse().withStatus(200).withBody("First Test: Success")));
        stubFor(get(urlMatching("/tests/.*")).atPriority(10).willReturn(aResponse().withStatus(404).withBody("Nothing to GET here.")));
        stubFor(get(urlMatching("/tests/second")).atPriority(0).withHeader("Accept", matching("application/json")).willReturn(aResponse().withStatus(200).withBody("Second Test: Header found")));
        stubFor(get(urlMatching("/tests/second")).atPriority(5).willReturn(aResponse().withStatus(400).withBody("Second Test: Header not found")));
        stubFor(get(urlMatching("/tests/third")).atPriority(0).withHeader("Content-Type", matching("application/json")).willReturn(aResponse().withStatus(200).withBody("Third Test: Header found")));
        stubFor(get(urlMatching("/tests/third")).atPriority(0).withHeader("Content-Type", matching(ContentType.APPLICATION_JSON.toString()))
                                                .willReturn(aResponse().withStatus(200).withBody("Third Test: Header found")));
    }

    @Test
    public void testFirstGlobalHeaderSettingInBuilderMethod() {
        builder.url("http://localhost:8089/tests/second");

        BasicHeader header1 = new BasicHeader("Accept", "application/json");
        List<Header> headers = new ArrayList<Header>();
        headers.add(header1);

        builder.globalHeaders(headers);

        WellRestedResponse response1 = builder.build().get().submit();
        WellRestedResponse response2 = builder.build().get().submit();

        assertEquals(200, response1.getStatusCode());
        assertEquals(200, response2.getStatusCode());
        assertEquals("Second Test: Header found", response1.getServerResponse());
        assertEquals(response1.getServerResponse(), response2.getServerResponse());
    }

    @Test
    public void testSecondGlobalHeaderSettingInBuilderMethod() {
        builder.url("http://localhost:8089/tests/second");

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "application/json");

        builder.globalHeaders(headers);

        WellRestedResponse response1 = builder.build().get().submit();
        WellRestedResponse response2 = builder.build().get().submit();

        assertEquals(200, response1.getStatusCode());
        assertEquals(200, response2.getStatusCode());
        assertEquals("Second Test: Header found", response1.getServerResponse());
        assertEquals(response1.getServerResponse(), response2.getServerResponse());
    }

    @Test
    public void testThirdGlobalHeaderSettingInBuilderMethod() {
        builder.url("http://localhost:8089/tests/second");

        builder.addGlobalHeader("Accept", "application/json");

        WellRestedResponse response1 = builder.build().get().submit();
        WellRestedResponse response2 = builder.build().get().submit();

        assertEquals(200, response1.getStatusCode());
        assertEquals(200, response2.getStatusCode());
        assertEquals("Second Test: Header found", response1.getServerResponse());
        assertEquals(response1.getServerResponse(), response2.getServerResponse());
    }

    @Test
    public void testFourthGlobalHeaderSettingInBuilderMethod() {
        builder.url("http://localhost:8089/tests/third");

        builder.addContentTypeGlobalHeader("application/json");

        WellRestedResponse response1 = builder.build().get().submit();
        WellRestedResponse response2 = builder.build().get().submit();

        assertEquals(200, response1.getStatusCode());
        assertEquals(200, response2.getStatusCode());
        assertEquals("Third Test: Header found", response1.getServerResponse());
        assertEquals(response1.getServerResponse(), response2.getServerResponse());
    }

    @Test
    public void testFifthGlobalHeaderSettingInBuilderMethod() {
        builder.url("http://localhost:8089/tests/third");

        builder.addJsonContentTypeGlobalHeader();

        WellRestedResponse response1 = builder.build().get().submit();
        WellRestedResponse response2 = builder.build().get().submit();

        assertEquals(200, response1.getStatusCode());
        assertEquals(200, response2.getStatusCode());
        assertEquals("Third Test: Header found", response1.getServerResponse());
        assertEquals(response1.getServerResponse(), response2.getServerResponse());
    }

    @Test
    public void testListHeaderSettingInRequest() {
        builder.url("http://localhost:8089/tests/second");

        BasicHeader header1 = new BasicHeader("Accept", "application/json");
        List<Header> headers = new ArrayList<Header>();
        headers.add(header1);

        WellRestedRequest request = builder.build().globalHeaders(headers);

        WellRestedResponse response1 = request.get().submit();
        WellRestedResponse response2 = request.get().submit();

        assertEquals(200, response1.getStatusCode());
        assertEquals(200, response2.getStatusCode());
        assertEquals("Second Test: Header found", response1.getServerResponse());
        assertEquals(response1.getServerResponse(), response2.getServerResponse());
    }

    @Test
    public void testMapHeaderSettingInRequest() {
        builder.url("http://localhost:8089/tests/second");

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        WellRestedRequest request = builder.build().globalHeaders(headers);

        WellRestedResponse response1 = request.get().submit();
        WellRestedResponse response2 = request.get().submit();

        assertEquals(200, response1.getStatusCode());
        assertEquals(200, response2.getStatusCode());
        assertEquals("Second Test: Header found", response1.getServerResponse());
        assertEquals(response1.getServerResponse(), response2.getServerResponse());
    }

    @Test
    public void testSingleHeaderSettingInRequest() {
        builder.url("http://localhost:8089/tests/second");

        WellRestedRequest request = builder.build().addGlobalHeader("Accept", "application/json");

        WellRestedResponse response1 = request.get().submit();
        WellRestedResponse response2 = request.get().submit();

        assertEquals(200, response1.getStatusCode());
        assertEquals(200, response2.getStatusCode());
        assertEquals("Second Test: Header found", response1.getServerResponse());
        assertEquals(response1.getServerResponse(), response2.getServerResponse());
    }

    @Test
    public void testClearHeadersInRequest() {
        builder.url("http://localhost:8089/tests/second");

        WellRestedRequest request = builder.build().addGlobalHeader("Accept", "application/json");

        WellRestedResponse response1 = request.get().submit();
        WellRestedResponse response2 = request.clearGlobalHeaders().get().submit();

        assertEquals(200, response1.getStatusCode());
        assertEquals(400, response2.getStatusCode());
        assertEquals("Second Test: Header found", response1.getServerResponse());
        assertEquals("Second Test: Header not found", response2.getServerResponse());
    }

}
