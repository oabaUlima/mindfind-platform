package pe.edu.ulima.mindfind.triage_matching.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de entrada para registrar el cuestionario de triage (HU-04).
 * {@code modalidadPreferida} se recibe como {@code String} y las preguntas obligatorias se validan
 * en el dominio para garantizar exactamente el mensaje {@code "Debe responder esta pregunta para continuar"}.
 */
public record SubmitTriageEvaluationResource(
        @Schema(example = "Lucía Morales") String nombrePaciente,
        @Schema(example = "22") Integer edadPaciente,
        @Schema(example = "Siento mucha ansiedad académica y presión por los exámenes finales") String motivoConsulta,
        @Schema(example = "[\"ansiedad\", \"estrés laboral\"]") List<String> sintomasDeclarados,
        @Schema(example = "VIRTUAL", description = "Valores permitidos: PRESENCIAL, VIRTUAL") String modalidadPreferida,
        @Schema(example = "130.00", description = "Presupuesto máximo por sesión en PEN") BigDecimal presupuestoMaximo,
        @Schema(example = "Mañana") String disponibilidadPreferida,
        @Schema(example = "COGNITIVO_CONDUCTUAL", description = "Opcional: COGNITIVO_CONDUCTUAL, HUMANISTA, SISTEMICO, PSICODINAMICO, INTEGRATIVO") String enfoquePreferido,
        @Schema(example = "Prefiero una psicóloga con trato empático") String preferenciasProfesional,
        @Schema(example = "true") Boolean consentimientoCifrado,
        @Schema(example = "false", description = "Indicador opcional de riesgo inminente reportado") Boolean indicadorRiesgoInminente) {
}
