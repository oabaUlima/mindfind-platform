package pe.edu.ulima.mindfind.triage_matching.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Representación pública completa de una evaluación de triage y su ranking Top 5 o protocolo de crisis. */
public record TriageEvaluationResource(
        Long id,
        String nombrePaciente,
        Integer edadPaciente,
        String motivoConsulta,
        List<String> sintomasDeclarados,
        String modalidadPreferida,
        BigDecimal presupuestoMaximo,
        String moneda,
        String disponibilidadPreferida,
        String enfoquePreferido,
        String preferenciasProfesional,
        boolean consentimientoCifrado,
        String estado,
        String nivelRiesgo,
        Instant fechaInicio,
        Instant fechaFinalizacion,
        CrisisProtocolResource protocoloCrisis,
        List<MatchingResultResource> resultadosCompatibilidad) {
}
