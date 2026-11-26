package pe.edu.ulima.mindfind.identidad_acreditacion.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de entrada para el registro de psicólogos (HU-01).
 * {@code numeroColegiatura}, {@code enfoqueTeorico}, {@code especialidades}, {@code modalidadesAtencion}
 * y {@code tarifaPorSesion} se validan en el dominio para devolver exactamente los mensajes de negocio de HU-01.
 */
public record RegisterPsychologistResource(
        @Schema(example = "Dra. Sofía Ramírez León") @NotBlank String nombreCompleto,
        @Schema(example = "sofia.ramirez@mindfind.pe") @NotBlank String correo,
        @Schema(example = "991234567") @NotBlank String telefono,
        @Schema(example = "45678", description = "Numérico de 4 a 6 dígitos") String numeroColegiatura,
        @Schema(example = "COGNITIVO_CONDUCTUAL") String enfoqueTeorico,
        @Schema(example = "[\"ansiedad\", \"estrés laboral\"]") List<String> especialidades,
        @Schema(example = "[\"VIRTUAL\", \"PRESENCIAL\"]") List<String> modalidadesAtencion,
        @Schema(example = "120.00") BigDecimal tarifaPorSesion,
        @Schema(example = "Lunes a Viernes 15:00 - 20:00 (Tarde/Noche)") @NotBlank String disponibilidadHoraria) {
}
