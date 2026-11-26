package pe.edu.ulima.mindfind.identidad_acreditacion.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.aggregates.Psicologo;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.queries.GetAllPsychologistsQuery;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.queries.GetPsychologistByIdQuery;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.queries.GetVerifiedPsychologistsQuery;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.EstadoVerificacion;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.repositories.PsicologoRepository;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.services.PsicologoQueryService;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PsicologoQueryServiceImpl implements PsicologoQueryService {

    private final PsicologoRepository psicologoRepository;

    public PsicologoQueryServiceImpl(PsicologoRepository psicologoRepository) {
        this.psicologoRepository = psicologoRepository;
    }

    @Override
    public List<Psicologo> handle(GetAllPsychologistsQuery query) {
        return psicologoRepository.findAll();
    }

    @Override
    public Optional<Psicologo> handle(GetPsychologistByIdQuery query) {
        return psicologoRepository.findById(query.psicologoId());
    }

    @Override
    public List<Psicologo> handle(GetVerifiedPsychologistsQuery query) {
        return psicologoRepository.findByEstadoVerificacion(EstadoVerificacion.VERIFICADO);
    }
}
