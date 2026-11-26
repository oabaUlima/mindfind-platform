package pe.edu.ulima.mindfind.identidad_acreditacion.interfaces.acl;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contrato público (Published Language) expuesto por {@code identidad_acreditacion}
 * a través de {@link IdentityContextFacade}. Solo usa tipos planos Java.
 */
public record VerifiedPsychologistDto(
        Long id,
        String nombreCompleto,
        String enfoqueTeorico,
        List<String> especialidades,
        List<String> modalidadesAtencion,
        BigDecimal tarifaPorSesion,
        String disponibilidadHoraria,
        boolean verificado) {
}
