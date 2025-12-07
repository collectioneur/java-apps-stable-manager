package pl.agh.lab;

import jakarta.persistence.*;
import pl.agh.lab.model.*;
import java.util.Date;

public class TestDB {
    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("StablesPU");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        Stable st = new Stable("Demo Stable", 5);

        Horse h = new Horse(
                "Arrow",
                "Arabian",
                HorseType.GORACOKRWISTY,
                HorseCondition.ZDROWY,
                3,
                12000,
                510,
                163,
                "CHIP-99",
                new Date()
        );

        st.addHorse(h);

        em.persist(st);

        em.getTransaction().commit();
        em.close();
        emf.close();
    }
}
