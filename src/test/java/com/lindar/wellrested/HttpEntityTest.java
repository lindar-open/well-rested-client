package com.lindar.wellrested;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.lindar.wellrested.vo.WellRestedResponse;
import com.lindar.wellrested.xml.WellRestedXMLUtil;
import org.apache.http.NameValuePair;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

public class HttpEntityTest {

    WellRestedRequestBuilder builder;

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(8089);

    @Before
    public void setupConnections(){
        builder = new WellRestedRequestBuilder();

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

    @BeforeClass
    public static void waitToStart(){
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testXmlContent(){
        XMLEntry xmlData = new XMLEntry("testName", "testTitle", "testBody", 1);
        String xmlDataString = WellRestedXMLUtil.fromObjectToString(xmlData);

        WellRestedResponse response1 = builder.url("http://localhost:8089/entityTest/xml").build().post().xmlContent(xmlData).submit();
        WellRestedResponse response2 = builder.url("http://localhost:8089/entityTest/xml").build().post().xmlContent(xmlDataString).submit();


        assertEquals(201, response1.getStatusCode());
        assertEquals("{ \"Test\" : \"Successful\" }", response1.getServerResponse());
        assertEquals(201, response2.getStatusCode());
        assertEquals("{ \"Test\" : \"Successful\" }", response2.getServerResponse());
    }

    @Test
    public void testContentMethod(){
        XMLEntry xmlData = new XMLEntry("testName", "testTitle", "testBody", 1);
        String xmlDataString = WellRestedXMLUtil.fromObjectToString(xmlData);

        WellRestedResponse response1 = builder.url("http://localhost:8089/entityTest/xml").build().post().content(xmlDataString, ContentType.APPLICATION_XML).submit();

        assertEquals(201, response1.getStatusCode());
        assertEquals("{ \"Test\" : \"Successful\" }", response1.getServerResponse());
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
