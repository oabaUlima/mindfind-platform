package pe.edu.ulima.mindfind.identidad_acreditacion.domain;

import org.junit.jupiter.api.Test;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.aggregates.Psicologo;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.events.PsychologistRegisteredEvent;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.events.PsychologistVerifiedEvent;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.EnfoqueTeorico;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.EstadoVerificacion;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.ModalidadAtencion;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.NumeroColegiatura;
import pe.edu.ulima.mindfind.identidad_acreditacion.domain.model.valueobjects.TarifaSesion;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas unitarias puras de dominio (sin Spring ni BD) para validar invariantes de HU-01.
 */
class PsicologoTest {

    private static final NumeroColegiatura COLEGIATURA_VALIDA = new NumeroColegiatura("45678");
    private static final TarifaSesion TARIFA_VALIDA = TarifaSesion.enSoles(new BigDecimal("120"));

    @Test
    void newPsychologistStartsAsPendingVerificationAndRaisesRegisteredEvent() {
        var psicologo = Psicologo.register(
                "Dra. Sofía Ramírez",
                "SOFIA.RAMIREZ@mindfind.pe ",
                "991234567",
                COLEGIATURA_VALIDA,
                EnfoqueTeorico.COGNITIVO_CONDUCTUAL,
                List.of("ansiedad", " estrés laboral ", "ansiedad"),
                List.of(ModalidadAtencion.VIRTUAL),
                TARIFA_VALIDA,
                "Lunes a Viernes 15:00 - 20:00");

        assertThat(psicologo.getEstadoVerificacion()).isEqualTo(EstadoVerificacion.PENDIENTE_VERIFICACION);
        assertThat(psicologo.getCorreo()).isEqualTo("sofia.ramirez@mindfind.pe");
        assertThat(psicologo.getEspecialidades()).containsExactly("ansiedad", "estrés laboral");
        assertThat(psicologo.pullDomainEvents())
                .singleElement()
                .isInstanceOfSatisfying(PsychologistRegisteredEvent.class, event -> {
                    assertThat(event.nombreCompleto()).isEqualTo("Dra. Sofía Ramírez");
                    assertThat(event.correo()).isEqualTo("sofia.ramirez@mindfind.pe");
                    assertThat(event.numeroColegiatura()).isEqualTo("45678");
                });
    }

    @Test
    void colegiaturaMustBe4To6NumericDigits() {
        assertThat(new NumeroColegiatura("1234").value()).isEqualTo("1234");
        assertThat(new NumeroColegiatura(" 123456 ").value()).isEqualTo("123456");

        for (var invalid : List.of("", "   ", "123", "1234567", "12AB", "12-34")) {
            assertThatThrownBy(() -> new NumeroColegiatura(invalid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Debe ingresar un número de colegiatura válido");
        }
        assertThatThrownBy(() -> new NumeroColegiatura(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Debe ingresar un número de colegiatura válido");
    }

    @Test
    void missingApproachOrSpecialtyIsRejectedWithExactMessage() {
        assertThatThrownBy(() -> Psicologo.register(
                "Dra. Sofía Ramírez",
                "sofia@mindfind.pe",
                "991234567",
                COLEGIATURA_VALIDA,
                null,
                List.of("ansiedad"),
                List.of(ModalidadAtencion.VIRTUAL),
                TARIFA_VALIDA,
                "Mañanas"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Debe seleccionar al menos un enfoque teórico y un área de especialidad");

        assertThatThrownBy(() -> Psicologo.register(
                "Dra. Sofía Ramírez",
                "sofia@mindfind.pe",
                "991234567",
                COLEGIATURA_VALIDA,
                EnfoqueTeorico.HUMANISTA,
                List.of("  ", ""),
                List.of(ModalidadAtencion.VIRTUAL),
                TARIFA_VALIDA,
                "Mañanas"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Debe seleccionar al menos un enfoque teórico y un área de especialidad");
    }

    @Test
    void invalidFeeOrMissingModalityIsRejected() {
        assertThatThrownBy(() -> TarifaSesion.enSoles(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La tarifa por sesión debe ser mayor a 0 PEN");

        assertThatThrownBy(() -> TarifaSesion.enSoles(new BigDecimal("-10")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La tarifa por sesión debe ser mayor a 0 PEN");

        assertThatThrownBy(() -> Psicologo.register(
                "Dra. Sofía Ramírez",
                "sofia@mindfind.pe",
                "991234567",
                COLEGIATURA_VALIDA,
                EnfoqueTeorico.SISTEMICO,
                List.of("pareja"),
                List.of(),
                TARIFA_VALIDA,
                "Tardes"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void verifyingTransitionsToVerifiedAndRejectsSecondVerification() {
        var psicologo = Psicologo.restore(
                9L,
                "Dra. Sofía Ramírez",
                "sofia@mindfind.pe",
                "991234567",
                COLEGIATURA_VALIDA,
                EnfoqueTeorico.INTEGRATIVO,
                List.of("duelo"),
                List.of(ModalidadAtencion.PRESENCIAL),
                TARIFA_VALIDA,
                "Tardes",
                EstadoVerificacion.PENDIENTE_VERIFICACION);

        psicologo.verify();

        assertThat(psicologo.getEstadoVerificacion()).isEqualTo(EstadoVerificacion.VERIFICADO);
        assertThat(psicologo.pullDomainEvents())
                .singleElement()
                .isInstanceOfSatisfying(PsychologistVerifiedEvent.class, event -> {
                    assertThat(event.psicologoId()).isEqualTo(9L);
                    assertThat(event.numeroColegiatura()).isEqualTo("45678");
                });

        assertThatThrownBy(psicologo::verify)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("El perfil del psicólogo ya se encuentra verificado");
    }
}
