package pe.edu.ulima.mindfind.identidad_acreditacion.application.internal.eventhandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.events.PsychologistRegisteredEvent;

/**
 * Policy interna del contexto {@code identidad_acreditacion} que reacciona tras confirmar
 * el registro de un nuevo psicólogo pendiente de validación institucional.
 */
@Component
public class PsychologistRegisteredEventHandler {

    private static final Logger log = LoggerFactory.getLogger(PsychologistRegisteredEventHandler.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PsychologistRegisteredEvent event) {
        log.info("[Identidad] Nuevo perfil registrado pendiente de validación institucional: {} (colegiatura={}, correo={})",
                event.nombreCompleto(), event.numeroColegiatura(), event.correo());
    }
}
