package pe.edu.ulima.mindfind.identidad_acreditacion.infrastructure.persistence.jpa.seeders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.commands.RegisterPsychologistCommand;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.commands.VerifyPsychologistCommand;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.EnfoqueTeorico;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.ModalidadAtencion;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.repositories.PsicologoRepository;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.services.PsicologoCommandService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Carga 6 psicólogos clínicos verificados al iniciar la aplicación (si la tabla está vacía)
 * para habilitar el motor de compatibilidad y ranking Top 5 desde el primer arranque.
 */
@Component
public class IdentidadDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(IdentidadDataSeeder.class);

    private final PsicologoRepository psicologoRepository;
    private final PsicologoCommandService commandService;

    public IdentidadDataSeeder(PsicologoRepository psicologoRepository, PsicologoCommandService commandService) {
        this.psicologoRepository = psicologoRepository;
        this.commandService = commandService;
    }

    @Override
    public void run(String... args) {
        if (psicologoRepository.count() > 0) {
            return;
        }

        var seeds = List.of(
                new RegisterPsychologistCommand(
                        "Dra. Valeria Mendoza Ríos",
                        "valeria.mendoza@mindfind.pe",
                        "987654321",
                        "10421",
                        EnfoqueTeorico.COGNITIVO_CONDUCTUAL,
                        List.of("ansiedad", "estrés laboral", "autoestima"),
                        List.of(ModalidadAtencion.VIRTUAL, ModalidadAtencion.PRESENCIAL),
                        new BigDecimal("120.00"),
                        "Lunes a Viernes 09:00 - 14:00 (Mañana)"),
                new RegisterPsychologistCommand(
                        "Dr. Alejandro Paredes Quispe",
                        "alejandro.paredes@mindfind.pe",
                        "987654322",
                        "23845",
                        EnfoqueTeorico.HUMANISTA,
                        List.of("depresión", "duelo", "autoestima"),
                        List.of(ModalidadAtencion.VIRTUAL),
                        new BigDecimal("90.00"),
                        "Lunes a Viernes 15:00 - 21:00 (Tarde/Noche)"),
                new RegisterPsychologistCommand(
                        "Dra. Camila Torres Salcedo",
                        "camila.torres@mindfind.pe",
                        "987654323",
                        "31904",
                        EnfoqueTeorico.SISTEMICO,
                        List.of("pareja", "ansiedad", "estrés laboral"),
                        List.of(ModalidadAtencion.PRESENCIAL, ModalidadAtencion.VIRTUAL),
                        new BigDecimal("150.00"),
                        "Martes a Sábado 10:00 - 18:00 (Mañana/Tarde)"),
                new RegisterPsychologistCommand(
                        "Dr. Sebastián Huamán Vega",
                        "sebastian.huaman@mindfind.pe",
                        "987654324",
                        "45120",
                        EnfoqueTeorico.PSICODINAMICO,
                        List.of("depresión", "duelo", "pareja"),
                        List.of(ModalidadAtencion.PRESENCIAL),
                        new BigDecimal("180.00"),
                        "Lunes, Miércoles y Viernes 16:00 - 20:00 (Tarde/Noche)"),
                new RegisterPsychologistCommand(
                        "Dra. Lucía Fernández Navarro",
                        "lucia.fernandez@mindfind.pe",
                        "987654325",
                        "58234",
                        EnfoqueTeorico.INTEGRATIVO,
                        List.of("ansiedad", "autoestima", "duelo"),
                        List.of(ModalidadAtencion.VIRTUAL),
                        new BigDecimal("70.00"),
                        "Lunes a Sábado 08:00 - 13:00 (Mañana)"),
                new RegisterPsychologistCommand(
                        "Dr. Diego Castillo Morales",
                        "diego.castillo@mindfind.pe",
                        "987654326",
                        "64912",
                        EnfoqueTeorico.COGNITIVO_CONDUCTUAL,
                        List.of("estrés laboral", "depresión", "ansiedad"),
                        List.of(ModalidadAtencion.PRESENCIAL, ModalidadAtencion.VIRTUAL),
                        new BigDecimal("130.00"),
                        "Lunes a Viernes 18:00 - 22:00 (Noche)")
        );

        for (var seed : seeds) {
            var registered = commandService.handle(seed);
            commandService.handle(new VerifyPsychologistCommand(registered.getId()));
        }

        log.info("[Identidad] DataSeeder completado: {} psicólogos verificados disponibles para MindFind.", seeds.size());
    }
}
