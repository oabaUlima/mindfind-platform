package pe.edu.ulima.mindfind.identidad_acreditacion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import pe.edu.ulima.mindfind.identidad_acreditacion.interfaces.acl.IdentityContextFacade;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de integración HTTP end-to-end para HU-01 (registro, validaciones, unicidad,
 * verificación en vivo, DataSeeder y contrato ACL).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RegisterPsychologistFlowTest {

    @LocalServerPort
    private int port;

    @Autowired
    private IdentityContextFacade identityContextFacade;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void dataSeederLoadsSixVerifiedPsychologistsExposedViaApiAndAcl() throws Exception {
        var response = send("GET", "/api/v1/psychologists?soloVerificados=true", null);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
                .contains("valeria.mendoza@mindfind.pe")
                .contains("alejandro.paredes@mindfind.pe")
                .contains("camila.torres@mindfind.pe")
                .contains("sebastian.huaman@mindfind.pe")
                .contains("lucia.fernandez@mindfind.pe")
                .contains("diego.castillo@mindfind.pe");

        assertThat(identityContextFacade.fetchVerifiedPsychologists())
                .hasSizeGreaterThanOrEqualTo(6)
                .allMatch(dto -> dto.verificado());
    }

    @Test
    void registerVerifyAndRejectDuplicatePsychologistFlow() throws Exception {
        var registerBody = """
                {
                  "nombreCompleto": "Dra. Sofía Ramírez León",
                  "correo": "sofia.ramirez.flow@mindfind.pe",
                  "telefono": "991234567",
                  "numeroColegiatura": "77123",
                  "enfoqueTeorico": "COGNITIVO_CONDUCTUAL",
                  "especialidades": ["ansiedad", "estrés laboral"],
                  "modalidadesAtencion": ["VIRTUAL", "PRESENCIAL"],
                  "tarifaPorSesion": 125.00,
                  "disponibilidadHoraria": "Lunes a Viernes 15:00 - 20:00"
                }
                """;
        var created = send("POST", "/api/v1/psychologists", registerBody);
        assertThat(created.statusCode()).isEqualTo(201);
        assertThat(created.body())
                .contains("Registro completado con éxito. Su perfil está en proceso de validación institucional")
                .contains("\"estadoVerificacion\":\"PENDIENTE_VERIFICACION\"");

        var id = idOf(created.body());

        // Pendiente aún no aparece en filtro soloVerificados ni en el ACL
        assertThat(send("GET", "/api/v1/psychologists?soloVerificados=true", null).body())
                .doesNotContain("sofia.ramirez.flow@mindfind.pe");
        assertThat(identityContextFacade.fetchVerifiedPsychologistById(id)).isEmpty();

        // Duplicado por correo (mayúsculas) o colegiatura -> 409
        var duplicateEmail = send("POST", "/api/v1/psychologists", """
                {
                  "nombreCompleto": "Otro Nombre",
                  "correo": "SOFIA.RAMIREZ.FLOW@mindfind.pe",
                  "telefono": "999888777",
                  "numeroColegiatura": "77999",
                  "enfoqueTeorico": "HUMANISTA",
                  "especialidades": ["autoestima"],
                  "modalidadesAtencion": ["VIRTUAL"],
                  "tarifaPorSesion": 100.00,
                  "disponibilidadHoraria": "Mañanas"
                }
                """);
        assertThat(duplicateEmail.statusCode()).isEqualTo(409);
        assertThat(duplicateEmail.body())
                .contains("El número de colegiatura o correo electrónico ya se encuentra registrado en el sistema");

        var duplicateLicense = send("POST", "/api/v1/psychologists", """
                {
                  "nombreCompleto": "Otro Colega",
                  "correo": "otro.colega@mindfind.pe",
                  "telefono": "999888778",
                  "numeroColegiatura": "77123",
                  "enfoqueTeorico": "HUMANISTA",
                  "especialidades": ["autoestima"],
                  "modalidadesAtencion": ["VIRTUAL"],
                  "tarifaPorSesion": 100.00,
                  "disponibilidadHoraria": "Mañanas"
                }
                """);
        assertThat(duplicateLicense.statusCode()).isEqualTo(409);
        assertThat(duplicateLicense.body())
                .contains("El número de colegiatura o correo electrónico ya se encuentra registrado en el sistema");

        // Verificación en vivo -> 200 y visible en soloVerificados y ACL
        var verified = send("PATCH", "/api/v1/psychologists/" + id + "/verify", null);
        assertThat(verified.statusCode()).isEqualTo(200);
        assertThat(verified.body()).contains("\"estadoVerificacion\":\"VERIFICADO\"");

        assertThat(send("GET", "/api/v1/psychologists?soloVerificados=true", null).body())
                .contains("sofia.ramirez.flow@mindfind.pe");
        assertThat(identityContextFacade.fetchVerifiedPsychologistById(id))
                .isPresent()
                .get()
                .satisfies(dto -> assertThat(dto.verificado()).isTrue());

        // Segunda verificación -> 409
        var secondVerify = send("PATCH", "/api/v1/psychologists/" + id + "/verify", null);
        assertThat(secondVerify.statusCode()).isEqualTo(409);
    }

    @Test
    void validationErrorsReturn400WithExactHu01MessagesAndUnknownIdReturns404() throws Exception {
        var invalidLicense = send("POST", "/api/v1/psychologists", """
                {
                  "nombreCompleto": "Dr. Test Colegiatura",
                  "correo": "test.col@mindfind.pe",
                  "telefono": "991112223",
                  "numeroColegiatura": "12A",
                  "enfoqueTeorico": "COGNITIVO_CONDUCTUAL",
                  "especialidades": ["ansiedad"],
                  "modalidadesAtencion": ["VIRTUAL"],
                  "tarifaPorSesion": 100.00,
                  "disponibilidadHoraria": "Tardes"
                }
                """);
        assertThat(invalidLicense.statusCode()).isEqualTo(400);
        assertThat(invalidLicense.body()).contains("Debe ingresar un número de colegiatura válido");

        var blankApproach = send("POST", "/api/v1/psychologists", """
                {
                  "nombreCompleto": "Dr. Test Enfoque",
                  "correo": "test.enfoque@mindfind.pe",
                  "telefono": "991112224",
                  "numeroColegiatura": "88441",
                  "enfoqueTeorico": "",
                  "especialidades": ["ansiedad"],
                  "modalidadesAtencion": ["VIRTUAL"],
                  "tarifaPorSesion": 100.00,
                  "disponibilidadHoraria": "Tardes"
                }
                """);
        assertThat(blankApproach.statusCode()).isEqualTo(400);
        assertThat(blankApproach.body()).contains("Debe seleccionar al menos un enfoque teórico y un área de especialidad");

        var emptySpecialties = send("POST", "/api/v1/psychologists", """
                {
                  "nombreCompleto": "Dr. Test Especialidad",
                  "correo": "test.esp@mindfind.pe",
                  "telefono": "991112225",
                  "numeroColegiatura": "88442",
                  "enfoqueTeorico": "HUMANISTA",
                  "especialidades": [],
                  "modalidadesAtencion": ["PRESENCIAL"],
                  "tarifaPorSesion": 100.00,
                  "disponibilidadHoraria": "Tardes"
                }
                """);
        assertThat(emptySpecialties.statusCode()).isEqualTo(400);
        assertThat(emptySpecialties.body()).contains("Debe seleccionar al menos un enfoque teórico y un área de especialidad");

        assertThat(send("GET", "/api/v1/psychologists/999999", null).statusCode()).isEqualTo(404);
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
