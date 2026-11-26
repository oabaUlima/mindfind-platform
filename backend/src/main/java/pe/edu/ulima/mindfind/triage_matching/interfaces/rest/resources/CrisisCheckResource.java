package pe.edu.ulima.mindfind.triage_matching.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** DTO de entrada para verificación rápida de crisis en tiempo real mientras se llena el formulario. */
public record CrisisCheckResource(
        @Schema(example = "Últimamente he pensado en hacerme daño") String motivoConsulta,
        @Schema(example = "[\"tristeza profunda\"]") List<String> sintomasDeclarados,
        @Schema(example = "") String preferenciasProfesional,
        @Schema(example = "false") Boolean indicadorRiesgoInminente) {
}
