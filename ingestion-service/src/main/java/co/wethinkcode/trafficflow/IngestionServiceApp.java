package co.wethinkcode.trafficflow;

import com.opencsv.CSVReader;
import io.javalin.Javalin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class IngestionServiceApp {

    private static final List<Intersection> intersections =
            new ArrayList<>();

    public static void main(String[] args) {

        loadIntersections();

        Javalin app = Javalin.create().start(7020);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/intersections", ctx -> ctx.json(intersections));
    }

    private static void loadIntersections() {

        try {

            InputStream input =
                    IngestionServiceApp.class
                            .getClassLoader()
                            .getResourceAsStream(
                                    "intersections-legacy.csv"
                            );

            CSVReader reader =
                    new CSVReader(new InputStreamReader(input));

            reader.readNext(); // skip header

            String[] row;

            while ((row = reader.readNext()) != null) {

                String id = cleanId(row[0]);
                String district = cleanDistrict(row[1]);
                String signalType = cleanSignalType(row[2]);
                Boolean active = cleanActive(row[3]);

                addOrUpdateIntersection(
                        new Intersection(
                                id,
                                district,
                                signalType,
                                active
                        )
                );
            }

            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String cleanId(String value) {

        return value
                .trim()
                .toUpperCase();
    }

    private static String cleanDistrict(String value) {

        value = value.trim();
        value = value.replaceAll("\\s+", " ");

        if (value.isEmpty()
                || value.equalsIgnoreCase("N/A")
                || value.equalsIgnoreCase("TBD")
                || value.equalsIgnoreCase("unknown")
                || value.equals("-")
                || value.equalsIgnoreCase("NaN")) {

            return null;
        }

        String[] words =
                value.toLowerCase().split(" ");

        StringBuilder result =
                new StringBuilder();

        for (String word : words) {

            result.append(
                    Character.toUpperCase(word.charAt(0))
            );

            result.append(word.substring(1));
            result.append(" ");
        }

        return result.toString().trim();
    }

    private static String cleanSignalType(String value) {

        value = value.trim();

        if (value.isEmpty()
                || value.equalsIgnoreCase("N/A")
                || value.equalsIgnoreCase("TBD")
                || value.equalsIgnoreCase("unknown")
                || value.equals("-")
                || value.equalsIgnoreCase("NaN")) {

            return null;
        }

        value = value.toLowerCase();

        if (value.equals("4-way")) {
            return "4-way";
        }

        return value;
    }

    private static Boolean cleanActive(String value) {

        value = value.trim().toLowerCase();

        if (value.equals("y")
                || value.equals("yes")
                || value.equals("1")
                || value.equals("true")) {

            return true;
        }

        if (value.equals("n")
                || value.equals("no")
                || value.equals("0")
                || value.equals("false")) {

            return false;
        }

        return null;
    }

    private static void addOrUpdateIntersection(
            Intersection newIntersection) {

        for (int i = 0; i < intersections.size(); i++) {

            Intersection existing =
                    intersections.get(i);

            if (existing.getId()
                    .equals(newIntersection.getId())) {

                return;
            }
        }

        intersections.add(newIntersection);
    }
}
