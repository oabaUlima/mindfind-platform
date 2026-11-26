package pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.commands;

import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.EnfoqueTeorico;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.ModalidadAtencion;

import java.math.BigDecimal;
import java.util.List;

/** Comando para registrar un nuevo psicólogo clínico (HU-01). */
public record RegisterPsychologistCommand(
        String nombreCompleto,
        String correo,
        String telefono,
        String numeroColegiatura,
        EnfoqueTeorico enfoqueTeorico,
        List<String> especialidades,
        List<ModalidadAtencion> modalidadesAtencion,
        BigDecimal tarifaPorSesion,
        String disponibilidadHoraria) {
}
