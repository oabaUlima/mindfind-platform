package pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects;

import java.util.List;
import java.util.Objects;

/**
 * Registro del protocolo de crisis activado durante el triage (HU-06).
 * Al cerrar la alerta, {@code emparejamientoBloqueado} permanece en {@code true}.
 */
public record ProtocoloCrisis(
        String palabraDetectada,
        NivelRiesgo nivelRiesgo,
        boolean emparejamientoBloqueado,
        boolean alertaCerrada,
        List<RecursoEmergencia> recursosEmergencia) {

    public ProtocoloCrisis {
        Objects.requireNonNull(nivelRiesgo, "El nivel de riesgo del protocolo de crisis es obligatorio");
        recursosEmergencia = (recursosEmergencia == null || recursosEmergencia.isEmpty())
                ? RecursoEmergencia.recursosPeruPorDefecto()
                : List.copyOf(recursosEmergencia);
    }

    public static ProtocoloCrisis activar(String palabraDetectada) {
        return new ProtocoloCrisis(
                palabraDetectada,
                NivelRiesgo.CRITICO,
                true,
                false,
                RecursoEmergencia.recursosPeruPorDefecto());
    }

    /** Marca la alerta como cerrada manteniendo bloqueado el emparejamiento. */
    public ProtocoloCrisis cerrarAlerta() {
        return new ProtocoloCrisis(
                palabraDetectada,
                nivelRiesgo,
                true,
                true,
                recursosEmergencia);
    }
}
