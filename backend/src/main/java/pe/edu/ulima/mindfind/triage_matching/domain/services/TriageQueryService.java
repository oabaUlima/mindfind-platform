package pe.edu.ulima.mindfind.triage_matching.domain.services;

import pe.edu.ulima.mindfind.triage_matching.domain.model.aggregates.EvaluacionTriage;
import pe.edu.ulima.mindfind.triage_matching.domain.model.queries.GetMatchingResultsByEvaluationIdQuery;
import pe.edu.ulima.mindfind.triage_matching.domain.model.queries.GetTriageEvaluationByIdQuery;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.ResultadoCompatibilidad;

import java.util.List;
import java.util.Optional;

/** Puerto de aplicación para consultas de {@code triage_matching}. */
public interface TriageQueryService {

    Optional<EvaluacionTriage> handle(GetTriageEvaluationByIdQuery query);

    List<ResultadoCompatibilidad> handle(GetMatchingResultsByEvaluationIdQuery query);
}
