package pe.edu.ulima.mindfind.identidad_acreditacion.domain.services;

import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.aggregates.Psicologo;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.queries.GetAllPsychologistsQuery;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.queries.GetPsychologistByIdQuery;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.queries.GetVerifiedPsychologistsQuery;

import java.util.List;
import java.util.Optional;

public interface PsicologoQueryService {

    List<Psicologo> handle(GetAllPsychologistsQuery query);

    Optional<Psicologo> handle(GetPsychologistByIdQuery query);

    List<Psicologo> handle(GetVerifiedPsychologistsQuery query);
}
