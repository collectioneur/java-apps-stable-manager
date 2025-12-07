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
public class Main {

    public static void main(String[] args) {
        new SpringApplicationBuilder(Main.class)
                .headless(false)
                .run(args);
    }
    
    @Bean
    public CommandLineRunner run(StableService service) {
        return args -> {
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame(service);
                loginFrame.setVisible(true);
            });
        };
    }
}