package pe.edu.ulima.mindfind.triage_matching.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.ulima.mindfind.triage_matching.infrastructure.persistence.jpa.entities.EvaluacionTriageJpaEntity;

/** Repositorio Spring Data JPA de {@link EvaluacionTriageJpaEntity}. */
public interface EvaluacionTriageJpaRepository extends JpaRepository<EvaluacionTriageJpaEntity, Long> {
}
