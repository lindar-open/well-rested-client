package com.lindar.wellrested;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.lindar.wellrested.json.GsonJsonMapper;
import com.lindar.wellrested.util.BasicExclusionStrategy;
import com.lindar.wellrested.vo.WellRestedResponse;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertEquals;

public class ExclusionTest {

    private WellRestedRequestBuilder builder = new WellRestedRequestBuilder();

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(8089);

    @Before
    public void setupTests(){
        builder = new WellRestedRequestBuilder();

        stubFor(post(urlMatching("/tests/serializer")).atPriority(0).withRequestBody(matching("\\{\"userId\":1,\"id\":1,\"body\":\"Post Body\"\\}")).willReturn(aResponse().withStatus(201).withBody("Fourth Test: Correct Excluded Body found")));
        stubFor(post(urlMatching("/tests/serializer")).atPriority(0).withRequestBody(matching("\\{\"userId\":1,\"id\":1,\"title\":\"Post Title\",\"body\":\"Post Body\"\\}")).willReturn(aResponse().withStatus(202).withBody("Fourth Test: Correct Non-Excluded Body found")));
        stubFor(post(urlMatching("/tests/serializer")).atPriority(0).withRequestBody(matching("\\{\"title\":\"Post Title\",\"body\":\"Post Body\"\\}")).willReturn(aResponse().withStatus(203).withBody("Fourth Test: Correct Excluded Classes Body found")));
        stubFor(post(urlMatching("/tests/serializer")).atPriority(5).willReturn(aResponse().withStatus(500).withBody("Test Failed: Incorrect Body")));
        stubFor(post(urlMatching("/tests/.*")).atPriority(10).willReturn(aResponse().withStatus(404).withBody("Post failed: Incorrect url")));
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
    public void testFieldExclusion(){
        PHEntry postData = new PHEntry();
        postData.setUserId(1);
        postData.setTitle("Post Title");
        postData.setBody("Post Body");
        postData.setId(1);

        List<String> excludeFields = new ArrayList<String>();
        excludeFields.add("title");

        builder.url("http://localhost:8089/tests/serializer");

        builder.jsonMapper(new GsonJsonMapper.Builder()
                                    .excludeFields(excludeFields)
                                    .build());


        WellRestedResponse response1 = builder.build().post().jsonContent(postData).submit();

        verify(postRequestedFor(urlMatching("/tests/serializer")).withRequestBody(matching("\\{\"userId\":1,\"id\":1,\"body\":\"Post Body\"\\}")));
    }

    @Test
    public void testNullFieldExclusion(){
        builder = new WellRestedRequestBuilder();
        builder.jsonMapper(new GsonJsonMapper.Builder()
                                   .excludeFields(new ArrayList<>())
                                   .build());

        PHEntry postData1 = new PHEntry();
        postData1.setUserId(1);
        postData1.setTitle("Post Title");
        postData1.setBody("Post Body");
        postData1.setId(1);

        List<String> excludeFields = new ArrayList<String>();
        excludeFields.add("entry");

        builder.url("http://localhost:8089/tests/serializer");

        builder.jsonMapper(new GsonJsonMapper.Builder()
                                    .excludeFields(excludeFields)
                                    .build());

        WellRestedResponse response = builder.build().post().jsonContent(postData1).submit();

        assertEquals(202, response.getStatusCode());
        assertEquals("Fourth Test: Correct Non-Excluded Body found", response.getServerResponse());
    }

    @Test
    public void testClassExclusion(){
        builder = new WellRestedRequestBuilder();

        PHEntry postData2 = new PHEntry();
        postData2.setUserId(1);
        postData2.setTitle("Post Title");
        postData2.setBody("Post Body");
        postData2.setId(1);

        Set<String> excludeClasses = new HashSet<>();
        excludeClasses.add("int");

        builder.url("http://localhost:8089/tests/serializer");

        builder.jsonMapper(new GsonJsonMapper.Builder()
                                    .excludeClasses(excludeClasses)
                                    .build());

        WellRestedResponse response2 = builder.build().post().jsonContent(postData2).submit();

        assertEquals(203, response2.getStatusCode());
        assertEquals("Fourth Test: Correct Excluded Classes Body found", response2.getServerResponse());
    }

    @Test
    public void testNullClassExclusion(){
        builder = new WellRestedRequestBuilder();

        PHEntry postData2 = new PHEntry();
        postData2.setUserId(1);
        postData2.setTitle("Post Title");
        postData2.setBody("Post Body");
        postData2.setId(1);

        Set<String> excludeClasses = new HashSet<>();
        excludeClasses.add("Gson");

        builder.url("http://localhost:8089/tests/serializer");

        builder.jsonMapper(new GsonJsonMapper.Builder()
                                   .excludeClasses(excludeClasses)
                                   .build());

        WellRestedResponse response2 = builder.build().post().jsonContent(postData2).submit();

        assertEquals(202, response2.getStatusCode());
        assertEquals("Fourth Test: Correct Non-Excluded Body found", response2.getServerResponse());
    }

    @Test
    public void testClassExclusionStrategy(){
        builder = new WellRestedRequestBuilder();

        PHEntry postData2 = new PHEntry();
        postData2.setUserId(1);
        postData2.setTitle("Post Title");
        postData2.setBody("Post Body");
        postData2.setId(1);

        Set<String> excludeClasses = new HashSet<>();
        excludeClasses.add("int");

        BasicExclusionStrategy exclude = new BasicExclusionStrategy(excludeClasses);

        builder.url("http://localhost:8089/tests/serializer");

        builder.jsonMapper(new GsonJsonMapper.Builder()
                                   .exclusionStrategy(exclude)
                                   .build());

        WellRestedResponse response2 = builder.build().post().jsonContent(postData2).submit();

        assertEquals(203, response2.getStatusCode());
        assertEquals("Fourth Test: Correct Excluded Classes Body found", response2.getServerResponse());
    }

    @Test
    public void testCustomiserExclusion(){
        PHEntry postData = new PHEntry();
        postData.setUserId(1);
        postData.setTitle("Post Title");
        postData.setBody("Post Body");
        postData.setId(1);

        builder.url("http://localhost:8089/tests/serializer");

        TestGsonCustomiser custom = new TestGsonCustomiser();
        builder.jsonMapper(new GsonJsonMapper.Builder()
                            .gsonCustomiser(custom)
                            .build());

        WellRestedResponse response1 = builder.build().post().jsonContent(postData).submit();

        verify(postRequestedFor(urlMatching("/tests/serializer")).withRequestBody(matching("\\{\"userId\":1,\"id\":1,\"body\":\"Post Body\"\\}")));
    }

}
