package pe.edu.ulima.mindfind.identidad_acreditacion.interfaces.acl;

import org.springframework.stereotype.Service;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.aggregates.Psicologo;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.queries.GetPsychologistByIdQuery;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.queries.GetVerifiedPsychologistsQuery;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.EstadoVerificacion;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.services.PsicologoQueryService;

import java.util.List;
import java.util.Optional;

/**
 * Anti-Corruption Layer (lado proveedor): única puerta que otros Bounded Contexts
 * (como {@code triage_matching}) pueden usar para consultar psicólogos verificados.
 */
@Service
public class IdentityContextFacade {

    private final PsicologoQueryService psicologoQueryService;

    public IdentityContextFacade(PsicologoQueryService psicologoQueryService) {
        this.psicologoQueryService = psicologoQueryService;
    }

    /** Devuelve exclusivamente los psicólogos en estado {@code VERIFICADO}. */
    public List<VerifiedPsychologistDto> fetchVerifiedPsychologists() {
        return psicologoQueryService.handle(new GetVerifiedPsychologistsQuery()).stream()
                .filter(psicologo -> psicologo.getEstadoVerificacion() == EstadoVerificacion.VERIFICADO)
                .map(this::toDto)
                .toList();
    }

    public Optional<VerifiedPsychologistDto> fetchVerifiedPsychologistById(Long id) {
        return psicologoQueryService.handle(new GetPsychologistByIdQuery(id))
                .filter(psicologo -> psicologo.getEstadoVerificacion() == EstadoVerificacion.VERIFICADO)
                .map(this::toDto);
    }

    private VerifiedPsychologistDto toDto(Psicologo psicologo) {
        return new VerifiedPsychologistDto(
                psicologo.getId(),
                psicologo.getNombreCompleto(),
                psicologo.getEnfoqueTeorico().name(),
                psicologo.getEspecialidades(),
                psicologo.getModalidadesAtencion().stream().map(Enum::name).sorted().toList(),
                psicologo.getTarifaPorSesion().monto(),
                psicologo.getDisponibilidadHoraria(),
                true);
    }
}
