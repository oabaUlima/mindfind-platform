package pe.edu.ulima.mindfind.triage_matching.domain.model.commands;

/** Comando para cerrar la alerta de crisis manteniendo bloqueado el emparejamiento. */
public record CloseCrisisAlertCommand(Long evaluacionId) {
}
