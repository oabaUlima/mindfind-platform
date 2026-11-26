package pe.edu.ulima.mindfind.triage_matching.domain.model.queries;

/** Query para obtener el ranking Top 5 de compatibilidad de una evaluación. */
public record GetMatchingResultsByEvaluationIdQuery(Long evaluacionId) {
}
