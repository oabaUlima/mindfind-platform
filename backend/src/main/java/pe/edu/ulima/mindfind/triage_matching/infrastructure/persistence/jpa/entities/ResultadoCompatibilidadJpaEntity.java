package pe.edu.ulima.mindfind.triage_matching.infrastructure.persistence.jpa.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import pe.edu.ulima.mindfind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.ModalidadPreferida;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Modelo de persistencia JPA para cada resultado del ranking Top 5 (HU-05).
 */
@Entity
@Table(name = "resultados_compatibilidad_triage")
public class ResultadoCompatibilidadJpaEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluacion_id", nullable = false)
    private EvaluacionTriageJpaEntity evaluacion;

    @Column(nullable = false)
    private Long idPsicologo;

    @Column(nullable = false)
    private String nombrePsicologo;

    @Column(nullable = false)
    private String enfoqueTeorico;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "triage_resultado_especialidades", joinColumns = @JoinColumn(name = "resultado_id"))
    @OrderColumn(name = "orden")
    @Column(name = "especialidad", nullable = false)
    private List<String> especialidades = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "triage_resultado_modalidades", joinColumns = @JoinColumn(name = "resultado_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad", nullable = false)
    private Set<ModalidadPreferida> modalidadesAtencion = new LinkedHashSet<>();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifaPorSesion;

    @Column(nullable = false)
    private String disponibilidadHoraria;

    @Column(nullable = false)
    private double porcentajeCompatibilidad;

    @Column(nullable = false)
    private int posicionRanking;

    @Column(nullable = false)
    private boolean esMatchParcial;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "triage_resultado_razones", joinColumns = @JoinColumn(name = "resultado_id"))
    @OrderColumn(name = "orden")
    @Column(name = "razon", nullable = false, length = 500)
    private List<String> razonesCompatibilidad = new ArrayList<>();

    public EvaluacionTriageJpaEntity getEvaluacion() {
        return evaluacion;
    }

    public void setEvaluacion(EvaluacionTriageJpaEntity evaluacion) {
        this.evaluacion = evaluacion;
    }

    public Long getIdPsicologo() {
        return idPsicologo;
    }

    public void setIdPsicologo(Long idPsicologo) {
        this.idPsicologo = idPsicologo;
    }

    public String getNombrePsicologo() {
        return nombrePsicologo;
    }

    public void setNombrePsicologo(String nombrePsicologo) {
        this.nombrePsicologo = nombrePsicologo;
    }

    public String getEnfoqueTeorico() {
        return enfoqueTeorico;
    }

    public void setEnfoqueTeorico(String enfoqueTeorico) {
        this.enfoqueTeorico = enfoqueTeorico;
    }

    public List<String> getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(List<String> especialidades) {
        this.especialidades = especialidades;
    }

    public Set<ModalidadPreferida> getModalidadesAtencion() {
        return modalidadesAtencion;
    }

    public void setModalidadesAtencion(Set<ModalidadPreferida> modalidadesAtencion) {
        this.modalidadesAtencion = modalidadesAtencion;
    }

    public BigDecimal getTarifaPorSesion() {
        return tarifaPorSesion;
    }

    public void setTarifaPorSesion(BigDecimal tarifaPorSesion) {
        this.tarifaPorSesion = tarifaPorSesion;
    }

    public String getDisponibilidadHoraria() {
        return disponibilidadHoraria;
    }

    public void setDisponibilidadHoraria(String disponibilidadHoraria) {
        this.disponibilidadHoraria = disponibilidadHoraria;
    }

    public double getPorcentajeCompatibilidad() {
        return porcentajeCompatibilidad;
    }

    public void setPorcentajeCompatibilidad(double porcentajeCompatibilidad) {
        this.porcentajeCompatibilidad = porcentajeCompatibilidad;
    }

    public int getPosicionRanking() {
        return posicionRanking;
    }

    public void setPosicionRanking(int posicionRanking) {
        this.posicionRanking = posicionRanking;
    }

    public boolean isEsMatchParcial() {
        return esMatchParcial;
    }

    public void setEsMatchParcial(boolean esMatchParcial) {
        this.esMatchParcial = esMatchParcial;
    }

    public List<String> getRazonesCompatibilidad() {
        return razonesCompatibilidad;
    }

    public void setRazonesCompatibilidad(List<String> razonesCompatibilidad) {
        this.razonesCompatibilidad = razonesCompatibilidad;
    }
}
