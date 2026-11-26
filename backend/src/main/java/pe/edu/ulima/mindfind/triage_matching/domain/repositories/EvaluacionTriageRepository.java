package pe.edu.ulima.mindfind.triage_matching.domain.repositories;

import pe.edu.ulima.mindfind.triage_matching.domain.model.aggregates.EvaluacionTriage;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia para el agregado {@link EvaluacionTriage}. */
public interface EvaluacionTriageRepository {

    EvaluacionTriage save(EvaluacionTriage evaluacionTriage);

    Optional<EvaluacionTriage> findById(Long id);

    List<EvaluacionTriage> findAll();
}
