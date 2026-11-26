package pe.edu.ulima.mindfind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point. Modular monolith hosting the MindFind bounded contexts:
 * {@code identidad_acreditacion} and {@code triage_matching}, plus a purely technical {@code shared} package.
 */
@SpringBootApplication
public class MindFindPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(MindFindPlatformApplication.class, args);
    }
}
