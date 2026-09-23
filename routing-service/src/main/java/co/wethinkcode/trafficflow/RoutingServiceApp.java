package co.wethinkcode.trafficflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RoutingServiceApp {

    private static final String INTERSECTION_SERVICE =
            "http://localhost:7021";

    private static final String CONGESTION_SERVICE =
            "http://localhost:7022";

    private static final HttpClient client =
            HttpClient.newHttpClient();

    private static final ObjectMapper mapper =
            new ObjectMapper();

    public static void main(String[] args) {

        Javalin app = Javalin.create().start(7023);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/route/{intersectionId}", ctx -> {

            String intersectionId =
                    ctx.pathParam("intersectionId");

            try {

                // 1. Validate the intersection.
                HttpResponse<String> intersectionResponse =
                        get(
                                INTERSECTION_SERVICE
                                        + "/intersections/"
                                        + intersectionId
                        );

                if (intersectionResponse.statusCode() == 404) {
                    ctx.status(404);
                    ctx.result("Unknown intersection");
                    return;
                }

                if (intersectionResponse.statusCode() != 200) {
                    ctx.status(502);
                    ctx.result(
                            "Intersection service returned an error."
                    );
                    return;
                }

                // 2. Get current congestion.
                HttpResponse<String> congestionResponse =
                        get(
                                CONGESTION_SERVICE
                                        + "/congestion"
                        );

                if (congestionResponse.statusCode() != 200) {
                    ctx.status(502);
                    ctx.result(
                            "Congestion service returned an error."
                    );
                    return;
                }

                JsonNode congestionJson =
                        mapper.readTree(
                                congestionResponse.body()
                        );

                int congestionLevel =
                        congestionJson
                                .get("level")
                                .asInt();

                // 3. Calculate estimated travel time.
                int travelTime =
                        10 + (congestionLevel * 5);

                ctx.json(java.util.Map.of(
                        "intersectionId", intersectionId,
                        "congestionLevel", congestionLevel,
                        "estimatedTravelTimeMinutes",
                        travelTime
                ));

            } catch (Exception e) {

                ctx.status(502);
                ctx.result(
                        "Unable to reach required service."
                );
            }
        });
    }

    private static HttpResponse<String> get(
            String url) throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

        return client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }
}
