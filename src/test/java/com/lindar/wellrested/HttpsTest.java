package com.lindar.wellrested;

import com.lindar.wellrested.vo.WellRestedResponse;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpsTest {
    private final WellRestedRequestBuilder builder = new WellRestedRequestBuilder();

    @ParameterizedTest
    @CsvSource({
        "correct-password, 200",
        "incorrect-password, 401"
    })
    public void httpsWithCredentials(String password, int expectedStatusCode) {
        builder.url("https://httpbin.org/basic-auth/username/correct-password");
        builder.credentials(new UsernamePasswordCredentials("username", password.toCharArray()));

        WellRestedResponse response = builder.build().get().submit();

        assertEquals(expectedStatusCode, response.getStatusCode());
    }
}
