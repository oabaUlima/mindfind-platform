package pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects;

/**
 * Resultado de la evaluación rápida o completa del detector de riesgo clínico.
 */
public record ResultadoDeteccionCrisis(
        boolean crisisDetectada,
        NivelRiesgo nivelRiesgo,
        String palabraDetectada) {
}
