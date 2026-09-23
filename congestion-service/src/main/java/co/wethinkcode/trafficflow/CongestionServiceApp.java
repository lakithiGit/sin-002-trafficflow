package co.wethinkcode.trafficflow;

import io.javalin.Javalin;

public class CongestionServiceApp {

    private static int congestionLevel = 0;

    public static void main(String[] args) {

        Javalin app = Javalin.create().start(7022);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/congestion", ctx -> {
            ctx.json(java.util.Map.of(
                    "level", congestionLevel
            ));
        });

        app.post("/congestion/{level}", ctx -> {

            int level;

            try {
                level = Integer.parseInt(
                        ctx.pathParam("level")
                );
            } catch (NumberFormatException e) {
                ctx.status(400);
                ctx.result("Congestion level must be an integer.");
                return;
            }

            if (level < 0 || level > 8) {
                ctx.status(400);
                ctx.result("Congestion level must be between 0 and 8.");
                return;
            }

            congestionLevel = level;

            ctx.json(java.util.Map.of(
                    "level", congestionLevel
            ));
        });
    }
}