package pe.edu.ulima.mindfind.triage_matching.domain.model.commands;

import java.util.List;

/** Comando para verificación rápida de indicadores de crisis en tiempo real durante el cuestionario. */
public record CheckCrisisKeywordsCommand(
        String motivoConsulta,
        List<String> sintomasDeclarados,
        String preferenciasProfesional,
        boolean indicadorRiesgoInminente) {
}
