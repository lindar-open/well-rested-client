package com.lindar.wellrested;

import com.lindar.wellrested.vo.WellRestedResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.NameValuePair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToXml;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

@ExtendWith(TestEnvironment.class)
public class HttpEntityTest {
    private final WellRestedRequestBuilder builder = new WellRestedRequestBuilder();

    @BeforeAll
    public static void setupConnections(){
        stubFor(post("/entityTest/xml")
                .withRequestBody(equalToXml("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                        "<XmlEntry id=\"1\">\n" +
                        "    <name>testName</name>\n" +
                        "    <title>testTitle</title>\n" +
                        "    <body>testBody</body>\n" +
                        "</XmlEntry>"))
                .willReturn(aResponse()
                        .withStatus(201).withBody("{ \"Test\" : \"Successful\" }")));
    }

    @Test
    public void testFirstFormPairsMethod(){
        stubFor(post("/entitytest/formpair").willReturn(aResponse().withBody("Request Successful")));

        NameValuePair entry1 = new NameValuePair() {
            @Override
            public String getName() {
                return "testName1";
            }

            @Override
            public String getValue() {
                return "testValue1";
            }
        };

        NameValuePair entry2 = new NameValuePair() {
            @Override
            public String getName() {
                return "testName2";
            }

            @Override
            public String getValue() {
                return "testValue2";
            }
        };

        List<NameValuePair> data = new ArrayList<>();
        data.add(entry1);
        data.add(entry2);

        WellRestedResponse response1 = builder.url("http://localhost:8089/entitytest/formpair").build().post().formParams(data, Charset.defaultCharset()).submit();

        verify(postRequestedFor(urlMatching("/entitytest/formpair")).withRequestBody(containing("testName1")));
        verify(postRequestedFor(urlMatching("/entitytest/formpair")).withRequestBody(containing("testName2")));
        verify(postRequestedFor(urlMatching("/entitytest/formpair")).withRequestBody(containing("testValue1")));
        verify(postRequestedFor(urlMatching("/entitytest/formpair")).withRequestBody(containing("testValue2")));
    }

    @Test
    public void testSecondFormPairsMethod(){
        stubFor(post("/entitytest/formpair").willReturn(aResponse().withBody("Request Successful")));

        NameValuePair entry1 = new NameValuePair() {
            @Override
            public String getName() {
                return "testName1";
            }

            @Override
            public String getValue() {
                return "testValue1";
            }
        };

        NameValuePair entry2 = new NameValuePair() {
            @Override
            public String getName() {
                return "testName2";
            }

            @Override
            public String getValue() {
                return "testValue2";
            }
        };

        List<NameValuePair> data = new ArrayList<>();
        data.add(entry1);
        data.add(entry2);

        WellRestedResponse response1 = builder.url("http://localhost:8089/entitytest/formpair").build().post().formParams(data).submit();

        verify(postRequestedFor(urlMatching("/entitytest/formpair")).withRequestBody(containing("testName1")));
        verify(postRequestedFor(urlMatching("/entitytest/formpair")).withRequestBody(containing("testName2")));
        verify(postRequestedFor(urlMatching("/entitytest/formpair")).withRequestBody(containing("testValue1")));
        verify(postRequestedFor(urlMatching("/entitytest/formpair")).withRequestBody(containing("testValue2")));
    }

    @Test
    public void testThirdFormPairsMethod(){
        stubFor(post("/entitytest/formpair").willReturn(aResponse().withBody("Request Successful")));

        Map<String, String> data = new HashMap<String, String>();
        data.put("testName1", "testValue1");
        data.put("testName2", "testValue2");

        WellRestedResponse response1 = builder.url("http://localhost:8089/entitytest/formpair").build().post().formParams(data).submit();

        verify(postRequestedFor(urlMatching("/entitytest/formpair")).withRequestBody(containing("testName1")));
        verify(postRequestedFor(urlMatching("/entitytest/formpair")).withRequestBody(containing("testName2")));
        verify(postRequestedFor(urlMatching("/entitytest/formpair")).withRequestBody(containing("testValue1")));
        verify(postRequestedFor(urlMatching("/entitytest/formpair")).withRequestBody(containing("testValue2")));
    }

    @Test
    public void testFirstFileMethod(){
        stubFor(post("/entitytest/file").willReturn(aResponse().withBody("Request Successful").withStatus(200)));

        File dataFile = new File("./src/main/resources/META-INF/files/testfile.txt");

        WellRestedResponse response1 = builder.url("http://localhost:8089/entitytest/file").build().post().file(dataFile, ContentType.MULTIPART_FORM_DATA).submit();

        verify(postRequestedFor(urlMatching("/entitytest/file")).withRequestBody(containing("Test string for file post")));
    }

    @Test
    public void testSecondFileMethod(){
        stubFor(post("/entitytest/file").willReturn(aResponse().withBody("Request Successful").withStatus(200)));

        File dataFile = new File("./src/main/resources/META-INF/files/testfile.txt");

        WellRestedResponse response1 = builder.url("http://localhost:8089/entitytest/file").build().post().file(dataFile).submit();

        verify(postRequestedFor(urlMatching("/entitytest/file")).withRequestBody(containing("Test string for file post")));
    }

}
