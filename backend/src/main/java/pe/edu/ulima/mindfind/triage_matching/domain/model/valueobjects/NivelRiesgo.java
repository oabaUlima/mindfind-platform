package pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects;

/** Niveles de riesgo clínico detectados durante el triage. Solo {@code CRITICO} bloquea el matching. */
public enum NivelRiesgo {
    BAJO,
    MODERADO,
    ALTO,
    CRITICO
}
