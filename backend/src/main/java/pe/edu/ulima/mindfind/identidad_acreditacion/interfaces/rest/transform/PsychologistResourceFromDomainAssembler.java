package pe.edu.ulima.mindfind.identidad_acreditacion.interfaces.rest.transform;

import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.aggregates.Psicologo;
import pe.edu.ulima.mindfind.identidad_acreditacion.interfaces.rest.resources.PsychologistResource;

/** Traduce el agregado {@link Psicologo} al DTO REST {@link PsychologistResource}. */
public class PsychologistResourceFromDomainAssembler {

    public static PsychologistResource toResourceFromDomain(Psicologo psicologo) {
        return new PsychologistResource(
                psicologo.getId(),
                psicologo.getNombreCompleto(),
                psicologo.getCorreo(),
                psicologo.getTelefono(),
                psicologo.getNumeroColegiatura().value(),
                psicologo.getEnfoqueTeorico().name(),
                psicologo.getEspecialidades(),
                psicologo.getModalidadesAtencion().stream().map(Enum::name).sorted().toList(),
                psicologo.getTarifaPorSesion().monto(),
                psicologo.getTarifaPorSesion().moneda(),
                psicologo.getDisponibilidadHoraria(),
                psicologo.getEstadoVerificacion().name());
    }
}
