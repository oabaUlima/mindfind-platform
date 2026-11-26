package pe.edu.ulima.mindfind;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "cors.allowed-origins=https://mindfind.vercel.app")
class MindFindPlatformApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private OpenAPI openAPI;

    @Test
    void contextLoadsWithMindFindOpenApiMetadata() {
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("MindFind API");
        assertThat(openAPI.getInfo().getDescription()).isEqualTo("MindFind Platform - API REST DDD");
    }

    @Test
    void corsAllowsLocalhostAndConfiguredOrigins() throws Exception {
        var client = HttpClient.newHttpClient();

        var localPreflight = preflight(client, "http://localhost:5173");
        assertThat(localPreflight.headers().firstValue("Access-Control-Allow-Origin"))
                .contains("http://localhost:5173");

        var prodPreflight = preflight(client, "https://mindfind.vercel.app");
        assertThat(prodPreflight.headers().firstValue("Access-Control-Allow-Origin"))
                .contains("https://mindfind.vercel.app");
    }

    private HttpResponse<Void> preflight(HttpClient client, String origin) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v3/api-docs"))
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .header("Origin", origin)
                .header("Access-Control-Request-Method", "GET")
                .build();
        return client.send(request, HttpResponse.BodyHandlers.discarding());
    }
}
