package pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects;

import java.util.List;

/**
 * Línea de atención inmediata y emergencia en Perú mostrada cuando se activa el protocolo de crisis (HU-06).
 */
public record RecursoEmergencia(
        String nombreEntidad,
        String lineaTelefonica,
        String descripcion,
        String disponibilidad) {

    public static List<RecursoEmergencia> recursosPeruPorDefecto() {
        return List.of(
                new RecursoEmergencia(
                        "MINSA - Salud Mental (Línea 113 Opción 5)",
                        "113 Opción 5",
                        "Orientación y consejería psicológica gratuita del Ministerio de Salud",
                        "24/7"),
                new RecursoEmergencia(
                        "SAMU",
                        "106",
                        "Sistema de Atención Móvil de Urgencia para emergencias médicas en todo el Perú",
                        "24/7"),
                new RecursoEmergencia(
                        "MIMP - Línea 100",
                        "100",
                        "Atención, contención y orientación gratuita ante violencia familiar o abuso",
                        "24/7"),
                new RecursoEmergencia(
                        "Policía Nacional del Perú",
                        "105",
                        "Central de emergencias policiales ante riesgo físico inminente",
                        "24/7")
        );
    }
}
