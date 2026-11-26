package pe.edu.ulima.mindfind.triage_matching.domain.model.commands;

import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.ModalidadPreferida;

import java.math.BigDecimal;
import java.util.List;

/** Comando para registrar y procesar una evaluación completa de triage (HU-04 / HU-05 / HU-06). */
public record SubmitTriageEvaluationCommand(
        String nombrePaciente,
        Integer edadPaciente,
        String motivoConsulta,
        List<String> sintomasDeclarados,
        ModalidadPreferida modalidadPreferida,
        BigDecimal presupuestoMaximo,
        String disponibilidadPreferida,
        String enfoquePreferido,
        String preferenciasProfesional,
        Boolean consentimientoCifrado,
        boolean indicadorRiesgoInminente) {
}
