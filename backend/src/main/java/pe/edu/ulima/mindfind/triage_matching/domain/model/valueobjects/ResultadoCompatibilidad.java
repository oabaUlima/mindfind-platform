package pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Resultado calculado para un psicólogo dentro del ranking Top 5 de compatibilidad (HU-05).
 */
public record ResultadoCompatibilidad(
        Long idPsicologo,
        String nombrePsicologo,
        String enfoqueTeorico,
        List<String> especialidades,
        List<ModalidadPreferida> modalidadesAtencion,
        BigDecimal tarifaPorSesion,
        String disponibilidadHoraria,
        PorcentajeCompatibilidad porcentajeCompatibilidad,
        int posicionRanking,
        boolean esMatchParcial,
        List<String> razonesCompatibilidad) {

    public ResultadoCompatibilidad {
        Objects.requireNonNull(idPsicologo, "idPsicologo es obligatorio");
        Objects.requireNonNull(porcentajeCompatibilidad, "porcentajeCompatibilidad es obligatorio");
        if (posicionRanking < 1) {
            throw new IllegalArgumentException("La posición del ranking debe ser mayor o igual a 1");
        }
        especialidades = especialidades == null ? List.of() : List.copyOf(especialidades);
        modalidadesAtencion = modalidadesAtencion == null ? List.of() : List.copyOf(modalidadesAtencion);
        razonesCompatibilidad = razonesCompatibilidad == null ? List.of() : List.copyOf(razonesCompatibilidad);
    }
}
