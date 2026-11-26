package pe.edu.ulima.mindfind.triage_matching.domain;

import org.junit.jupiter.api.Test;
import pe.edu.ulima.mindfind.triage_matching.domain.model.aggregates.EvaluacionTriage;
import pe.edu.ulima.mindfind.triage_matching.domain.model.events.CrisisIndicatorDetectedEvent;
import pe.edu.ulima.mindfind.triage_matching.domain.model.events.MatchingRankingGeneratedEvent;
import pe.edu.ulima.mindfind.triage_matching.domain.model.events.TriageEvaluationCompletedEvent;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.EstadoEvaluacion;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.ModalidadPreferida;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.NivelRiesgo;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.PorcentajeCompatibilidad;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.PresupuestoMaximo;
import pe.edu.ulima.mindfind.triage_matching.domain.model.valueobjects.PsicologoRef;
import pe.edu.ulima.mindfind.triage_matching.domain.services.DetectorCrisis;
import pe.edu.ulima.mindfind.triage_matching.domain.services.MotorMatching;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas unitarias puras de dominio (sin Spring ni BD) para validar HU-04, HU-06 y HU-05.
 */
class EvaluacionTriageAndMatchingTest {

    private final DetectorCrisis detectorCrisis = new DetectorCrisis();
    private final MotorMatching motorMatching = new MotorMatching();

    @Test
    void missingRequiredQuestionsRejectsWithExactHu04Message() {
        assertThatThrownBy(() -> EvaluacionTriage.iniciar(
                "Lucía",
                22,
                "   ",
                List.of("ansiedad"),
                ModalidadPreferida.VIRTUAL,
                PresupuestoMaximo.enSoles(new BigDecimal("120")),
                "Mañana",
                null,
                null,
                true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Debe responder esta pregunta para continuar");

        assertThatThrownBy(() -> EvaluacionTriage.iniciar(
                "Lucía",
                22,
                "Ansiedad laboral",
                List.of("  ", ""),
                ModalidadPreferida.VIRTUAL,
                PresupuestoMaximo.enSoles(new BigDecimal("120")),
                "Mañana",
                null,
                null,
                true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Debe responder esta pregunta para continuar");

        assertThatThrownBy(() -> EvaluacionTriage.iniciar(
                "Lucía",
                22,
                "Ansiedad laboral",
                List.of("ansiedad"),
                null,
                PresupuestoMaximo.enSoles(new BigDecimal("120")),
                "Mañana",
                null,
                null,
                true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Debe responder esta pregunta para continuar");
    }

    @Test
    void detectsCrisisWithAndWithoutAccentsAndBlocksMatchingEvenAfterClosingAlert() {
        var textosCrisis = List.of(
                "He pensado en autolesión recientemente",
                "AUTOLESION constante",
                "Siento ganas de hacerme daño",
                "No quiero hacerme dano",
                "Pensamientos suicidas cada noche",
                "Quiero quitarme la vida",
                "Sufro violencia intrafamiliar en casa");

        for (var texto : textosCrisis) {
            var deteccion = detectorCrisis.evaluar(texto, List.of("tristeza"), null, false);
            assertThat(deteccion.crisisDetectada()).as("crisis for: " + texto).isTrue();
            assertThat(deteccion.nivelRiesgo()).isEqualTo(NivelRiesgo.CRITICO);
        }

        var flagDeteccion = detectorCrisis.evaluar("Me siento abrumado", List.of("estrés"), null, true);
        assertThat(flagDeteccion.crisisDetectada()).isTrue();
        assertThat(flagDeteccion.nivelRiesgo()).isEqualTo(NivelRiesgo.CRITICO);

        var evaluacion = EvaluacionTriage.iniciar(
                null,
                21,
                "He tenido ideas de hacerme daño desde hace semanas",
                List.of("desesperanza"),
                ModalidadPreferida.VIRTUAL,
                PresupuestoMaximo.enSoles(new BigDecimal("100")),
                "Tarde",
                null,
                null,
                true);

        boolean hayCrisis = evaluacion.evaluarRiesgoCrisis(detectorCrisis, false);

        assertThat(hayCrisis).isTrue();
        assertThat(evaluacion.getEstado()).isEqualTo(EstadoEvaluacion.INTERRUMPIDA_POR_CRISIS);
        assertThat(evaluacion.getProtocoloCrisis()).isNotNull();
        assertThat(evaluacion.getProtocoloCrisis().nivelRiesgo()).isEqualTo(NivelRiesgo.CRITICO);
        assertThat(evaluacion.getProtocoloCrisis().emparejamientoBloqueado()).isTrue();
        assertThat(evaluacion.getProtocoloCrisis().alertaCerrada()).isFalse();
        assertThat(evaluacion.getProtocoloCrisis().recursosEmergencia())
                .extracting(r -> r.lineaTelefonica())
                .contains("113 Opción 5", "106", "100", "105");
        assertThat(evaluacion.pullDomainEvents())
                .singleElement()
                .isInstanceOf(CrisisIndicatorDetectedEvent.class);

        // Bloqueado inmediatamente tras crisis
        assertThatThrownBy(() -> evaluacion.registrarResultadosMatching(List.of()))
                .isInstanceOf(IllegalStateException.class);

        // Al cerrar la alerta, el bloqueo y el estado INTERRUMPIDA_POR_CRISIS se mantienen intactos
        evaluacion.cerrarAlertaCrisis();
        assertThat(evaluacion.getProtocoloCrisis().alertaCerrada()).isTrue();
        assertThat(evaluacion.getProtocoloCrisis().emparejamientoBloqueado()).isTrue();
        assertThat(evaluacion.getEstado()).isEqualTo(EstadoEvaluacion.INTERRUMPIDA_POR_CRISIS);

        assertThatThrownBy(() -> evaluacion.registrarResultadosMatching(List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void motorMatchingFiltersVerifiedCalculatesWeightedScoreAndReturnsTop5() {
        var evaluacion = EvaluacionTriage.iniciar(
                "Carlos",
                25,
                "Ansiedad y estrés en el trabajo",
                List.of("ansiedad", "estrés laboral"),
                ModalidadPreferida.VIRTUAL,
                PresupuestoMaximo.enSoles(new BigDecimal("130.00")),
                "Mañana",
                "COGNITIVO_CONDUCTUAL",
                null,
                true);

        assertThat(evaluacion.evaluarRiesgoCrisis(detectorCrisis, false)).isFalse();

        var candidatos = List.of(
                // No verificado: debe ser excluido aunque coincida al 100%
                new PsicologoRef(1L, "No Verificado", "COGNITIVO_CONDUCTUAL",
                        List.of("ansiedad", "estrés laboral"), List.of(ModalidadPreferida.VIRTUAL),
                        new BigDecimal("80.00"), "Mañana", false),
                // Match perfecto: 40 + 20 + 20 + 10 + 10 = 100.0%
                new PsicologoRef(2L, "Dra. Perfecta", "COGNITIVO_CONDUCTUAL",
                        List.of("ansiedad", "estrés laboral"), List.of(ModalidadPreferida.VIRTUAL, ModalidadPreferida.PRESENCIAL),
                        new BigDecimal("120.00"), "Lunes a Viernes 09:00 - 14:00 (Mañana)", true),
                // Match parcial (horario distinto): 40 + 20 + 20 + 10 + 0 = 90.0%
                new PsicologoRef(3L, "Dr. Noche", "COGNITIVO_CONDUCTUAL",
                        List.of("ansiedad", "estrés laboral"), List.of(ModalidadPreferida.VIRTUAL),
                        new BigDecimal("130.00"), "Lunes a Viernes 18:00 - 22:00 (Noche)", true),
                // Match parcial (1 síntoma, otro enfoque): 20 + 20 + 20 + 0 + 10 = 70.0%
                new PsicologoRef(4L, "Dra. Parcial", "INTEGRATIVO",
                        List.of("ansiedad", "duelo"), List.of(ModalidadPreferida.VIRTUAL),
                        new BigDecimal("70.00"), "Mañana", true),
                // Match bajo (presupuesto excedido 180 > 130*1.25): 0 + 0 + 0 + 0 + 0 = 0.0%
                new PsicologoRef(5L, "Dr. Lejano", "PSICODINAMICO",
                        List.of("pareja"), List.of(ModalidadPreferida.PRESENCIAL),
                        new BigDecimal("180.00"), "Noche", true),
                // Match intermedio: 40 + 20 + 0 + 0 + 10 = 70.0% (tarifa 170)
                new PsicologoRef(6L, "Dra. Sistémica", "SISTEMICO",
                        List.of("ansiedad", "estrés laboral"), List.of(ModalidadPreferida.VIRTUAL),
                        new BigDecimal("170.00"), "Mañana/Tarde", true),
                // Sexto verificado (debe quedar fuera del Top 5)
                new PsicologoRef(7L, "Dr. Sexto", "HUMANISTA",
                        List.of("ansiedad"), List.of(ModalidadPreferida.PRESENCIAL),
                        new BigDecimal("90.00"), "Tarde", true)
        );

        var top5 = motorMatching.calcularTop5(evaluacion, candidatos);

        assertThat(top5).hasSize(5);
        assertThat(top5).extracting(r -> r.idPsicologo()).doesNotContain(1L, 5L);
        assertThat(top5.get(0).idPsicologo()).isEqualTo(2L);
        assertThat(top5.get(0).porcentajeCompatibilidad()).isEqualTo(new PorcentajeCompatibilidad(100.0));
        assertThat(top5.get(0).posicionRanking()).isEqualTo(1);
        assertThat(top5.get(0).esMatchParcial()).isFalse();

        assertThat(top5.get(1).idPsicologo()).isEqualTo(3L);
        assertThat(top5.get(1).porcentajeCompatibilidad()).isEqualTo(new PorcentajeCompatibilidad(90.0));
        assertThat(top5.get(1).posicionRanking()).isEqualTo(2);
        assertThat(top5.get(1).esMatchParcial()).isTrue();

        evaluacion.registrarResultadosMatching(top5);
        assertThat(evaluacion.getEstado()).isEqualTo(EstadoEvaluacion.COMPLETADA);
        assertThat(evaluacion.getResultadosCompatibilidad()).hasSize(5);
        assertThat(evaluacion.pullDomainEvents())
                .hasSize(2)
                .satisfiesExactly(
                        first -> assertThat(first).isInstanceOf(TriageEvaluationCompletedEvent.class),
                        second -> assertThat(second).isInstanceOf(MatchingRankingGeneratedEvent.class));
    }

    @Test
    void neutralFavorableApproachScoreAppliesWhenApproachNotSpecified() {
        var evaluacion = EvaluacionTriage.iniciar(
                "Ana",
                24,
                "Manejo de estrés",
                List.of("ansiedad"),
                ModalidadPreferida.VIRTUAL,
                PresupuestoMaximo.enSoles(new BigDecimal("100.00")),
                "Mañana",
                null,
                null,
                true);

        var psicologo = new PsicologoRef(
                10L,
                "Dra. Neutral",
                "HUMANISTA",
                List.of("ansiedad"),
                List.of(ModalidadPreferida.VIRTUAL),
                new BigDecimal("90.00"),
                "Mañana",
                true);

        var top = motorMatching.calcularTop5(evaluacion, List.of(psicologo));

        assertThat(top).singleElement().satisfies(resultado -> {
            assertThat(resultado.porcentajeCompatibilidad().valor()).isEqualTo(100.0);
            assertThat(resultado.esMatchParcial()).isFalse();
        });
    }
}
