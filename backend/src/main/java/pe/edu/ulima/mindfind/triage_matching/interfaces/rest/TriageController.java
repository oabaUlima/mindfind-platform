package pe.edu.ulima.mindfind.triage_matching.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.ulima.mindfind.shared.application.exceptions.ResourceNotFoundException;
import pe.edu.ulima.mindfind.triage_matching.domain.model.commands.CloseCrisisAlertCommand;
import pe.edu.ulima.mindfind.triage_matching.domain.model.queries.GetMatchingResultsByEvaluationIdQuery;
import pe.edu.ulima.mindfind.triage_matching.domain.model.queries.GetTriageEvaluationByIdQuery;
import pe.edu.ulima.mindfind.triage_matching.domain.services.TriageCommandService;
import pe.edu.ulima.mindfind.triage_matching.domain.services.TriageQueryService;
import pe.edu.ulima.mindfind.triage_matching.interfaces.rest.resources.CrisisCheckResource;
import pe.edu.ulima.mindfind.triage_matching.interfaces.rest.resources.CrisisCheckResponseResource;
import pe.edu.ulima.mindfind.triage_matching.interfaces.rest.resources.MatchingResultResource;
import pe.edu.ulima.mindfind.triage_matching.interfaces.rest.resources.SubmitTriageEvaluationResource;
import pe.edu.ulima.mindfind.triage_matching.interfaces.rest.resources.TriageEvaluationResource;
import pe.edu.ulima.mindfind.triage_matching.interfaces.rest.transform.CheckCrisisKeywordsCommandFromResourceAssembler;
import pe.edu.ulima.mindfind.triage_matching.interfaces.rest.transform.CrisisCheckResponseFromDomainAssembler;
import pe.edu.ulima.mindfind.triage_matching.interfaces.rest.transform.MatchingResultResourceFromDomainAssembler;
import pe.edu.ulima.mindfind.triage_matching.interfaces.rest.transform.SubmitTriageEvaluationCommandFromResourceAssembler;
import pe.edu.ulima.mindfind.triage_matching.interfaces.rest.transform.TriageEvaluationResourceFromDomainAssembler;

import java.util.List;

/** Open Host Service (REST): puerta pública de {@code triage_matching} para MindFind. */
@RestController
@RequestMapping("/api/v1/triage")
@Tag(name = "Triage & Matching", description = "Cuestionario de triage (HU-04), protocolo de crisis (HU-06) y ranking Top 5 (HU-05)")
public class TriageController {

    private final TriageCommandService commandService;
    private final TriageQueryService queryService;

    public TriageController(TriageCommandService commandService, TriageQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping("/evaluations")
    @Operation(
            summary = "Registrar evaluación de triage (HU-04, HU-05, HU-06)",
            description = "Valida preguntas obligatorias, evalúa indicadores de crisis primero y, si no hay crisis, genera el ranking Top 5 de psicólogos verificados.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = "application/json",
            examples = {
                    @ExampleObject(
                            name = "Evaluación estándar (HU-04 + HU-05)",
                            value = """
                                    {
                                      "nombrePaciente": "Lucía Morales",
                                      "edadPaciente": 22,
                                      "motivoConsulta": "Siento mucha ansiedad académica y presión por los exámenes finales",
                                      "sintomasDeclarados": ["ansiedad", "estrés laboral"],
                                      "modalidadPreferida": "VIRTUAL",
                                      "presupuestoMaximo": 130.00,
                                      "disponibilidadPreferida": "Mañana",
                                      "enfoquePreferido": "COGNITIVO_CONDUCTUAL",
                                      "preferenciasProfesional": "Trato empático y estructurado",
                                      "consentimientoCifrado": true,
                                      "indicadorRiesgoInminente": false
                                    }
                                    """),
                    @ExampleObject(
                            name = "Evaluación con indicador de crisis (HU-06)",
                            value = """
                                    {
                                      "nombrePaciente": "Paciente Anónimo",
                                      "edadPaciente": 20,
                                      "motivoConsulta": "He tenido pensamientos de hacerme daño y no quiero vivir así",
                                      "sintomasDeclarados": ["desesperanza", "ansiedad"],
                                      "modalidadPreferida": "VIRTUAL",
                                      "presupuestoMaximo": 100.00,
                                      "disponibilidadPreferida": "Tarde",
                                      "consentimientoCifrado": true,
                                      "indicadorRiesgoInminente": false
                                    }
                                    """),
                    @ExampleObject(
                            name = "ERROR 400: Pregunta obligatoria sin responder",
                            value = """
                                    {
                                      "edadPaciente": 22,
                                      "motivoConsulta": "",
                                      "sintomasDeclarados": ["ansiedad"],
                                      "modalidadPreferida": "",
                                      "presupuestoMaximo": 120.00,
                                      "disponibilidadPreferida": "Mañana",
                                      "consentimientoCifrado": true
                                    }
                                    """)
            }))
    public ResponseEntity<TriageEvaluationResource> submitEvaluation(
            @RequestBody SubmitTriageEvaluationResource resource) {
        var command = SubmitTriageEvaluationCommandFromResourceAssembler.toCommandFromResource(resource);
        var evaluacion = commandService.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TriageEvaluationResourceFromDomainAssembler.toResourceFromDomain(evaluacion));
    }

    @PostMapping("/crisis-check")
    @Operation(
            summary = "Verificación rápida de crisis en tiempo real (HU-06)",
            description = "Analiza texto parcial del cuestionario antes de finalizarlo para activar alertas tempranas de crisis.")
    public CrisisCheckResponseResource checkCrisisKeywords(@RequestBody CrisisCheckResource resource) {
        var command = CheckCrisisKeywordsCommandFromResourceAssembler.toCommandFromResource(resource);
        var resultado = commandService.handle(command);
        return CrisisCheckResponseFromDomainAssembler.toResourceFromDomain(resultado);
    }

    @GetMapping("/evaluations/{id}")
    @Operation(summary = "Obtener evaluación de triage por ID")
    public TriageEvaluationResource getEvaluationById(@PathVariable Long id) {
        return queryService.handle(new GetTriageEvaluationByIdQuery(id))
                .map(TriageEvaluationResourceFromDomainAssembler::toResourceFromDomain)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluación de triage %d no encontrada".formatted(id)));
    }

    @GetMapping("/evaluations/{id}/matches")
    @Operation(
            summary = "Obtener ranking Top 5 de compatibilidad (HU-05)",
            description = "Devuelve los 5 especialistas más compatibles o HTTP 409 Conflict si la evaluación fue interrumpida por crisis.")
    public List<MatchingResultResource> getEvaluationMatches(@PathVariable Long id) {
        return queryService.handle(new GetMatchingResultsByEvaluationIdQuery(id)).stream()
                .map(MatchingResultResourceFromDomainAssembler::toResourceFromDomain)
                .toList();
    }

    @PatchMapping("/evaluations/{id}/crisis/close")
    @Operation(
            summary = "Cerrar alerta de crisis manteniendo bloqueo (HU-06)",
            description = "Marca la alerta como cerrada visualmente, manteniendo el estado INTERRUMPIDA_POR_CRISIS y el emparejamiento bloqueado.")
    public TriageEvaluationResource closeCrisisAlert(@PathVariable Long id) {
        var evaluacion = commandService.handle(new CloseCrisisAlertCommand(id));
        return TriageEvaluationResourceFromDomainAssembler.toResourceFromDomain(evaluacion);
    }
}
