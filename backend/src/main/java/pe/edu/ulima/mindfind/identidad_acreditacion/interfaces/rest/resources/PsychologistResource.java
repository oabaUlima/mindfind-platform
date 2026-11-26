package pe.edu.ulima.mindfind.identidad_acreditacion.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.List;

/** Representación pública de un perfil de psicólogo en la API REST de MindFind. */
public record PsychologistResource(
        Long id,
        String nombreCompleto,
        String correo,
        String telefono,
        String numeroColegiatura,
        String enfoqueTeorico,
        List<String> especialidades,
        List<String> modalidadesAtencion,
        BigDecimal tarifaPorSesion,
        String moneda,
        String disponibilidadHoraria,
        String estadoVerificacion) {
}
