package pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.aggregates;

import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.events.PsychologistRegisteredEvent;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.events.PsychologistVerifiedEvent;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.EnfoqueTeorico;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.EstadoVerificacion;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.ModalidadAtencion;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.NumeroColegiatura;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.TarifaSesion;
import pe.edu.ulima.mindfind.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Aggregate Root {@code Psicologo} del contexto {@code identidad_acreditacion} (HU-01).
 * Java puro: valida todos los invariantes de negocio y registra eventos de dominio.
 */
public class Psicologo extends AbstractDomainAggregateRoot {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final String MISSING_APPROACH_OR_SPECIALTY_MESSAGE =
            "Debe seleccionar al menos un enfoque teórico y un área de especialidad";

    private final Long id;
    private final String nombreCompleto;
    private final String correo;
    private final String telefono;
    private final NumeroColegiatura numeroColegiatura;
    private final EnfoqueTeorico enfoqueTeorico;
    private final List<String> especialidades;
    private final Set<ModalidadAtencion> modalidadesAtencion;
    private final TarifaSesion tarifaPorSesion;
    private final String disponibilidadHoraria;
    private EstadoVerificacion estadoVerificacion;

    private Psicologo(
            Long id,
            String nombreCompleto,
            String correo,
            String telefono,
            NumeroColegiatura numeroColegiatura,
            EnfoqueTeorico enfoqueTeorico,
            List<String> especialidades,
            Set<ModalidadAtencion> modalidadesAtencion,
            TarifaSesion tarifaPorSesion,
            String disponibilidadHoraria,
            EstadoVerificacion estadoVerificacion) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.telefono = telefono;
        this.numeroColegiatura = numeroColegiatura;
        this.enfoqueTeorico = enfoqueTeorico;
        this.especialidades = List.copyOf(especialidades);
        this.modalidadesAtencion = Set.copyOf(modalidadesAtencion);
        this.tarifaPorSesion = tarifaPorSesion;
        this.disponibilidadHoraria = disponibilidadHoraria;
        this.estadoVerificacion = estadoVerificacion;
    }

    /**
     * Registra un nuevo perfil profesional. Siempre inicia en {@link EstadoVerificacion#PENDIENTE_VERIFICACION}.
     */
    public static Psicologo register(
            String nombreCompleto,
            String correo,
            String telefono,
            NumeroColegiatura numeroColegiatura,
            EnfoqueTeorico enfoqueTeorico,
            Collection<String> especialidades,
            Collection<ModalidadAtencion> modalidadesAtencion,
            TarifaSesion tarifaPorSesion,
            String disponibilidadHoraria) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw new IllegalArgumentException("El nombre completo es obligatorio");
        }
        if (correo == null || correo.isBlank() || !EMAIL_PATTERN.matcher(correo.trim()).matches()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio y debe tener un formato válido");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El teléfono es obligatorio");
        }
        if (numeroColegiatura == null) {
            throw new IllegalArgumentException("Debe ingresar un número de colegiatura válido");
        }

        var especialidadesLimpias = normalizeEspecialidades(especialidades);
        if (enfoqueTeorico == null || especialidadesLimpias.isEmpty()) {
            throw new IllegalArgumentException(MISSING_APPROACH_OR_SPECIALTY_MESSAGE);
        }

        var modalidadesLimpias = normalizeModalidades(modalidadesAtencion);
        if (modalidadesLimpias.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una modalidad de atención (PRESENCIAL o VIRTUAL)");
        }
        if (tarifaPorSesion == null) {
            throw new IllegalArgumentException("La tarifa por sesión debe ser mayor a 0 PEN");
        }
        if (disponibilidadHoraria == null || disponibilidadHoraria.isBlank()) {
            throw new IllegalArgumentException("La disponibilidad horaria es obligatoria");
        }

        var normalizedName = nombreCompleto.trim();
        var normalizedEmail = correo.trim().toLowerCase();
        var psicologo = new Psicologo(
                null,
                normalizedName,
                normalizedEmail,
                telefono.trim(),
                numeroColegiatura,
                enfoqueTeorico,
                especialidadesLimpias,
                modalidadesLimpias,
                tarifaPorSesion,
                disponibilidadHoraria.trim(),
                EstadoVerificacion.PENDIENTE_VERIFICACION);

        psicologo.registerDomainEvent(new PsychologistRegisteredEvent(
                normalizedName,
                normalizedEmail,
                numeroColegiatura.value(),
                Instant.now()));
        return psicologo;
    }

    /** Reconstruye un perfil existente desde persistencia sin re-validar ni emitir eventos. */
    public static Psicologo restore(
            Long id,
            String nombreCompleto,
            String correo,
            String telefono,
            NumeroColegiatura numeroColegiatura,
            EnfoqueTeorico enfoqueTeorico,
            Collection<String> especialidades,
            Collection<ModalidadAtencion> modalidadesAtencion,
            TarifaSesion tarifaPorSesion,
            String disponibilidadHoraria,
            EstadoVerificacion estadoVerificacion) {
        return new Psicologo(
                id,
                nombreCompleto,
                correo,
                telefono,
                numeroColegiatura,
                enfoqueTeorico,
                List.copyOf(especialidades),
                Set.copyOf(modalidadesAtencion),
                tarifaPorSesion,
                disponibilidadHoraria,
                estadoVerificacion);
    }

    /** Acredita institucionalmente al psicólogo pasando su estado a VERIFICADO. */
    public void verify() {
        if (id == null) {
            throw new IllegalStateException("Solo se puede verificar un perfil de psicólogo registrado");
        }
        if (estadoVerificacion == EstadoVerificacion.VERIFICADO) {
            throw new IllegalStateException("El perfil del psicólogo ya se encuentra verificado");
        }
        this.estadoVerificacion = EstadoVerificacion.VERIFICADO;
        registerDomainEvent(new PsychologistVerifiedEvent(
                id,
                nombreCompleto,
                numeroColegiatura.value(),
                Instant.now()));
    }

    private static List<String> normalizeEspecialidades(Collection<String> raw) {
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

    private static Set<ModalidadAtencion> normalizeModalidades(Collection<ModalidadAtencion> raw) {
        if (raw == null) {
            return Set.of();
        }
        return raw.stream()
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    public Long getId() {
        return id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public NumeroColegiatura getNumeroColegiatura() {
        return numeroColegiatura;
    }

    public EnfoqueTeorico getEnfoqueTeorico() {
        return enfoqueTeorico;
    }

    public List<String> getEspecialidades() {
        return especialidades;
    }

    public Set<ModalidadAtencion> getModalidadesAtencion() {
        return modalidadesAtencion;
    }

    public TarifaSesion getTarifaPorSesion() {
        return tarifaPorSesion;
    }

    public String getDisponibilidadHoraria() {
        return disponibilidadHoraria;
    }

    public EstadoVerificacion getEstadoVerificacion() {
        return estadoVerificacion;
    }
}
