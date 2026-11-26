package pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.events;

import java.time.Instant;

/**
 * Domain Event interno al contexto {@code identidad_acreditacion} emitido al registrar un nuevo psicólogo.
 */
public record PsychologistRegisteredEvent(
        String nombreCompleto,
        String correo,
        String numeroColegiatura,
        Instant occurredAt) {
}
