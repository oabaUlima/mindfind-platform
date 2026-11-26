package pe.edu.ulima.mindfind.triage_matching;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import pe.edu.ulima.mindfind.triage_matching.application.internal.outboundservices.acl.ExternalIdentityService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de integración HTTP end-to-end para el Bounded Context {@code triage_matching}:
 * HU-04 (cuestionario y validaciones), HU-05 (matching Top 5 vía ACL con DataSeeder),
 * HU-06 (detección de crisis, líneas de Perú, cierre manteniendo bloqueo HTTP 409) y
 * reconstrucción completa del agregado desde JPA con {@code open-in-view=false}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TriageAndMatchingFlowTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ExternalIdentityService externalIdentityService;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void standardTriageFlowGeneratesTop5ViaAclAndReloadsAggregateWithGet() throws Exception {
        assertThat(externalIdentityService.fetchVerifiedPsychologists())
                .hasSizeGreaterThanOrEqualTo(6)
                .allMatch(p -> p.verificado());

        var payload = """
                {
                  "nombrePaciente": "Lucía Morales",
                  "edadPaciente": 22,
                  "motivoConsulta": "Siento ansiedad académica y estrés laboral constante",
                  "sintomasDeclarados": ["ansiedad", "estrés laboral"],
                  "modalidadPreferida": "VIRTUAL",
                  "presupuestoMaximo": 130.00,
                  "disponibilidadPreferida": "Mañana",
                  "enfoquePreferido": "COGNITIVO_CONDUCTUAL",
                  "preferenciasProfesional": "Trato empático",
                  "consentimientoCifrado": true,
                  "indicadorRiesgoInminente": false
                }
                """;

        var created = send("POST", "/api/v1/triage/evaluations", payload);
        assertThat(created.statusCode()).isEqualTo(201);
        assertThat(created.body())
                .contains("\"estado\":\"COMPLETADA\"")
                .contains("\"protocoloCrisis\":null")
                .contains("Dra. Valeria Mendoza Ríos")
                .contains("\"posicionRanking\":1")
                .contains("\"posicionRanking\":5")
                .doesNotContain("\"posicionRanking\":6");

        var evaluationId = idOf(created.body());

        // Verifica que el GET reconstruye completamente el agregado actualizado en dos pasos (EN_PROGRESO -> COMPLETADA)
        var fetched = send("GET", "/api/v1/triage/evaluations/" + evaluationId, null);
        assertThat(fetched.statusCode()).isEqualTo(200);
        assertThat(fetched.body())
                .contains("\"id\":" + evaluationId)
                .contains("\"estado\":\"COMPLETADA\"")
                .contains("\"sintomasDeclarados\":[\"ansiedad\",\"estrés laboral\"]")
                .contains("Dra. Valeria Mendoza Ríos")
                .contains("\"posicionRanking\":1")
                .contains("\"posicionRanking\":5");

        var matches = send("GET", "/api/v1/triage/evaluations/" + evaluationId + "/matches", null);
        assertThat(matches.statusCode()).isEqualTo(200);
        assertThat(matches.body())
                .contains("Dra. Valeria Mendoza Ríos")
                .contains("\"porcentajeCompatibilidad\":100.0")
                .contains("\"posicionRanking\":1")
                .contains("\"posicionRanking\":5")
                .doesNotContain("\"posicionRanking\":6");
    }

    @Test
    void crisisFlowInterruptsTriageReturnsPeruResourcesAndKeepsBlockingAfterClose() throws Exception {
        // Verificación en tiempo real previa al envío
        var quickCheckCrisis = send("POST", "/api/v1/triage/crisis-check", """
                {
                  "motivoConsulta": "A veces pienso en quitarme la vida",
                  "sintomasDeclarados": ["tristeza"],
                  "preferenciasProfesional": "",
                  "indicadorRiesgoInminente": false
                }
                """);
        assertThat(quickCheckCrisis.statusCode()).isEqualTo(200);
        assertThat(quickCheckCrisis.body())
                .contains("\"crisisDetectada\":true")
                .contains("\"nivelRiesgo\":\"CRITICO\"")
                .contains("113 Opción 5")
                .contains("106");

        var quickCheckSafe = send("POST", "/api/v1/triage/crisis-check", """
                {
                  "motivoConsulta": "Quiero mejorar mi organización diaria",
                  "sintomasDeclarados": ["estrés laboral"],
                  "indicadorRiesgoInminente": false
                }
                """);
        assertThat(quickCheckSafe.statusCode()).isEqualTo(200);
        assertThat(quickCheckSafe.body()).contains("\"crisisDetectada\":false");

        // Evaluación completa con indicador de crisis (con tilde)
        var createdCrisis = send("POST", "/api/v1/triage/evaluations", """
                {
                  "nombrePaciente": "Paciente Reservado",
                  "edadPaciente": 20,
                  "motivoConsulta": "Últimamente he tenido pensamientos de autolesión",
                  "sintomasDeclarados": ["desesperanza", "ansiedad"],
                  "modalidadPreferida": "VIRTUAL",
                  "presupuestoMaximo": 100.00,
                  "disponibilidadPreferida": "Tarde",
                  "consentimientoCifrado": true,
                  "indicadorRiesgoInminente": false
                }
                """);
        assertThat(createdCrisis.statusCode()).isEqualTo(201);
        assertThat(createdCrisis.body())
                .contains("\"estado\":\"INTERRUMPIDA_POR_CRISIS\"")
                .contains("\"nivelRiesgo\":\"CRITICO\"")
                .contains("\"emparejamientoBloqueado\":true")
                .contains("\"alertaCerrada\":false")
                .contains("113 Opción 5")
                .contains("SAMU")
                .contains("106")
                .contains("100")
                .contains("105")
                .contains("\"resultadosCompatibilidad\":[]");

        var evaluationId = idOf(createdCrisis.body());

        // Verifica reconstrucción completa del agregado en crisis vía GET
        var fetchedCrisis = send("GET", "/api/v1/triage/evaluations/" + evaluationId, null);
        assertThat(fetchedCrisis.statusCode()).isEqualTo(200);
        assertThat(fetchedCrisis.body())
                .contains("\"estado\":\"INTERRUMPIDA_POR_CRISIS\"")
                .contains("\"emparejamientoBloqueado\":true")
                .contains("113 Opción 5")
                .contains("\"resultadosCompatibilidad\":[]");

        // /matches debe responder HTTP 409 Conflict
        var blockedMatches = send("GET", "/api/v1/triage/evaluations/" + evaluationId + "/matches", null);
        assertThat(blockedMatches.statusCode()).isEqualTo(409);

        // Cerrar alerta mantiene el bloqueo y el estado INTERRUMPIDA_POR_CRISIS
        var closedAlert = send("PATCH", "/api/v1/triage/evaluations/" + evaluationId + "/crisis/close", null);
        assertThat(closedAlert.statusCode()).isEqualTo(200);
        assertThat(closedAlert.body())
                .contains("\"estado\":\"INTERRUMPIDA_POR_CRISIS\"")
                .contains("\"alertaCerrada\":true")
                .contains("\"emparejamientoBloqueado\":true")
                .contains("\"resultadosCompatibilidad\":[]");

        var matchesAfterClose = send("GET", "/api/v1/triage/evaluations/" + evaluationId + "/matches", null);
        assertThat(matchesAfterClose.statusCode()).isEqualTo(409);
    }

    @Test
    void mandatoryQuestionsReturnExactHu04MessageAndUnknownIdReturns404() throws Exception {
        // modalidadPreferida en blanco -> dominio lanza mensaje exacto (ajuste preventivo 1)
        var missingModality = send("POST", "/api/v1/triage/evaluations", """
                {
                  "edadPaciente": 23,
                  "motivoConsulta": "Estrés en exámenes",
                  "sintomasDeclarados": ["ansiedad"],
                  "modalidadPreferida": "",
                  "presupuestoMaximo": 120.00,
                  "disponibilidadPreferida": "Mañana",
                  "consentimientoCifrado": true
                }
                """);
        assertThat(missingModality.statusCode()).isEqualTo(400);
        assertThat(missingModality.body()).contains("Debe responder esta pregunta para continuar");

        var emptySymptoms = send("POST", "/api/v1/triage/evaluations", """
                {
                  "edadPaciente": 23,
                  "motivoConsulta": "Estrés en exámenes",
                  "sintomasDeclarados": [],
                  "modalidadPreferida": "VIRTUAL",
                  "presupuestoMaximo": 120.00,
                  "disponibilidadPreferida": "Mañana",
                  "consentimientoCifrado": true
                }
                """);
        assertThat(emptySymptoms.statusCode()).isEqualTo(400);
        assertThat(emptySymptoms.body()).contains("Debe responder esta pregunta para continuar");

        assertThat(send("GET", "/api/v1/triage/evaluations/999999", null).statusCode()).isEqualTo(404);
        assertThat(send("GET", "/api/v1/triage/evaluations/999999/matches", null).statusCode()).isEqualTo(404);
    }

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        builder.method(method, body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body));
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static long idOf(String json) {
        var matcher = Pattern.compile("\"id\":(\\d+)").matcher(json);
        assertThat(matcher.find()).as("id in " + json).isTrue();
        return Long.parseLong(matcher.group(1));
    }
}
