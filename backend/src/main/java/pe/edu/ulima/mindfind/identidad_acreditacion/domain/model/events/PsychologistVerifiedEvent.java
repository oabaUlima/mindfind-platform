package pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.events;

import java.time.Instant;

/**
 * Domain Event interno al contexto {@code identidad_acreditacion} emitido cuando un psicólogo pasa a VERIFICADO.
 */
public record PsychologistVerifiedEvent(
        Long psicologoId,
        String nombreCompleto,
        String numeroColegiatura,
        Instant occurredAt) {
}
