package pe.edu.ulima.mindfind.triage_matching.interfaces.rest.transform;

import pe.edu.ulima.mindfind.triage_matching.domain.model.aggregates.EvaluacionTriage;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.ProtocoloCrisis;
import pe.edu.ulima.mindfind.triage_matching.interfaces.rest.resources.CrisisProtocolResource;
import pe.edu.ulima.mindfind.triage_matching.interfaces.rest.resources.RecursoEmergenciaResource;
import pe.edu.ulima.mindfind.triage_matching.interfaces.rest.resources.TriageEvaluationResource;

/** Traduce el agregado {@link EvaluacionTriage} al DTO REST {@link TriageEvaluationResource}. */
public class TriageEvaluationResourceFromDomainAssembler {

    public static TriageEvaluationResource toResourceFromDomain(EvaluacionTriage evaluacion) {
        var protocolo = toCrisisProtocolResource(evaluacion.getProtocoloCrisis());
        var matches = evaluacion.getResultadosCompatibilidad().stream()
                .map(MatchingResultResourceFromDomainAssembler::toResourceFromDomain)
                .toList();

        return new TriageEvaluationResource(
                evaluacion.getId(),
                evaluacion.getNombrePaciente(),
                evaluacion.getEdadPaciente(),
                evaluacion.getMotivoConsulta(),
                evaluacion.getSintomasDeclarados(),
                evaluacion.getModalidadPreferida().name(),
                evaluacion.getPresupuestoMaximo().monto(),
                evaluacion.getPresupuestoMaximo().moneda(),
                evaluacion.getDisponibilidadPreferida(),
                evaluacion.getEnfoquePreferido(),
                evaluacion.getPreferenciasProfesional(),
                evaluacion.isConsentimientoCifrado(),
                evaluacion.getEstado().name(),
                evaluacion.getNivelRiesgo().name(),
                evaluacion.getFechaInicio(),
                evaluacion.getFechaFinalizacion(),
                protocolo,
                matches);
    }

    private static CrisisProtocolResource toCrisisProtocolResource(ProtocoloCrisis protocolo) {
        if (protocolo == null) {
            return null;
        }
        var recursos = protocolo.recursosEmergencia().stream()
                .map(r -> new RecursoEmergenciaResource(
                        r.nombreEntidad(),
                        r.lineaTelefonica(),
                        r.descripcion(),
                        r.disponibilidad()))
                .toList();
        return new CrisisProtocolResource(
                protocolo.palabraDetectada(),
                protocolo.nivelRiesgo().name(),
                protocolo.emparejamientoBloqueado(),
                protocolo.alertaCerrada(),
                recursos);
    }
}
