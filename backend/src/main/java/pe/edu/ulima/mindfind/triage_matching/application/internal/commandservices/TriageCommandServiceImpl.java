package pe.edu.ulima.mindfind.triage_matching.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ulima.mindfind.shared.application.exceptions.ResourceNotFoundException;
import pe.edu.ulima.mindfind.triage_matching.application.internal.outboundservices.acl.ExternalIdentityService;
import pe.edu.ulima.mindfind.triage_matching.domain.model.aggregates.EvaluacionTriage;
import pe.edu.ulima.mindfind.triage_matching.domain.model.commands.CheckCrisisKeywordsCommand;
import pe.edu.ulima.mindfind.triage_matching.domain.model.commands.CloseCrisisAlertCommand;
import pe.edu.ulima.mindfind.triage_matching.domain.model.commands.SubmitTriageEvaluationCommand;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.PresupuestoMaximo;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.ResultadoDeteccionCrisis;
import pe.edu.ulima.mindfind.triage_matching.domain.repositories.EvaluacionTriageRepository;
import pe.edu.ulima.mindfind.triage_matching.domain.services.DetectorCrisis;
import pe.edu.ulima.mindfind.triage_matching.domain.services.MotorMatching;
import pe.edu.ulima.mindfind.triage_matching.domain.services.TriageCommandService;

/**
 * Orquestador de comandos para {@code triage_matching}.
 * Prioriza el protocolo de crisis (HU-06) antes de consultar el ACL o ejecutar el matching (HU-05).
 */
@Service
@Transactional
public class TriageCommandServiceImpl implements TriageCommandService {

    private final EvaluacionTriageRepository evaluacionTriageRepository;
    private final ExternalIdentityService externalIdentityService;
    private final DetectorCrisis detectorCrisis = new DetectorCrisis();
    private final MotorMatching motorMatching = new MotorMatching();

    public TriageCommandServiceImpl(
            EvaluacionTriageRepository evaluacionTriageRepository,
            ExternalIdentityService externalIdentityService) {
        this.evaluacionTriageRepository = evaluacionTriageRepository;
        this.externalIdentityService = externalIdentityService;
    }

    @Override
    public EvaluacionTriage handle(SubmitTriageEvaluationCommand command) {
        var presupuesto = command.presupuestoMaximo() == null
                ? null
                : PresupuestoMaximo.enSoles(command.presupuestoMaximo());

        var evaluacion = EvaluacionTriage.iniciar(
                command.nombrePaciente(),
                command.edadPaciente(),
                command.motivoConsulta(),
                command.sintomasDeclarados(),
                command.modalidadPreferida(),
                presupuesto,
                command.disponibilidadPreferida(),
                command.enfoquePreferido(),
                command.preferenciasProfesional(),
                command.consentimientoCifrado());

        // Paso 1: persistir en EN_PROGRESO dentro de la transacción para obtener el ID antes de emitir eventos.
        evaluacion = evaluacionTriageRepository.save(evaluacion);

        // Paso 2: evaluar indicadores de crisis primero (HU-06).
        boolean crisisDetectada = evaluacion.evaluarRiesgoCrisis(detectorCrisis, command.indicadorRiesgoInminente());
        if (crisisDetectada) {
            return evaluacionTriageRepository.save(evaluacion);
        }

        // Paso 3: sin crisis -> obtener psicólogos verificados vía ACL, calcular Top 5 y completar (HU-05).
        var psicologosVerificados = externalIdentityService.fetchVerifiedPsychologists();
        var top5 = motorMatching.calcularTop5(evaluacion, psicologosVerificados);
        evaluacion.registrarResultadosMatching(top5);
        return evaluacionTriageRepository.save(evaluacion);
    }

    @Override
    @Transactional(readOnly = true)
    public ResultadoDeteccionCrisis handle(CheckCrisisKeywordsCommand command) {
        return detectorCrisis.evaluar(
                command.motivoConsulta(),
                command.sintomasDeclarados(),
                command.preferenciasProfesional(),
                command.indicadorRiesgoInminente());
    }

    @Override
    public EvaluacionTriage handle(CloseCrisisAlertCommand command) {
        var evaluacion = evaluacionTriageRepository.findById(command.evaluacionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Evaluación de triage %d no encontrada".formatted(command.evaluacionId())));
        evaluacion.cerrarAlertaCrisis();
        return evaluacionTriageRepository.save(evaluacion);
    }
}
