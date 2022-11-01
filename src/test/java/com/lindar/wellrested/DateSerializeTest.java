package com.lindar.wellrested;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.lindar.wellrested.json.GsonJsonMapper;
import com.lindar.wellrested.model.DateEntry;
import com.lindar.wellrested.util.DateDeserializer;
import com.lindar.wellrested.util.LongDateSerializer;
import com.lindar.wellrested.util.StringDateSerializer;
import com.lindar.wellrested.vo.WellRestedResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@ExtendWith(TestEnvironment.class)
public class DateSerializeTest {
    private final WellRestedRequestBuilder builder = new WellRestedRequestBuilder();

    @Test
    public void stringSerializeTest() {

        stubFor(post("/tests/dateString").atPriority(0).withRequestBody(matching("\\{\"date\":\"2018-02-01T01:01:00\",\"body\":\"Body Data\",\"id\":1\\}"))
                                         .willReturn(aResponse().withBody("Test Successful: Date Serialized").withStatus(201)));

        Calendar cal = Calendar.getInstance();
        cal.set(2018, 1, 1, 1, 1, 0);

        DateEntry data = new DateEntry();
        data.setBody("Body Data");
        data.setDate(cal.getTime());
        data.setId(1);

        builder.url("http://localhost:8089/tests/dateString");
        builder.jsonMapper(new GsonJsonMapper.Builder()
                                   .dateSerializer(new StringDateSerializer())
                                   .dateDeserializer(new DateDeserializer())
                                   .build());

        WellRestedResponse response = builder.build().post().jsonContent(data).submit();

        verify(postRequestedFor(urlMatching("/tests/dateString")).withRequestBody(containing("2018-02-01T01:01:00")));
    }

    @Test
    public void longSerializeTest() {

        stubFor(post("/tests/dateString").atPriority(0).withRequestBody(matching("\\{\"date\":6000,\"body\":\"Body Data\",\"id\":1\\}"))
                                         .willReturn(aResponse().withBody("Test Successful: Date Serialized").withStatus(201)));

        DateEntry data = new DateEntry();
        data.setBody("Body Data");
        data.setDate(new Date(6000));
        data.setId(1);

        builder.url("http://localhost:8089/tests/dateString");

        builder.jsonMapper(new GsonJsonMapper.Builder()
                                   .dateSerializer(new LongDateSerializer())
                                   .dateDeserializer(new DateDeserializer())
                                   .build());

        WellRestedResponse response = builder.build().post().jsonContent(data).submit();

        verify(postRequestedFor(urlMatching("/tests/dateString")).withRequestBody(containing("6000")));

    }

    @Test
    public void testStringSerializeFormat() {

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
    public void testDeserialize() {

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
    public void testFormattedDeserialize() {

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
    public void testLongDeserialize() {

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
    public void testBuilderDateFormatMethod() {

        stubFor(post("/tests/dateformat").willReturn(aResponse().withBody("Request Successful")));

        Calendar cal = Calendar.getInstance();
        cal.set(2018, 3, 2, 1, 1, 0);

        DateEntry data = new DateEntry();
        data.setBody("Body Data");
        data.setDate(cal.getTime());
        data.setId(1);

        WellRestedRequest request = builder.url("http://localhost:8089/tests/dateformat")
                                           .jsonMapper(new GsonJsonMapper.Builder()
                                                               .dateFormat("yyyy-dd-MM'T'HH-mm-ss")
                                                               .dateDeserializer(new DateDeserializer())
                                                               .build())
                                           .build();
        WellRestedResponse response1 = request.post().jsonContent(data).submit();

        verify(postRequestedFor(urlMatching("/tests/dateformat")).withRequestBody(containing("\"2018-02-04T01-01-00\"")));

    }


}

