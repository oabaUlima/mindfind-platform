package pe.edu.ulima.mindfind.identidad_acreditacion.interfaces.rest.resources;

/** Respuesta del endpoint de registro HU-01 con mensaje institucional y perfil creado. */
public record RegisterPsychologistResponseResource(
        String mensaje,
        PsychologistResource psicologo) {
}
