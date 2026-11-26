package pe.edu.ulima.mindfind.triage_matching.interfaces.rest.transform;

import pe.edu.ulima.mindfind.triage_matching.domain.model.commands.SubmitTriageEvaluationCommand;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.ModalidadPreferida;
import pe.edu.ulima.mindfind.triage_matching.interfaces.rest.resources.SubmitTriageEvaluationResource;

/** Traduce {@link SubmitTriageEvaluationResource} al comando de dominio. */
public class SubmitTriageEvaluationCommandFromResourceAssembler {

    public static SubmitTriageEvaluationCommand toCommandFromResource(SubmitTriageEvaluationResource resource) {
        return new SubmitTriageEvaluationCommand(
                resource.nombrePaciente(),
                resource.edadPaciente(),
                resource.motivoConsulta(),
                resource.sintomasDeclarados(),
                parseModalidadPreferida(resource.modalidadPreferida()),
                resource.presupuestoMaximo(),
                resource.disponibilidadPreferida(),
                resource.enfoquePreferido(),
                resource.preferenciasProfesional(),
                resource.consentimientoCifrado(),
                Boolean.TRUE.equals(resource.indicadorRiesgoInminente()));
    }

    private static ModalidadPreferida parseModalidadPreferida(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return ModalidadPreferida.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Modalidad preferida inválida: '%s'. Valores permitidos: PRESENCIAL, VIRTUAL"
                            .formatted(raw.trim()));
        }
    }
}
