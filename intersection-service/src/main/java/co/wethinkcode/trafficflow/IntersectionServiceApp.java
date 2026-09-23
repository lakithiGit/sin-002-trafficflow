package co.wethinkcode.trafficflow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class IntersectionServiceApp {

    private static final List<Intersection> intersections =
            new ArrayList<>();

    private static final ObjectMapper objectMapper =
            new ObjectMapper();

    public static void main(String[] args) {

        loadIntersections();

        Javalin app = Javalin.create().start(7021);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/intersections", ctx ->
                ctx.json(intersections)
        );

        app.get("/intersections/{id}", ctx -> {

            String id = ctx.pathParam("id").toUpperCase();

            for (Intersection intersection : intersections) {

                if (intersection.getId().equals(id)) {
                    ctx.json(intersection);
                    return;
                }
            }

            ctx.status(404);
            ctx.result("Intersection not found");
        });
    }

    private static void loadIntersections() {

        try {

            HttpClient client =
                    HttpClient.newHttpClient();

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(
                                    "http://localhost:7020/intersections"
                            ))
                            .GET()
                            .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            intersections.addAll(
                    objectMapper.readValue(
                            response.body(),
                            new TypeReference<List<Intersection>>() {}
                    )
            );

            System.out.println(
                    "Loaded "
                            + intersections.size()
                            + " intersections."
            );

        } catch (Exception e) {

            System.err.println(
                    "Failed to load intersections from ingestion service."
            );

            e.printStackTrace();
        }
    }
}