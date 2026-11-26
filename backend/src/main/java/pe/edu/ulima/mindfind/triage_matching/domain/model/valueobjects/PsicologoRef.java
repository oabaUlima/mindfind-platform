package pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects;

import java.math.BigDecimal;
import java.util.List;

/**
 * Modelo local propio de {@code triage_matching} traducido por el ACL ({@code ExternalIdentityService})
 * a partir del contrato publicado por {@code identidad_acreditacion}.
 */
public record PsicologoRef(
        Long idPsicologo,
        String nombreCompleto,
        String enfoqueTeorico,
        List<String> especialidades,
        List<ModalidadPreferida> modalidadesAtencion,
        BigDecimal tarifaPorSesion,
        String disponibilidadHoraria,
        boolean verificado) {

    public PsicologoRef {
        especialidades = especialidades == null ? List.of() : List.copyOf(especialidades);
        modalidadesAtencion = modalidadesAtencion == null ? List.of() : List.copyOf(modalidadesAtencion);
    }
}
