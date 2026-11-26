package pe.edu.ulima.mindfind.triage_matching.interfaces.rest.resources;

import java.util.List;

/** DTO de salida para la verificación rápida de riesgo crítico en tiempo real. */
public record CrisisCheckResponseResource(
        boolean crisisDetectada,
        String nivelRiesgo,
        String palabraDetectada,
        boolean emparejamientoBloqueado,
        String mensajeAlerta,
        List<RecursoEmergenciaResource> recursosEmergencia) {
}
