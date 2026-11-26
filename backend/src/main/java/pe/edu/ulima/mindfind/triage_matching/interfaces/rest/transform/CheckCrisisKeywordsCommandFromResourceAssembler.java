package pe.edu.ulima.mindfind.triage_matching.interfaces.rest.transform;

import pe.edu.ulima.mindfind.triage_matching.domain.model.commands.CheckCrisisKeywordsCommand;
import pe.edu.ulima.mindfind.triage_matching.interfaces.rest.resources.CrisisCheckResource;

/** Traduce {@link CrisisCheckResource} a {@link CheckCrisisKeywordsCommand}. */
public class CheckCrisisKeywordsCommandFromResourceAssembler {

    public static CheckCrisisKeywordsCommand toCommandFromResource(CrisisCheckResource resource) {
        return new CheckCrisisKeywordsCommand(
                resource.motivoConsulta(),
                resource.sintomasDeclarados(),
                resource.preferenciasProfesional(),
                Boolean.TRUE.equals(resource.indicadorRiesgoInminente()));
    }
}
