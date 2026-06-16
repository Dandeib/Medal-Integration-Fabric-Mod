package com.dandeib.client;

import com.dandeib.MedalIntegrationFabric;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Triggers a Medal recording via the local Medal HTTP API. The call is async so it never
 * blocks the game thread. Set PUBLIC_KEY/GAME_ID and the event fields to match Medal.
 */
public final class MedalClipTrigger {

    private static final boolean ENABLED = true;

    private static final String ENDPOINT = "http://localhost:12665/api/v1/event/invoke";

    private static final String PUBLIC_KEY = "pub_ynzhVRczaSIQJHueavujpqU827hguMHU";
    private static final String GAME_ID = "hAXdelx2t";

    private static final String EVENT_ID = "1";
    private static final String EVENT_NAME = "Player Kill";
    private static final int CLIP_DURATION = 60;
    private static final int CLIP_DELAY = 5;

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private MedalClipTrigger() {}

    public static void onKill(String victim) {
        if (!ENABLED) {
            return;
        }

        String json = """
                {
                  "eventId": "%s",
                  "eventName": "%s",
                  "triggerActions": ["SaveClip"],
                  "clipOptions": {
                    "duration": %d,
                    "captureDelayMs": %d
                  }
                }""".formatted(escape(EVENT_ID), escape(EVENT_NAME), CLIP_DURATION, CLIP_DELAY);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .timeout(Duration.ofSeconds(5))
                .header("publicKey", PUBLIC_KEY)
                .header("gameId", GAME_ID)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response ->
                        MedalIntegrationFabric.LOGGER.info(
                                "[Medal] Clip event sent (kill: {}). Response: {} {}",
                                victim, response.statusCode(), response.body()))
                .exceptionally(ex -> {
                    MedalIntegrationFabric.LOGGER.error(
                            "[Medal] Medal API unreachable (is Medal running?). Endpoint: {}",
                            ENDPOINT, ex);
                    return null;
                });
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
