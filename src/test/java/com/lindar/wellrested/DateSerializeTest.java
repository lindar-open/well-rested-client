package com.lindar.wellrested;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.lindar.wellrested.util.DateDeserializer;
import com.lindar.wellrested.util.LongDateSerializer;
import com.lindar.wellrested.util.StringDateSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.lindar.wellrested.vo.WellRestedResponse;
import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class DateSerializeTest {

    WellRestedRequestBuilder builder;

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
    public void stringSerializeTest(){

        stubFor(post("/tests/dateString").atPriority(0).withRequestBody(matching("\\{\"date\":\"2018-02-01T01:01:00\",\"body\":\"Body Data\",\"id\":1\\}")).willReturn(aResponse().withBody("Test Successful: Date Serialized").withStatus(201)));

        Calendar cal = Calendar.getInstance();
        cal.set(2018, 1, 1, 1, 1, 0);

        DateEntry data = new DateEntry();
        data.setBody("Body Data");
        data.setDate(cal.getTime());
        data.setId(1);

        builder.url("http://localhost:8089/tests/dateString");

        builder.dateSerializer(new StringDateSerializer());
        builder.dateDeserializer(new DateDeserializer());

        WellRestedResponse response = builder.build().post().jsonContent(data).submit();

        verify(postRequestedFor(urlMatching("/tests/dateString")).withRequestBody(containing("2018-02-01T01:01:00")));

    }

    @Test
    public void longSerializeTest(){

        stubFor(post("/tests/dateString").atPriority(0).withRequestBody(matching("\\{\"date\":6000,\"body\":\"Body Data\",\"id\":1\\}")).willReturn(aResponse().withBody("Test Successful: Date Serialized").withStatus(201)));

        DateEntry data = new DateEntry();
        data.setBody("Body Data");
        data.setDate(new Date(6000));
        data.setId(1);

        builder.url("http://localhost:8089/tests/dateString");

        builder.dateSerializer(new LongDateSerializer());
        builder.dateDeserializer(new DateDeserializer());

        WellRestedResponse response = builder.build().post().jsonContent(data).submit();

        verify(postRequestedFor(urlMatching("/tests/dateString")).withRequestBody(containing("6000")));

    }

    @Test
    public void testStringSerializeFormat(){

        Calendar cal = Calendar.getInstance();
        cal.set(2018, 3, 2, 1, 1, 0);

        Date dateData = cal.getTime();

        String newFormat = "yyyy-dd-MM'T'HH-mm-ss";

        StringDateSerializer serializer = new StringDateSerializer(newFormat);

        JsonElement serializedDate = serializer.serialize(dateData, mock(Type.class), mock(JsonSerializationContext.class));

        String reference = "\"2018-02-04T01-01-00\"";

        assertEquals(reference, serializedDate.toString());

    }

    @Test
    public void testDeserialize(){

        Calendar cal = Calendar.getInstance();
        cal.set(2018, 3, 2, 1, 1, 0);

        Date dateData = cal.getTime();

        StringDateSerializer serializer = new StringDateSerializer();

        JsonElement serializedDate = serializer.serialize(dateData, mock(Type.class), mock(JsonSerializationContext.class));

        DateDeserializer deSerializer = new DateDeserializer();

        Date convertedDate = deSerializer.deserialize(serializedDate, mock(Type.class), mock(JsonDeserializationContext.class));

        assertEquals(dateData.toString(), convertedDate.toString());

    }

    @Test
    public void testFormattedDeserialize(){

        Calendar cal = Calendar.getInstance();
        cal.set(2018, 3, 2, 1, 1, 0);

        Date dateData = cal.getTime();

        String newFormat = "yyyy-dd-MM'T'HH-mm-ss";

        StringDateSerializer serializer = new StringDateSerializer(newFormat);

        JsonElement serializedDate = serializer.serialize(dateData, mock(Type.class), mock(JsonSerializationContext.class));

        DateDeserializer deSerializer = new DateDeserializer(newFormat);

        Date convertedDate = deSerializer.deserialize(serializedDate, mock(Type.class), mock(JsonDeserializationContext.class));

        assertEquals(dateData.toString(), convertedDate.toString());

    }

    @Test
    public void testLongDeserialize(){

        Date dateData = new Date(60000);

        String newFormat = "yyyy-dd-MM'T'HH-mm-ss";
        List<String> formats = new ArrayList<String>();
        formats.add(newFormat);

        LongDateSerializer serializer = new LongDateSerializer();

        JsonElement serializedDate = serializer.serialize(dateData, mock(Type.class), mock(JsonSerializationContext.class));

        DateDeserializer deSerializer = new DateDeserializer(formats);

        Date convertedDate = deSerializer.deserialize(serializedDate, mock(Type.class), mock(JsonDeserializationContext.class));

        assertEquals(dateData, convertedDate);

    }

    @Test
    public void testBuilderDateFormatMethod(){

        stubFor(post("/tests/dateformat").willReturn(aResponse().withBody("Request Successful")));

        Calendar cal = Calendar.getInstance();
        cal.set(2018, 3, 2, 1, 1, 0);

        DateEntry data = new DateEntry();
        data.setBody("Body Data");
        data.setDate(cal.getTime());
        data.setId(1);

        WellRestedRequest request = builder.url("http://localhost:8089/tests/dateformat").dateFormat("yyyy-dd-MM'T'HH-mm-ss").build();
        WellRestedResponse response1 = request.post().jsonContent(data).submit();

        verify(postRequestedFor(urlMatching("/tests/dateformat")).withRequestBody(containing("\"2018-02-04T01-01-00\"")));

    }


}

