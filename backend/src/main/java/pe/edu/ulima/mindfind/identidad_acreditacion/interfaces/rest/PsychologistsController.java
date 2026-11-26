package pe.edu.ulima.mindfind.identidad_acreditacion.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.commands.VerifyPsychologistCommand;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.queries.GetAllPsychologistsQuery;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.queries.GetPsychologistByIdQuery;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.queries.GetVerifiedPsychologistsQuery;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.services.PsicologoCommandService;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.services.PsicologoQueryService;
import pe.edu.ulima.mindfind.identidad_acreditacion.interfaces.rest.resources.PsychologistResource;
import pe.edu.ulima.mindfind.identidad_acreditacion.interfaces.rest.resources.RegisterPsychologistResource;
import pe.edu.ulima.mindfind.identidad_acreditacion.interfaces.rest.resources.RegisterPsychologistResponseResource;
import pe.edu.ulima.mindfind.identidad_acreditacion.interfaces.rest.transform.PsychologistResourceFromDomainAssembler;
import pe.edu.ulima.mindfind.identidad_acreditacion.interfaces.rest.transform.RegisterPsychologistCommandFromResourceAssembler;
import pe.edu.ulima.mindfind.shared.application.exceptions.ResourceNotFoundException;

import java.util.List;

/** Open Host Service (REST): puerta pública de {@code identidad_acreditacion} para MindFind. */
@RestController
@RequestMapping("/api/v1/psychologists")
@Tag(name = "Psychologists", description = "Identidad y Acreditación: registro y validación de psicólogos clínicos (HU-01)")
public class PsychologistsController {

    private static final String REGISTRATION_SUCCESS_MESSAGE =
            "Registro completado con éxito. Su perfil está en proceso de validación institucional";

    private final PsicologoCommandService commandService;
    private final PsicologoQueryService queryService;

    public PsychologistsController(PsicologoCommandService commandService, PsicologoQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    @Operation(
            summary = "Registrar perfil profesional de psicólogo (HU-01)",
            description = "Crea un perfil en estado PENDIENTE_VERIFICACION validando número de colegiatura, enfoque, especialidades y unicidad.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = "application/json",
            examples = {
                    @ExampleObject(
                            name = "Registro válido",
                            value = """
                                    {
                                      "nombreCompleto": "Dra. Sofía Ramírez León",
                                      "correo": "sofia.ramirez@mindfind.pe",
                                      "telefono": "991234567",
                                      "numeroColegiatura": "45678",
                                      "enfoqueTeorico": "COGNITIVO_CONDUCTUAL",
                                      "especialidades": ["ansiedad", "estrés laboral"],
                                      "modalidadesAtencion": ["VIRTUAL", "PRESENCIAL"],
                                      "tarifaPorSesion": 120.00,
                                      "disponibilidadHoraria": "Lunes a Viernes 15:00 - 20:00 (Tarde/Noche)"
                                    }
                                    """),
                    @ExampleObject(
                            name = "ERROR 400: Colegiatura inválida",
                            value = """
                                    {
                                      "nombreCompleto": "Dr. Test",
                                      "correo": "test.colegiatura@mindfind.pe",
                                      "telefono": "991234568",
                                      "numeroColegiatura": "123",
                                      "enfoqueTeorico": "HUMANISTA",
                                      "especialidades": ["autoestima"],
                                      "modalidadesAtencion": ["VIRTUAL"],
                                      "tarifaPorSesion": 100.00,
                                      "disponibilidadHoraria": "Mañanas"
                                    }
                                    """)
            }))
    public ResponseEntity<RegisterPsychologistResponseResource> registerPsychologist(
            @Valid @RequestBody RegisterPsychologistResource resource) {
        var command = RegisterPsychologistCommandFromResourceAssembler.toCommandFromResource(resource);
        var psicologo = commandService.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterPsychologistResponseResource(
                        REGISTRATION_SUCCESS_MESSAGE,
                        PsychologistResourceFromDomainAssembler.toResourceFromDomain(psicologo)));
    }

    @GetMapping
    @Operation(summary = "Listar psicólogos", description = "Lista todos los psicólogos o filtra únicamente los verificados.")
    public List<PsychologistResource> getPsychologists(
            @Parameter(description = "Si es true, retorna solo perfiles en estado VERIFICADO")
            @RequestParam(required = false, defaultValue = "false") boolean soloVerificados) {
        var list = soloVerificados
                ? queryService.handle(new GetVerifiedPsychologistsQuery())
                : queryService.handle(new GetAllPsychologistsQuery());
        return list.stream()
                .map(PsychologistResourceFromDomainAssembler::toResourceFromDomain)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener psicólogo por ID")
    public PsychologistResource getPsychologistById(@PathVariable Long id) {
        return queryService.handle(new GetPsychologistByIdQuery(id))
                .map(PsychologistResourceFromDomainAssembler::toResourceFromDomain)
                .orElseThrow(() -> new ResourceNotFoundException("Psicólogo %d no encontrado".formatted(id)));
    }

    @PatchMapping("/{id}/verify")
    @Operation(summary = "Verificar perfil de psicólogo", description = "Cambia el estado del perfil a VERIFICADO (demostración de acreditación).")
    public PsychologistResource verifyPsychologist(@PathVariable Long id) {
        var psicologo = commandService.handle(new VerifyPsychologistCommand(id));
        return PsychologistResourceFromDomainAssembler.toResourceFromDomain(psicologo);
    }
}
