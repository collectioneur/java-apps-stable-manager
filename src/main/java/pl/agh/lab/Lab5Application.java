package pl.agh.lab;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import pl.agh.lab.service.StableService;
import pl.agh.lab.ui.LoginFrame;

import javax.swing.*;
import java.awt.*;

@SpringBootApplication
public class Lab5Application {

    public static void main(String[] args) {
        // Запускаем Spring, разрешая графический интерфейс (headless(false))
        new SpringApplicationBuilder(Lab5Application.class)
                .headless(false)
                .run(args);
    }

    // Этот код выполнится сразу после старта
    @Bean
    public CommandLineRunner run(StableService service) {
        return args -> {
            // Запуск Swing в правильном потоке
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame(service);
                loginFrame.setVisible(true);
            });
        };
    }
}