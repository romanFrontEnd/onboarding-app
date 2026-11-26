package com.videotron.bsi.service.b.cucumber;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.Map;

public class WireMockExtensions implements QuarkusTestResourceLifecycleManager {

    private static WireMockServer wireMockServer;
    private static int port;

    @Override
    public Map<String, String> start() {
        // Dynamic port: fixed ports cause BindException conflicts
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        port = wireMockServer.port();

        return Map.of();
    }

    @Override
    public void stop() {
        if (wireMockServer != null) {
            wireMockServer.stop();
            wireMockServer = null;
        }
    }

    // Static access for step definitions — ALWAYS use instance methods
    public static WireMockServer getWireMockServer() {
        return wireMockServer;
    }

    public static boolean isRunning() {
        return wireMockServer != null && wireMockServer.isRunning();
    }

    public static void resetAll() {
        if (isRunning()) {
            wireMockServer.resetAll();
        }
    }

    /** Simulates a refused connection: nothing listens on the port any more. */
    public static void stopServer() {
        if (isRunning()) {
            wireMockServer.stop();
        }
    }

    /** Restarts the server on the SAME port so the configured client URL stays valid. */
    public static void restartIfStopped() {
        if (wireMockServer != null && !wireMockServer.isRunning()) {
            wireMockServer = new WireMockServer(WireMockConfiguration.options().port(port));
            wireMockServer.start();
        }
    }
}
