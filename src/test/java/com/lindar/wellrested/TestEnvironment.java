package com.lindar.wellrested;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

class TestEnvironment implements BeforeAllCallback {

    public static WireMockServer wireMockServer;

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        startIfRequired();
    }

    private void startIfRequired() {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(8089);
            wireMockServer.start();

            WireMock.configureFor(8089);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> wireMockServer.stop()));
        }
    }
}
