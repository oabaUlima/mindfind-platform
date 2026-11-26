package pe.edu.ulima.mindfind.identidad_acreditacion.domain.services;

import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.aggregates.Psicologo;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.commands.RegisterPsychologistCommand;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.commands.VerifyPsychologistCommand;

public interface PsicologoCommandService {

    Psicologo handle(RegisterPsychologistCommand command);

    Psicologo handle(VerifyPsychologistCommand command);
}
