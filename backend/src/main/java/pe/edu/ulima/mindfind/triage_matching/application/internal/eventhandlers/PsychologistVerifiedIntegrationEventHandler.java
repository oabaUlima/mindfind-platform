package pe.edu.ulima.mindfind.triage_matching.application.internal.eventhandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.edu.ulima.mindfind.identidad_acreditacion.interfaces.events.PsychologistVerifiedIntegrationEvent;
import pe.edu.ulima.mindfind.triage_matching.application.internal.outboundservices.acl.ExternalIdentityService;

/**
 * INTEGRATION EVENT HANDLER: reacciona al evento público {@link PsychologistVerifiedIntegrationEvent}
 * emitido por {@code identidad_acreditacion} (patrón idéntico a {@code notifications} en la referencia).
 * Se ejecuta AFTER_COMMIT en una transacción independiente para registrar trazabilidad sin acoplar contextos.
 */
@Component
public class PsychologistVerifiedIntegrationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(PsychologistVerifiedIntegrationEventHandler.class);

    private final ExternalIdentityService externalIdentityService;

    public PsychologistVerifiedIntegrationEventHandler(ExternalIdentityService externalIdentityService) {
        this.externalIdentityService = externalIdentityService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(PsychologistVerifiedIntegrationEvent event) {
        log.info("[TriageMatching] Procesando PsychologistVerifiedIntegrationEvent (psicologoId={}) -> consultando perfil vía ACL",
                event.psicologoId());
        externalIdentityService.fetchVerifiedPsychologistById(event.psicologoId()).ifPresentOrElse(
                psicologo -> log.info(
                        "[TriageMatching] Especialista acreditado y habilitado para matching Top 5: id={}, nombre='{}', enfoque={}, especialidades={}",
                        psicologo.idPsicologo(),
                        psicologo.nombreCompleto(),
                        psicologo.enfoqueTeorico(),
                        psicologo.especialidades()),
                () -> log.warn("[TriageMatching] Psicólogo {} no encontrado o no verificado al consultar ACL", event.psicologoId()));
    }
}
