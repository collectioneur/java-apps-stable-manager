//package pl.agh.lab;
//
//import jakarta.persistence.EntityManagerFactory;
//import jakarta.persistence.Persistence;
//import pl.agh.lab.io.StableCsvUtil;
//import pl.agh.lab.model.Stable;
//import pl.agh.lab.service.StableService;
//
//import java.io.Console;
//import java.io.File;
//
//public class TestCsv {
//
//    public static void main(String[] args) throws Exception {
//        EntityManagerFactory emf = Persistence.createEntityManagerFactory("StablesPU");
//        StableService service = new StableService(emf);
//
//        Stable stable = service.getAllStables().get(0);
//
//
//        File csv = new File("stable_from_db.csv");
//        StableCsvUtil.exportStableToCsv(emf, stable, csv);
//
//        System.out.println("Wyeksportowano do: " + csv.getAbsolutePath());
//        emf.close();
//    }
//}
