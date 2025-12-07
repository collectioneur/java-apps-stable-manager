package pl.agh.lab.io;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import pl.agh.lab.model.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class StableCsvUtil {

    private static final String DATE_PATTERN = "yyyy-MM-dd";


    public static void exportStableToCsv(EntityManagerFactory emf, Stable stable, File file) throws IOException {
        if (stable.getId() == null) {
            throw new IllegalArgumentException("Stable must be persisted (id != null) before export");
        }
        List<Horse> horses;

        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Horse> query = em.createQuery(
                    "SELECT h FROM Horse h " +
                            "WHERE h.stable.id = :sid " +
                            "ORDER BY LOWER(h.name), LOWER(h.breed), h.age",
                    Horse.class
            );
            query.setParameter("sid", stable.getId());
            horses = query.getResultList();
        } finally {
            em.close();
        }

        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {

            pw.println("name,breed,type,status,age,price,weightKg,heightCm,microchipId,acquisitionDate");

            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.ROOT);

            for (Horse h : horses) {
                String dateStr = sdf.format(h.getAcquisitionDate());

                pw.printf(Locale.ROOT,
                        "%s,%s,%s,%s,%d,%.2f,%.1f,%.1f,%s,%s%n",
                        escape(h.getName()),
                        escape(h.getBreed()),
                        h.getType().name(),
                        h.getStatus().name(),
                        h.getAge(),
                        h.getPrice(),
                        h.getWeightKg(),
                        h.getHeightCm(),
                        escape(h.getMicrochipId()),
                        dateStr
                );
            }
        }
    }

    private static String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static void importStableFromCsv(Stable stable, File file) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.ROOT);

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {

            String line = br.readLine();
            if (line == null) {
                return;
            }

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                List<String> tokens = parseCsvLine(line);
                if (tokens.size() < 10) {
                    System.err.println("Invalid CSV line: " + line);
                    continue;
                }

                String name        = tokens.get(0);
                String breed       = tokens.get(1);
                HorseType type     = HorseType.valueOf(tokens.get(2));
                HorseCondition status = HorseCondition.valueOf(tokens.get(3));
                int age           = Integer.parseInt(tokens.get(4));
                double price      = Double.parseDouble(tokens.get(5));
                double weightKg   = Double.parseDouble(tokens.get(6));
                double heightCm   = Double.parseDouble(tokens.get(7));
                String microchipId = tokens.get(8);
                String dateStr    = tokens.get(9);

                Date acquisitionDate;
                try {
                    acquisitionDate = sdf.parse(dateStr);
                } catch (Exception e) {
                    acquisitionDate = new Date();
                }

                Horse h = new Horse(
                        name,
                        breed,
                        type,
                        status,
                        age,
                        price,
                        weightKg,
                        heightCm,
                        microchipId,
                        acquisitionDate
                );

                stable.addHorse(h);
            }
        }
    }


    private static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '\"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                        sb.append('\"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '\"') {
                    inQuotes = true;
                } else if (c == ',') {
                    result.add(sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
        }
        result.add(sb.toString());
        return result;
    }
}
