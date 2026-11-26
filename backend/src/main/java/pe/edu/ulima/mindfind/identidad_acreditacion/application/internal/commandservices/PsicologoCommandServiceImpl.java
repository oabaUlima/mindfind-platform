package pe.edu.ulima.mindfind.identidad_acreditacion.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.aggregates.Psicologo;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.commands.RegisterPsychologistCommand;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.commands.VerifyPsychologistCommand;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.NumeroColegiatura;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.TarifaSesion;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.repositories.PsicologoRepository;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.services.PsicologoCommandService;
import pe.edu.ulima.mindfind.shared.application.exceptions.ResourceNotFoundException;

/**
 * Servicio de aplicación para comandos de {@code identidad_acreditacion}.
 * Orquesta unicidad, persistencia y publicación de eventos; las reglas de negocio viven en {@link Psicologo}.
 */
@Service
@Transactional
public class PsicologoCommandServiceImpl implements PsicologoCommandService {

    private static final String DUPLICATE_MESSAGE =
            "El número de colegiatura o correo electrónico ya se encuentra registrado en el sistema";

    private final PsicologoRepository psicologoRepository;

    public PsicologoCommandServiceImpl(PsicologoRepository psicologoRepository) {
        this.psicologoRepository = psicologoRepository;
    }

    @Override
    public Psicologo handle(RegisterPsychologistCommand command) {
        var psicologo = Psicologo.register(
                command.nombreCompleto(),
                command.correo(),
                command.telefono(),
                new NumeroColegiatura(command.numeroColegiatura()),
                command.enfoqueTeorico(),
                command.especialidades(),
                command.modalidadesAtencion(),
                TarifaSesion.enSoles(command.tarifaPorSesion()),
                command.disponibilidadHoraria());

        if (psicologoRepository.existsByCorreoIgnoreCase(psicologo.getCorreo())
                || psicologoRepository.existsByNumeroColegiatura(psicologo.getNumeroColegiatura().value())) {
            throw new IllegalStateException(DUPLICATE_MESSAGE);
        }

        return psicologoRepository.save(psicologo);
    }

    @Override
    public Psicologo handle(VerifyPsychologistCommand command) {
        var psicologo = psicologoRepository.findById(command.psicologoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Psicólogo %d no encontrado".formatted(command.psicologoId())));
        psicologo.verify();
        return psicologoRepository.save(psicologo);
    }
}
