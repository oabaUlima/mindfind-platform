package pe.edu.ulima.mindfind.identidad_acreditacion.interfaces.rest.transform;

import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.commands.RegisterPsychologistCommand;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.EnfoqueTeorico;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.ModalidadAtencion;
import pe.edu.ulima.mindfind.identidad_acreditacion.interfaces.rest.resources.RegisterPsychologistResource;

import java.util.List;

/** Traduce el DTO REST {@link RegisterPsychologistResource} al comando de dominio. */
public class RegisterPsychologistCommandFromResourceAssembler {

    public static RegisterPsychologistCommand toCommandFromResource(RegisterPsychologistResource resource) {
        return new RegisterPsychologistCommand(
                resource.nombreCompleto(),
                resource.correo(),
                resource.telefono(),
                resource.numeroColegiatura(),
                parseEnfoqueTeorico(resource.enfoqueTeorico()),
                resource.especialidades(),
                parseModalidades(resource.modalidadesAtencion()),
                resource.tarifaPorSesion(),
                resource.disponibilidadHoraria());
    }

    private static EnfoqueTeorico parseEnfoqueTeorico(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return EnfoqueTeorico.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Enfoque teórico inválido: '%s'. Valores permitidos: COGNITIVO_CONDUCTUAL, HUMANISTA, SISTEMICO, PSICODINAMICO, INTEGRATIVO"
                            .formatted(raw.trim()));
        }
    }

    private static List<ModalidadAtencion> parseModalidades(List<String> rawList) {
        if (rawList == null) {
            return List.of();
        }
        return rawList.stream()
                .filter(item -> item != null && !item.isBlank())
                .map(item -> {
                    try {
                        return ModalidadAtencion.valueOf(item.trim().toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        throw new IllegalArgumentException(
                                "Modalidad de atención inválida: '%s'. Valores permitidos: PRESENCIAL, VIRTUAL"
                                        .formatted(item.trim()));
                    }
                })
                .toList();
    }
}
