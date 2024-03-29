package com.lindar.wellrested;

import com.github.tomakehurst.wiremock.matching.AbsentPattern;
import com.lindar.wellrested.vo.WellRestedResponse;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(TestEnvironment.class)
public class GetRequestTest {
    private final WellRestedRequestBuilder builder = new WellRestedRequestBuilder();

    @BeforeAll
    public static void setupConnections() {
        stubFor(get(urlMatching("/gettest/.*")).atPriority(10).willReturn(aResponse().withStatus(404).withBody("Nothing to GET here.")));
        stubFor(get(urlEqualTo("/gettest/first")).atPriority(0).willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("{ \"Test\" : \"Successful\" }")));
        stubFor(get(urlEqualTo("/gettest/empty")).atPriority(0).willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(404)));
        stubFor(get(urlEqualTo("/gettest/usertest")).atPriority(0).withBasicAuth("testuser", "testpass").willReturn(aResponse().withStatus(200).withBody("{ \"Test\" : \"Successful\" }")));
        stubFor(get(urlEqualTo("/gettest/usertest")).atPriority(10).willReturn(aResponse().withStatus(400).withBody("{ \"Authentication\" : \"UnSuccessful\" }")));
        stubFor(get(urlEqualTo("/gettest/json")).atPriority(0).withHeader("Accept", containing("application/json")).willReturn(aResponse().withStatus(200).withBody("{ \"Test\" : \"Successful\" }")
                                                                                                                                          .withHeader("Content-Type", "application/json")));
        stubFor(get(urlEqualTo("/gettest/json")).atPriority(0).withHeader("Accept", notMatching("application/json")).willReturn(aResponse().withStatus(406).withBody("{ \"Test\" : \"UnSuccessful\" }")
                                                                                                                                           .withHeader("Content-Type", "application/json")));
    }

    @Test
    public void testSimpleGet() {
        builder.url("http://localhost:8089/gettest/first");

        WellRestedRequest request1 = builder.build();
        WellRestedRequest.GetRequest getTest = request1.get();
        WellRestedResponse response1 = getTest.submit();

        String responseRef = "{ \"Test\" : \"Successful\" }";

        assertEquals(response1.getServerResponse(), responseRef);
        assertEquals(response1.getStatusCode(), 200);
    }

    @Test
    public void testNullGet() {
        WellRestedResponse response1 = builder.url("http://localhost:8089/gettest/empty").build().get().submit();
        assertEquals(response1.getStatusCode(), 404);
    }

    @Test
    public void testCredentialsGet() {
        builder.url("http://localhost:8089/gettest/usertest");

        UsernamePasswordCredentials testCredentials = new UsernamePasswordCredentials("testuser", "testpass".toCharArray());
        builder.credentials(testCredentials);

        WellRestedResponse response1 = builder.build().get().submit();

        System.out.println(response1.getServerResponse());

        assertEquals(200, response1.getStatusCode());
    }

    @Test
    public void testSecondCredentialsGet() {
        builder.url("http://localhost:8089/gettest/usertest");

        builder.credentials("testuser", "testpass");

        WellRestedResponse response1 = builder.build().get().submit();

        System.out.println(response1.getServerResponse());

        assertEquals(200, response1.getStatusCode());
    }

    @Test
    public void testThirdCredentialsGet() {
        builder.url("http://localtest.me:8089/gettest/usertest");

        builder.disableCookiesForAuthRequests();
        builder.credentials("testuser", "testpass");

        WellRestedResponse response1 = builder.build().get().submit();

        assertEquals(200, response1.getStatusCode());
    }

    @Test
    public void testFalseAcceptHeader() {
        Header acceptHeader = new BasicHeader("Accept", "image/jpeg");
        List<Header> headers = new ArrayList<>();
        headers.add(acceptHeader);

        WellRestedResponse response1 = builder.url("http://localhost:8089/gettest/json").build().get().headers(headers).submit();

        assertEquals(406, response1.getStatusCode());
    }

    @Test
    public void testTrueAcceptHeader() {
        Header acceptHeader = new BasicHeader("Accept", "application/json");
        List<Header> headers = new ArrayList<>();
        headers.add(acceptHeader);

        WellRestedResponse response1 = builder.url("http://localhost:8089/gettest/json").build().get().headers(headers).submit();

        assertEquals(200, response1.getStatusCode());
        assertEquals("{ \"Test\" : \"Successful\" }", response1.getServerResponse());
    }

    @Test
    public void testHeaderSupport() {
        builder.url("http://localhost:8089/gettest/json");

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "application/json");

        WellRestedResponse response1 = builder.build().get().headers(headers).submit();

        assertEquals(200, response1.getStatusCode());
        assertEquals("{ \"Test\" : \"Successful\" }", response1.getServerResponse());
    }

    @Test
    public void testCookiesSent() {
        stubFor(get(urlMatching("/gettest/cookies/.*")).withBasicAuth("testuser", "testpass")
                                                       .willReturn(aResponse().withHeader("set-cookie", "test=1234; Domain=localhost; Path=/; HttpOnly").withStatus(200)));

        builder.url("http://localhost:8089/gettest/cookies/1");
        WellRestedResponse setCookieResponse = builder.credentials("testuser", "testpass").build().get().submit();

        assertEquals(200, setCookieResponse.getStatusCode());

        verify(getRequestedFor(urlMatching("/gettest/cookies/1")).withHeader("Authorization", containing("Basic")));

        builder.url("http://localhost:8089/gettest/cookies/2");
        WellRestedResponse getCookieResponse = builder.credentials("testuser", "testpass").build().get().submit();

        assertEquals(200, getCookieResponse.getStatusCode());

        verify(getRequestedFor(urlMatching("/gettest/cookies/2")).withCookie("test", equalTo("1234")).withHeader("Authorization", containing("Basic")));
    }

    @Test
    public void testCookiesNotSent() {
        stubFor(get(urlMatching("/gettest/cookies/.*")).withBasicAuth("testuser", "testpass")
                                                       .willReturn(aResponse().withHeader("set-cookie", "test=1234; Domain=localhost; Path=/; HttpOnly").withStatus(200)));

        builder.url("http://localhost:8089/gettest/cookies/1");
        builder.disableCookiesForAuthRequests();
        WellRestedResponse setCookieResponse = builder.credentials("testuser", "testpass").build().get().submit();

        assertEquals(200, setCookieResponse.getStatusCode());

        verify(getRequestedFor(urlMatching("/gettest/cookies/1")).withHeader("Authorization", containing("Basic")));

        builder.url("http://localhost:8089/gettest/cookies/2");
        builder.disableCookiesForAuthRequests();
        WellRestedResponse getCookieResponse = builder.credentials("testuser", "testpass").build().get().submit();

        assertEquals(200, getCookieResponse.getStatusCode());

        verify(getRequestedFor(urlMatching("/gettest/cookies/2")).withCookie("test", AbsentPattern.ABSENT).withHeader("Authorization", containing("Basic")));
    }

    @Test
    public void testDifferentAuthSent() {
        stubFor(get(urlMatching("/gettest/cookies/1")).withBasicAuth("testuser", "testpass").willReturn(aResponse().withStatus(200)));
        stubFor(get(urlMatching("/gettest/cookies/2")).withBasicAuth("testuser2", "testpass2").willReturn(aResponse().withStatus(200)));

        builder.url("http://localhost:8089/gettest/cookies/1");
        builder.disableCookiesForAuthRequests();
        WellRestedResponse setCookieResponse = builder.credentials("testuser", "testpass").build().get().submit();

        assertEquals(200, setCookieResponse.getStatusCode());

        verify(getRequestedFor(urlMatching("/gettest/cookies/1")).withHeader("Authorization", containing("Basic")));

        builder.url("http://localhost:8089/gettest/cookies/2");
        builder.disableCookiesForAuthRequests();
        WellRestedResponse getCookieResponse = builder.credentials("testuser2", "testpass2").build().get().submit();

        assertEquals(200, getCookieResponse.getStatusCode());

        verify(getRequestedFor(urlMatching("/gettest/cookies/2")).withHeader("Authorization", containing("Basic")));
    }
}
