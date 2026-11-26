package pe.edu.ulima.mindfind.triage_matching.domain.model.aggregates;

import pe.edu.ulima.mindfind.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import pe.edu.ulima.mindfind.triage_matching.domain.model.events.CrisisIndicatorDetectedEvent;
import pe.edu.ulima.mindfind.triage_matching.domain.model.events.MatchingRankingGeneratedEvent;
import pe.edu.ulima.mindfind.triage_matching.domain.model.events.TriageEvaluationCompletedEvent;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.EstadoEvaluacion;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.ModalidadPreferida;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.NivelRiesgo;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.PresupuestoMaximo;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.ProtocoloCrisis;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.ResultadoCompatibilidad;
import pe.edu.ulima.mindfind.triage_matching.domain.services.DetectorCrisis;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root {@code EvaluacionTriage} del Core Domain {@code triage_matching}.
 * Implementa las reglas de negocio de HU-04 (cuestionario), HU-06 (protocolo de crisis)
 * y HU-05 (registro del ranking Top 5). Java puro: sin JPA ni Spring.
 */
public class EvaluacionTriage extends AbstractDomainAggregateRoot {

    private static final String MISSING_REQUIRED_ANSWER_MESSAGE =
            "Debe responder esta pregunta para continuar";
    private static final String BLOCKED_BY_CRISIS_MESSAGE =
            "El emparejamiento está bloqueado porque se detectó un indicador de riesgo crítico";

    private final Long id;
    private final String nombrePaciente;
    private final Integer edadPaciente;
    private final String motivoConsulta;
    private final List<String> sintomasDeclarados;
    private final ModalidadPreferida modalidadPreferida;
    private final PresupuestoMaximo presupuestoMaximo;
    private final String disponibilidadPreferida;
    private final String enfoquePreferido;
    private final String preferenciasProfesional;
    private final boolean consentimientoCifrado;
    private EstadoEvaluacion estado;
    private NivelRiesgo nivelRiesgo;
    private final Instant fechaInicio;
    private Instant fechaFinalizacion;
    private ProtocoloCrisis protocoloCrisis;
    private List<ResultadoCompatibilidad> resultadosCompatibilidad;

    private EvaluacionTriage(
            Long id,
            String nombrePaciente,
            Integer edadPaciente,
            String motivoConsulta,
            List<String> sintomasDeclarados,
            ModalidadPreferida modalidadPreferida,
            PresupuestoMaximo presupuestoMaximo,
            String disponibilidadPreferida,
            String enfoquePreferido,
            String preferenciasProfesional,
            boolean consentimientoCifrado,
            EstadoEvaluacion estado,
            NivelRiesgo nivelRiesgo,
            Instant fechaInicio,
            Instant fechaFinalizacion,
            ProtocoloCrisis protocoloCrisis,
            List<ResultadoCompatibilidad> resultadosCompatibilidad) {
        this.id = id;
        this.nombrePaciente = nombrePaciente;
        this.edadPaciente = edadPaciente;
        this.motivoConsulta = motivoConsulta;
        this.sintomasDeclarados = List.copyOf(sintomasDeclarados);
        this.modalidadPreferida = modalidadPreferida;
        this.presupuestoMaximo = presupuestoMaximo;
        this.disponibilidadPreferida = disponibilidadPreferida;
        this.enfoquePreferido = enfoquePreferido;
        this.preferenciasProfesional = preferenciasProfesional;
        this.consentimientoCifrado = consentimientoCifrado;
        this.estado = estado;
        this.nivelRiesgo = nivelRiesgo;
        this.fechaInicio = fechaInicio;
        this.fechaFinalizacion = fechaFinalizacion;
        this.protocoloCrisis = protocoloCrisis;
        this.resultadosCompatibilidad = resultadosCompatibilidad == null
                ? List.of()
                : List.copyOf(resultadosCompatibilidad);
    }

    /**
     * Inicia una evaluación validando que todas las preguntas obligatorias hayan sido respondidas (HU-04).
     */
    public static EvaluacionTriage iniciar(
            String nombrePaciente,
            Integer edadPaciente,
            String motivoConsulta,
            Collection<String> sintomasDeclarados,
            ModalidadPreferida modalidadPreferida,
            PresupuestoMaximo presupuestoMaximo,
            String disponibilidadPreferida,
            String enfoquePreferido,
            String preferenciasProfesional,
            Boolean consentimientoCifrado) {

        var sintomasLimpios = normalizeSintomas(sintomasDeclarados);

        if (edadPaciente == null
                || motivoConsulta == null || motivoConsulta.isBlank()
                || sintomasLimpios.isEmpty()
                || modalidadPreferida == null
                || presupuestoMaximo == null
                || disponibilidadPreferida == null || disponibilidadPreferida.isBlank()
                || consentimientoCifrado == null) {
            throw new IllegalArgumentException(MISSING_REQUIRED_ANSWER_MESSAGE);
        }
        if (edadPaciente <= 0 || edadPaciente > 120) {
            throw new IllegalArgumentException("La edad del paciente debe ser válida");
        }
        if (!consentimientoCifrado) {
            throw new IllegalArgumentException("Debe aceptar el consentimiento de confidencialidad y cifrado para continuar");
        }

        return new EvaluacionTriage(
                null,
                trimToNull(nombrePaciente),
                edadPaciente,
                motivoConsulta.trim(),
                sintomasLimpios,
                modalidadPreferida,
                presupuestoMaximo,
                disponibilidadPreferida.trim(),
                trimToNull(enfoquePreferido),
                trimToNull(preferenciasProfesional),
                true,
                EstadoEvaluacion.EN_PROGRESO,
                NivelRiesgo.BAJO,
                Instant.now(),
                null,
                null,
                List.of());
    }

    /** Reconstruye un agregado existente desde persistencia sin re-validar ni emitir eventos. */
    public static EvaluacionTriage restore(
            Long id,
            String nombrePaciente,
            Integer edadPaciente,
            String motivoConsulta,
            List<String> sintomasDeclarados,
            ModalidadPreferida modalidadPreferida,
            PresupuestoMaximo presupuestoMaximo,
            String disponibilidadPreferida,
            String enfoquePreferido,
            String preferenciasProfesional,
            boolean consentimientoCifrado,
            EstadoEvaluacion estado,
            NivelRiesgo nivelRiesgo,
            Instant fechaInicio,
            Instant fechaFinalizacion,
            ProtocoloCrisis protocoloCrisis,
            List<ResultadoCompatibilidad> resultadosCompatibilidad) {
        return new EvaluacionTriage(
                id,
                nombrePaciente,
                edadPaciente,
                motivoConsulta,
                sintomasDeclarados,
                modalidadPreferida,
                presupuestoMaximo,
                disponibilidadPreferida,
                enfoquePreferido,
                preferenciasProfesional,
                consentimientoCifrado,
                estado,
                nivelRiesgo,
                fechaInicio,
                fechaFinalizacion,
                protocoloCrisis,
                resultadosCompatibilidad);
    }

    /**
     * Evalúa indicadores de crisis. Si detecta riesgo crítico, interrumpe el triage,
     * bloquea el emparejamiento, registra el protocolo con líneas de emergencia de Perú
     * y emite {@link CrisisIndicatorDetectedEvent}.
     */
    public boolean evaluarRiesgoCrisis(DetectorCrisis detectorCrisis, boolean indicadorRiesgoInminente) {
        Objects.requireNonNull(detectorCrisis, "detectorCrisis es obligatorio");
        var resultado = detectorCrisis.evaluar(
                motivoConsulta,
                sintomasDeclarados,
                preferenciasProfesional,
                indicadorRiesgoInminente);

        this.nivelRiesgo = resultado.nivelRiesgo();

        if (resultado.crisisDetectada()) {
            var ahora = Instant.now();
            this.estado = EstadoEvaluacion.INTERRUMPIDA_POR_CRISIS;
            this.fechaFinalizacion = ahora;
            this.protocoloCrisis = ProtocoloCrisis.activar(resultado.palabraDetectada());
            this.resultadosCompatibilidad = List.of();
            registerDomainEvent(new CrisisIndicatorDetectedEvent(
                    id,
                    resultado.palabraDetectada(),
                    resultado.nivelRiesgo(),
                    true,
                    ahora));
            return true;
        }
        return false;
    }

    /**
     * Cierra visualmente la alerta de crisis manteniendo el bloqueo de emparejamiento
     * y el estado {@link EstadoEvaluacion#INTERRUMPIDA_POR_CRISIS} (HU-06).
     */
    public void cerrarAlertaCrisis() {
        if (estado != EstadoEvaluacion.INTERRUMPIDA_POR_CRISIS || protocoloCrisis == null) {
            throw new IllegalStateException("La evaluación no tiene una alerta de crisis activa");
        }
        this.protocoloCrisis = this.protocoloCrisis.cerrarAlerta();
        this.estado = EstadoEvaluacion.INTERRUMPIDA_POR_CRISIS;
    }

    /** Marca la evaluación como completada sin crisis y emite {@link TriageEvaluationCompletedEvent}. */
    public void completarSinCrisis() {
        validarNoBloqueadaPorCrisis();
        if (this.estado != EstadoEvaluacion.COMPLETADA) {
            var ahora = Instant.now();
            this.estado = EstadoEvaluacion.COMPLETADA;
            this.fechaFinalizacion = ahora;
            registerDomainEvent(new TriageEvaluationCompletedEvent(
                    id,
                    nivelRiesgo,
                    modalidadPreferida,
                    ahora));
        }
    }

    /**
     * Registra el ranking Top 5 calculado por el motor de matching (HU-05).
     * Lanza {@link IllegalStateException} si la evaluación fue interrumpida por crisis.
     */
    public void registrarResultadosMatching(List<ResultadoCompatibilidad> resultados) {
        validarNoBloqueadaPorCrisis();
        if (this.estado == EstadoEvaluacion.EN_PROGRESO) {
            completarSinCrisis();
        }
        var top5 = resultados == null ? List.<ResultadoCompatibilidad>of() : resultados.stream().limit(5).toList();
        this.resultadosCompatibilidad = List.copyOf(top5);
        double mejorPorcentaje = top5.isEmpty()
                ? 0.0
                : top5.getFirst().porcentajeCompatibilidad().valor();
        registerDomainEvent(new MatchingRankingGeneratedEvent(
                id,
                top5.size(),
                mejorPorcentaje,
                Instant.now()));
    }

    public boolean estaInterrumpidaPorCrisis() {
        return estado == EstadoEvaluacion.INTERRUMPIDA_POR_CRISIS
                || (protocoloCrisis != null && protocoloCrisis.emparejamientoBloqueado());
    }

    private void validarNoBloqueadaPorCrisis() {
        if (estaInterrumpidaPorCrisis()) {
            throw new IllegalStateException(BLOCKED_BY_CRISIS_MESSAGE);
        }
    }

    private static List<String> normalizeSintomas(Collection<String> raw) {
        if (raw == null) {
            return List.of();
        }
        var unique = new LinkedHashSet<String>();
        for (var item : raw) {
            if (item != null && !item.isBlank()) {
                unique.add(item.trim());
            }
        }
        return List.copyOf(unique);
    }

    private static String trimToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    public Long getId() {
        return id;
    }

    public String getNombrePaciente() {
        return nombrePaciente;
    }

    public Integer getEdadPaciente() {
        return edadPaciente;
    }

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public List<String> getSintomasDeclarados() {
        return sintomasDeclarados;
    }

    public ModalidadPreferida getModalidadPreferida() {
        return modalidadPreferida;
    }

    public PresupuestoMaximo getPresupuestoMaximo() {
        return presupuestoMaximo;
    }

    public String getDisponibilidadPreferida() {
        return disponibilidadPreferida;
    }

    public String getEnfoquePreferido() {
        return enfoquePreferido;
    }

    public String getPreferenciasProfesional() {
        return preferenciasProfesional;
    }

    public boolean isConsentimientoCifrado() {
        return consentimientoCifrado;
    }

    public EstadoEvaluacion getEstado() {
        return estado;
    }

    public NivelRiesgo getNivelRiesgo() {
        return nivelRiesgo;
    }

    public Instant getFechaInicio() {
        return fechaInicio;
    }

    public Instant getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public ProtocoloCrisis getProtocoloCrisis() {
        return protocoloCrisis;
    }

    public List<ResultadoCompatibilidad> getResultadosCompatibilidad() {
        return resultadosCompatibilidad;
    }
}
