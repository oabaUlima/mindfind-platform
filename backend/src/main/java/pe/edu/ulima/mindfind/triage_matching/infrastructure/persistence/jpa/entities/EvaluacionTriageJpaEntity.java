package pe.edu.ulima.mindfind.triage_matching.infrastructure.persistence.jpa.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import pe.edu.ulima.mindfind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.EstadoEvaluacion;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.ModalidadPreferida;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.NivelRiesgo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de persistencia JPA de {@code EvaluacionTriage}.
 * Utiliza {@link FetchMode#SUBSELECT} en colecciones {@code EAGER} para evitar joins cartesianos
 * y {@code LazyInitializationException} con {@code spring.jpa.open-in-view=false}.
 */
@Entity
@Table(name = "evaluaciones_triage")
public class EvaluacionTriageJpaEntity extends AuditableAbstractPersistenceEntity {

    @Column
    private String nombrePaciente;

    @Column(nullable = false)
    private Integer edadPaciente;

    @Column(nullable = false, length = 1000)
    private String motivoConsulta;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "triage_sintomas", joinColumns = @JoinColumn(name = "evaluacion_id"))
    @OrderColumn(name = "orden")
    @Column(name = "sintoma", nullable = false)
    private List<String> sintomasDeclarados = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModalidadPreferida modalidadPreferida;

    @Column(name = "presupuesto_monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal presupuestoMonto;

    @Column(name = "presupuesto_moneda", nullable = false, length = 3)
    private String presupuestoMoneda;

    @Column(nullable = false)
    private String disponibilidadPreferida;

    @Column
    private String enfoquePreferido;

    @Column(length = 1000)
    private String preferenciasProfesional;

    @Column(nullable = false)
    private boolean consentimientoCifrado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEvaluacion estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelRiesgo nivelRiesgo;

    @Column(nullable = false)
    private Instant fechaInicio;

    @Column
    private Instant fechaFinalizacion;

    @Column(nullable = false)
    private boolean crisisActiva;

    @Column
    private String crisisPalabraDetectada;

    @Enumerated(EnumType.STRING)
    @Column
    private NivelRiesgo crisisNivelRiesgo;

    @Column(nullable = false)
    private boolean crisisEmparejamientoBloqueado;

    @Column(nullable = false)
    private boolean crisisAlertaCerrada;

    @OneToMany(mappedBy = "evaluacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @OrderColumn(name = "posicion_orden")
    private List<ResultadoCompatibilidadJpaEntity> resultadosCompatibilidad = new ArrayList<>();

    public String getNombrePaciente() {
        return nombrePaciente;
    }

    public void setNombrePaciente(String nombrePaciente) {
        this.nombrePaciente = nombrePaciente;
    }

    public Integer getEdadPaciente() {
        return edadPaciente;
    }

    public void setEdadPaciente(Integer edadPaciente) {
        this.edadPaciente = edadPaciente;
    }

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }

    public List<String> getSintomasDeclarados() {
        return sintomasDeclarados;
    }

    public void setSintomasDeclarados(List<String> sintomasDeclarados) {
        this.sintomasDeclarados = sintomasDeclarados;
    }

    public ModalidadPreferida getModalidadPreferida() {
        return modalidadPreferida;
    }

    public void setModalidadPreferida(ModalidadPreferida modalidadPreferida) {
        this.modalidadPreferida = modalidadPreferida;
    }

    public BigDecimal getPresupuestoMonto() {
        return presupuestoMonto;
    }

    public void setPresupuestoMonto(BigDecimal presupuestoMonto) {
        this.presupuestoMonto = presupuestoMonto;
    }

    public String getPresupuestoMoneda() {
        return presupuestoMoneda;
    }

    public void setPresupuestoMoneda(String presupuestoMoneda) {
        this.presupuestoMoneda = presupuestoMoneda;
    }

    public String getDisponibilidadPreferida() {
        return disponibilidadPreferida;
    }

    public void setDisponibilidadPreferida(String disponibilidadPreferida) {
        this.disponibilidadPreferida = disponibilidadPreferida;
    }

    public String getEnfoquePreferido() {
        return enfoquePreferido;
    }

    public void setEnfoquePreferido(String enfoquePreferido) {
        this.enfoquePreferido = enfoquePreferido;
    }

    public String getPreferenciasProfesional() {
        return preferenciasProfesional;
    }

    public void setPreferenciasProfesional(String preferenciasProfesional) {
        this.preferenciasProfesional = preferenciasProfesional;
    }

    public boolean isConsentimientoCifrado() {
        return consentimientoCifrado;
    }

    public void setConsentimientoCifrado(boolean consentimientoCifrado) {
        this.consentimientoCifrado = consentimientoCifrado;
    }

    public EstadoEvaluacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoEvaluacion estado) {
        this.estado = estado;
    }

    public NivelRiesgo getNivelRiesgo() {
        return nivelRiesgo;
    }

    public void setNivelRiesgo(NivelRiesgo nivelRiesgo) {
        this.nivelRiesgo = nivelRiesgo;
    }

    public Instant getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Instant fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Instant getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(Instant fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public boolean isCrisisActiva() {
        return crisisActiva;
    }

    public void setCrisisActiva(boolean crisisActiva) {
        this.crisisActiva = crisisActiva;
    }

    public String getCrisisPalabraDetectada() {
        return crisisPalabraDetectada;
    }

    public void setCrisisPalabraDetectada(String crisisPalabraDetectada) {
        this.crisisPalabraDetectada = crisisPalabraDetectada;
    }

    public NivelRiesgo getCrisisNivelRiesgo() {
        return crisisNivelRiesgo;
    }

    public void setCrisisNivelRiesgo(NivelRiesgo crisisNivelRiesgo) {
        this.crisisNivelRiesgo = crisisNivelRiesgo;
    }

    public boolean isCrisisEmparejamientoBloqueado() {
        return crisisEmparejamientoBloqueado;
    }

    public void setCrisisEmparejamientoBloqueado(boolean crisisEmparejamientoBloqueado) {
        this.crisisEmparejamientoBloqueado = crisisEmparejamientoBloqueado;
    }

    public boolean isCrisisAlertaCerrada() {
        return crisisAlertaCerrada;
    }

    public void setCrisisAlertaCerrada(boolean crisisAlertaCerrada) {
        this.crisisAlertaCerrada = crisisAlertaCerrada;
    }

    public List<ResultadoCompatibilidadJpaEntity> getResultadosCompatibilidad() {
        return resultadosCompatibilidad;
    }

    public void setResultadosCompatibilidad(List<ResultadoCompatibilidadJpaEntity> resultadosCompatibilidad) {
        this.resultadosCompatibilidad = resultadosCompatibilidad;
    }
}
