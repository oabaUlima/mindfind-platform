package pe.edu.ulima.mindfind.triage_matching.interfaces.rest.resources;

import java.util.List;

/** Representación pública del protocolo de crisis activado durante el triage (HU-06). */
public record CrisisProtocolResource(
        String palabraDetectada,
        String nivelRiesgo,
        boolean emparejamientoBloqueado,
        boolean alertaCerrada,
        List<RecursoEmergenciaResource> recursosEmergencia) {
}
