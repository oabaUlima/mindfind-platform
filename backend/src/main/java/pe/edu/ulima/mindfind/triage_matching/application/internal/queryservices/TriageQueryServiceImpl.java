package pe.edu.ulima.mindfind.triage_matching.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ulima.mindfind.shared.application.exceptions.ResourceNotFoundException;
import pe.edu.ulima.mindfind.triage_matching.domain.model.aggregates.EvaluacionTriage;
import pe.edu.ulima.mindfind.triage_matching.domain.model.queries.GetMatchingResultsByEvaluationIdQuery;
import pe.edu.ulima.mindfind.triage_matching.domain.model.queries.GetTriageEvaluationByIdQuery;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.ResultadoCompatibilidad;
import pe.edu.ulima.mindfind.triage_matching.domain.repositories.EvaluacionTriageRepository;
import pe.edu.ulima.mindfind.triage_matching.domain.services.TriageQueryService;

import java.util.List;
import java.util.Optional;

/** Servicio de consultas de {@code triage_matching}. */
@Service
@Transactional(readOnly = true)
public class TriageQueryServiceImpl implements TriageQueryService {

    private final EvaluacionTriageRepository evaluacionTriageRepository;

    public TriageQueryServiceImpl(EvaluacionTriageRepository evaluacionTriageRepository) {
        this.evaluacionTriageRepository = evaluacionTriageRepository;
    }

    @Override
    public Optional<EvaluacionTriage> handle(GetTriageEvaluationByIdQuery query) {
        return evaluacionTriageRepository.findById(query.evaluacionId());
    }

    @Override
    public List<ResultadoCompatibilidad> handle(GetMatchingResultsByEvaluationIdQuery query) {
        var evaluacion = evaluacionTriageRepository.findById(query.evaluacionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Evaluación de triage %d no encontrada".formatted(query.evaluacionId())));
        if (evaluacion.estaInterrumpidaPorCrisis()) {
            throw new IllegalStateException(
                    "El emparejamiento está bloqueado porque se detectó un indicador de riesgo crítico");
        }
        return evaluacion.getResultadosCompatibilidad();
    }
}
