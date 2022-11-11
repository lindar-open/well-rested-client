package com.lindar.wellrested;

import com.lindar.wellrested.model.PHEntry;
import com.lindar.wellrested.vo.WellRestedResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(TestEnvironment.class)
public class PatchRequestTest {
    private static final int    PORT = 8089;
    private static final String HOST = "http://localhost:" + PORT;

    private final WellRestedRequestBuilder builder = new WellRestedRequestBuilder();

    @Test
    public void testSimplePatch() {
        stubFor(patch(urlEqualTo("/patch-test/first"))
                        .atPriority(0)
                        .withRequestBody(containing("{\"Input\": \"True\"}"))
                        .willReturn(aResponse().withStatus(200).withBody("{ \"Test\" : \"Successful\" }").withHeader("Content-Type", "application/json")));

        stubFor(patch(urlMatching(".*/.*"))
                        .atPriority(1)
                        .willReturn(aResponse().withStatus(404).withBody("{ \"Test\" : \"Unsuccessful\" }")));

        final WellRestedResponse response = builder.url(HOST + "/patch-test/first").build().patch().jsonContent("{\"Input\": \"True\"}").submit();

        final String responseRef = "{ \"Test\" : \"Successful\" }";

        assertEquals(200, response.getStatusCode());
        assertEquals(responseRef, response.getServerResponse());
    }

    @Test
    public void testNullPatch() {
        stubFor(patch(urlEqualTo("/patch-test/first"))
                        .atPriority(0)
                        .withRequestBody(containing("{\"Input\": \"True\"}"))
                        .willReturn(aResponse().withStatus(200).withBody("{ \"Test\" : \"Successful\" }").withHeader("Content-Type", "application/json")));

        stubFor(patch(urlMatching(".*/.*")).atPriority(1).willReturn(aResponse().withStatus(404).withBody("{ \"Test\" : \"Unsuccessful\" }")));

        final WellRestedResponse response = builder.url(HOST + "/patch-test/last").build().patch().jsonContent("{\"Input\": \"True\"}").submit();

        final String responseRef = "{ \"Test\" : \"Unsuccessful\" }";

        assertEquals(404, response.getStatusCode());
        assertEquals(responseRef, response.getServerResponse());
    }

    @Test
    public void testPatchHeaders() {
        stubFor(patch(urlEqualTo("/patch-test/first"))
                        .atPriority(0)
                        .withHeader("Accept", matching("application/json"))
                        .withRequestBody(containing("{\"Input\": \"True\"}"))
                        .willReturn(aResponse().withStatus(200).withBody("{ \"Test\" : \"Successful\" }").withHeader("Content-Type", "application/json")));

        final BasicHeader header1 = new BasicHeader("Accept", "application/json");
        final List<Header> headers = new ArrayList<>();
        headers.add(header1);

        builder.url(HOST + "/patch-test/first").build().patch().headers(headers).jsonContent("{\"Input\": \"True\"}").submit();

        verify(patchRequestedFor(urlMatching("/patch-test/first")).withHeader("Accept", matching("application/json")));
    }

    @Test
    public void testPatchJsonContent() {
        final PHEntry patchData = PHEntry.builder()
                                         .body("Test Body")
                                         .id(1)
                                         .title("Test Title")
                                         .userId(1)
                                         .build();

        stubFor(patch(urlEqualTo("/patch-test/first"))
                        .atPriority(0)
                        .withRequestBody(containing("{\"userId\":1,\"id\":1,\"title\":\"Test Title\",\"body\":\"Test Body\"}"))
                        .willReturn(aResponse().withStatus(200).withBody("{ \"Test\" : \"Successful\" }").withHeader("Content-Type", "application/json")));

        builder.url(HOST + "/patch-test/first").build().patch().jsonContent(patchData).submit();

        verify(patchRequestedFor(urlMatching("/patch-test/first")).withRequestBody(containing("{\"userId\":1,\"id\":1,\"title\":\"Test Title\",\"body\":\"Test Body\"}")));
    }

}
