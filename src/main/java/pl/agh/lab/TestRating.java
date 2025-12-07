package pl.agh.lab;

import jakarta.persistence.*;
import pl.agh.lab.model.*;

import java.util.Date;

public class TestRating {
    public static void main(String[] args) {

        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("StablesPU");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        Horse h = new Horse(
                "Spirit",
                "Arabian",
                HorseType.GORACOKRWISTY,
                HorseCondition.ZDROWY,
                4,
                12000,
                480,
                160,
                "CHIP-SP-001",
                new Date()
        );

        em.persist(h);

        Rating r = new Rating(5, h, new Date(), "Very strong and healthy");
        em.persist(r);

        em.getTransaction().commit();
        em.close();
        emf.close();
    }
}
