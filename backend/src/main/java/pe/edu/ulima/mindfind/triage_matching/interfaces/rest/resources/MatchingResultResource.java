package pe.edu.ulima.mindfind.triage_matching.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.List;

/** Representación pública de un psicólogo dentro del ranking Top 5 (HU-05). */
public record MatchingResultResource(
        Long idPsicologo,
        String nombrePsicologo,
        String enfoqueTeorico,
        List<String> especialidades,
        List<String> modalidadesAtencion,
        BigDecimal tarifaPorSesion,
        String moneda,
        String disponibilidadHoraria,
        double porcentajeCompatibilidad,
        int posicionRanking,
        boolean esMatchParcial,
        List<String> razonesCompatibilidad) {
}
