package pe.edu.ulima.mindfind.triage_matching.interfaces.rest.transform;

import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.PresupuestoMaximo;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.ResultadoCompatibilidad;
import pe.edu.ulima.mindfind.triage_matching.interfaces.rest.resources.MatchingResultResource;

/** Traduce {@link ResultadoCompatibilidad} al DTO REST {@link MatchingResultResource}. */
public class MatchingResultResourceFromDomainAssembler {

    public static MatchingResultResource toResourceFromDomain(ResultadoCompatibilidad resultado) {
        return new MatchingResultResource(
                resultado.idPsicologo(),
                resultado.nombrePsicologo(),
                resultado.enfoqueTeorico(),
                resultado.especialidades(),
                resultado.modalidadesAtencion().stream().map(Enum::name).toList(),
                resultado.tarifaPorSesion(),
                PresupuestoMaximo.MONEDA_PEN,
                resultado.disponibilidadHoraria(),
                resultado.porcentajeCompatibilidad().valor(),
                resultado.posicionRanking(),
                resultado.esMatchParcial(),
                resultado.razonesCompatibilidad());
    }
}
