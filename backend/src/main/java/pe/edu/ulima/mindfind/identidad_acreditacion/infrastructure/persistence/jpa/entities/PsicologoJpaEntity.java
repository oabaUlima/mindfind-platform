package pe.edu.ulima.mindfind.identidad_acreditacion.infrastructure.persistence.jpa.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.EnfoqueTeorico;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.EstadoVerificacion;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.ModalidadAtencion;
import pe.edu.ulima.mindfind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Modelo de persistencia JPA de {@code Psicologo} (solo columnas, sin reglas de negocio).
 */
@Entity
@Table(name = "psicologos")
public class PsicologoJpaEntity extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false)
    private String nombreCompleto;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    private String telefono;

    @Column(name = "numero_colegiatura", nullable = false, unique = true, length = 6)
    private String numeroColegiatura;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnfoqueTeorico enfoqueTeorico;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "psicologo_especialidades", joinColumns = @JoinColumn(name = "psicologo_id"))
    @Column(name = "especialidad", nullable = false)
    private List<String> especialidades = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "psicologo_modalidades", joinColumns = @JoinColumn(name = "psicologo_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad", nullable = false)
    private Set<ModalidadAtencion> modalidadesAtencion = new LinkedHashSet<>();

    @Column(name = "tarifa_monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifaMonto;

    @Column(name = "tarifa_moneda", nullable = false, length = 3)
    private String tarifaMoneda;

    @Column(nullable = false)
    private String disponibilidadHoraria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVerificacion estadoVerificacion;

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getNumeroColegiatura() {
        return numeroColegiatura;
    }

    public void setNumeroColegiatura(String numeroColegiatura) {
        this.numeroColegiatura = numeroColegiatura;
    }

    public EnfoqueTeorico getEnfoqueTeorico() {
        return enfoqueTeorico;
    }

    public void setEnfoqueTeorico(EnfoqueTeorico enfoqueTeorico) {
        this.enfoqueTeorico = enfoqueTeorico;
    }

    public List<String> getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(List<String> especialidades) {
        this.especialidades = especialidades;
    }

    public Set<ModalidadAtencion> getModalidadesAtencion() {
        return modalidadesAtencion;
    }

    public void setModalidadesAtencion(Set<ModalidadAtencion> modalidadesAtencion) {
        this.modalidadesAtencion = modalidadesAtencion;
    }

    public BigDecimal getTarifaMonto() {
        return tarifaMonto;
    }

    public void setTarifaMonto(BigDecimal tarifaMonto) {
        this.tarifaMonto = tarifaMonto;
    }

    public String getTarifaMoneda() {
        return tarifaMoneda;
    }

    public void setTarifaMoneda(String tarifaMoneda) {
        this.tarifaMoneda = tarifaMoneda;
    }

    public String getDisponibilidadHoraria() {
        return disponibilidadHoraria;
    }

    public void setDisponibilidadHoraria(String disponibilidadHoraria) {
        this.disponibilidadHoraria = disponibilidadHoraria;
    }

    public EstadoVerificacion getEstadoVerificacion() {
        return estadoVerificacion;
    }

    public void setEstadoVerificacion(EstadoVerificacion estadoVerificacion) {
        this.estadoVerificacion = estadoVerificacion;
    }
}
