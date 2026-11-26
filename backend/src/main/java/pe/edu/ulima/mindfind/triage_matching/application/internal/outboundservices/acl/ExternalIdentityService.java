package pe.edu.ulima.mindfind.triage_matching.application.internal.outboundservices.acl;

import org.springframework.stereotype.Service;
import pe.edu.ulima.mindfind.identidad_acreditacion.interfaces.acl.IdentityContextFacade;
import pe.edu.ulima.mindfind.identidad_acreditacion.interfaces.acl.VerifiedPsychologistDto;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.ModalidadPreferida;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.PsicologoRef;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ACL, lado consumidor (Anti-Corruption Layer).
 * Única clase en {@code triage_matching} que conoce {@link IdentityContextFacade} de
 * {@code identidad_acreditacion} y traduce sus DTOs al modelo local {@link PsicologoRef}.
 */
@Service
public class ExternalIdentityService {

    private final IdentityContextFacade identityContextFacade;

    public ExternalIdentityService(IdentityContextFacade identityContextFacade) {
        this.identityContextFacade = identityContextFacade;
    }

    public List<PsicologoRef> fetchVerifiedPsychologists() {
        return identityContextFacade.fetchVerifiedPsychologists().stream()
                .map(this::toLocalRef)
                .toList();
    }

    public Optional<PsicologoRef> fetchVerifiedPsychologistById(Long psychologistId) {
        return identityContextFacade.fetchVerifiedPsychologistById(psychologistId)
                .map(this::toLocalRef);
    }

    private PsicologoRef toLocalRef(VerifiedPsychologistDto dto) {
        var modalidades = dto.modalidadesAtencion() == null
                ? List.<ModalidadPreferida>of()
                : dto.modalidadesAtencion().stream()
                        .map(this::parseModalidad)
                        .filter(Objects::nonNull)
                        .toList();

        return new PsicologoRef(
                dto.id(),
                dto.nombreCompleto(),
                dto.enfoqueTeorico(),
                dto.especialidades(),
                modalidades,
                dto.tarifaPorSesion(),
                dto.disponibilidadHoraria(),
                dto.verificado());
    }

    private ModalidadPreferida parseModalidad(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return ModalidadPreferida.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
