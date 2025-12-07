package pl.agh.lab.ui;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import pl.agh.lab.model.HorseCondition;
import pl.agh.lab.model.HorseType;
import pl.agh.lab.model.Stable;
import pl.agh.lab.service.HorseOperationException;
import pl.agh.lab.service.StableOperationException;
import pl.agh.lab.service.StableService;
import pl.agh.lab.service.ValidationException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.util.Date;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("StablesPU");
            StableService service = new StableService(emf);

            LoginFrame loginFrame = new LoginFrame(service);
            loginFrame.setVisible(true);
        });
    }
}
