package pe.edu.ulima.mindfind.triage_matching.domain.services;

import pe.edu.ulima.mindfind.triage_matching.domain.model.aggregates.EvaluacionTriage;
import pe.edu.ulima.mindfind.triage_matching.domain.model.commands.CheckCrisisKeywordsCommand;
import pe.edu.ulima.mindfind.triage_matching.domain.model.commands.CloseCrisisAlertCommand;
import pe.edu.ulima.mindfind.triage_matching.domain.model.commands.SubmitTriageEvaluationCommand;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.ResultadoDeteccionCrisis;

/** Puerto de aplicación para comandos de {@code triage_matching}. */
public interface TriageCommandService {

    EvaluacionTriage handle(SubmitTriageEvaluationCommand command);

    ResultadoDeteccionCrisis handle(CheckCrisisKeywordsCommand command);

    EvaluacionTriage handle(CloseCrisisAlertCommand command);
}
