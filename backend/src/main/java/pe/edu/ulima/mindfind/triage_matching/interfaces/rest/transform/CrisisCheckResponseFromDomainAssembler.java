package pe.edu.ulima.mindfind.triage_matching.interfaces.rest.transform;

import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.RecursoEmergencia;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.ResultadoDeteccionCrisis;
import pe.edu.ulima.mindfind.triage_matching.interfaces.rest.resources.CrisisCheckResponseResource;
import pe.edu.ulima.mindfind.triage_matching.interfaces.rest.resources.RecursoEmergenciaResource;

import java.util.List;

/** Traduce {@link ResultadoDeteccionCrisis} a {@link CrisisCheckResponseResource}. */
public class CrisisCheckResponseFromDomainAssembler {

    private static final String CRISIS_ALERT_MESSAGE =
            "Hemos detectado una situación que requiere atención inmediata. Por tu seguridad, el cuestionario se pausará y te conectamos con líneas de ayuda gratuitas 24/7 en Perú.";

    public static CrisisCheckResponseResource toResourceFromDomain(ResultadoDeteccionCrisis resultado) {
        if (!resultado.crisisDetectada()) {
            return new CrisisCheckResponseResource(
                    false,
                    resultado.nivelRiesgo().name(),
                    null,
                    false,
                    null,
                    List.of());
        }
        var recursos = RecursoEmergencia.recursosPeruPorDefecto().stream()
                .map(r -> new RecursoEmergenciaResource(
                        r.nombreEntidad(),
                        r.lineaTelefonica(),
                        r.descripcion(),
                        r.disponibilidad()))
                .toList();
        return new CrisisCheckResponseResource(
                true,
                resultado.nivelRiesgo().name(),
                resultado.palabraDetectada(),
                true,
                CRISIS_ALERT_MESSAGE,
                recursos);
    }
}
