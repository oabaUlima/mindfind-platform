package pe.edu.ulima.mindfind.triage_matching.interfaces.rest.resources;

/** Recurso de atención inmediata de emergencia en Perú expuesto en la API REST. */
public record RecursoEmergenciaResource(
        String nombreEntidad,
        String lineaTelefonica,
        String descripcion,
        String disponibilidad) {
}
