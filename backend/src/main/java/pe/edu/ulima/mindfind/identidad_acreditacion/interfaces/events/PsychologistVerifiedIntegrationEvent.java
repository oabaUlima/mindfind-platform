package pe.edu.ulima.mindfind.identidad_acreditacion.interfaces.events;

import java.time.Instant;

/**
 * Integration Event público emitido por {@code identidad_acreditacion} cuando un psicólogo queda verificado.
 */
public record PsychologistVerifiedIntegrationEvent(Long psicologoId, Instant occurredAt) {
}
