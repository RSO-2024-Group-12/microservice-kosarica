package si.nakupify.service.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import si.nakupify.service.KosaricaService;
import si.nakupify.service.dto.ErrorDTO;
import si.nakupify.service.dto.IzdelekDTO;
import si.nakupify.service.dto.PairDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.logging.Logger;

@ApplicationScoped
public class IzdelekClient {

    private HttpClient client;
    private ObjectMapper mapper;

    @ConfigProperty(name="izdelek.url")
    private String izdelkiUrl;

    private Logger log = Logger.getLogger(KosaricaService.class.getName());

    @PostConstruct
    public void init() {
        client = HttpClient.newBuilder().build();
        mapper = new ObjectMapper();
    }

    public PairDTO<IzdelekDTO, ErrorDTO> getIzdelekDTO(Long id_izdelek) {
        try {
            String query = "query ($id: BigInteger) { getIzdelek(id: $id) { id_izdelek naziv cena } }";
            String payload = mapper.writeValueAsString(Map.of(
                    "query", query,
                    "variables", Map.of("id", id_izdelek)
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(izdelkiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(response.body());

            if (response.statusCode() == 404) {
                log.info("HTTP response code 404: Izdelka z id=" + id_izdelek + " ni bilo mogoče najti");
                JsonNode node = root.path("error").path("extensions");
                ErrorDTO error = new ErrorDTO(node.path("code").asInt(), node.path("error").asText());
                return new PairDTO<>(null, error);
            }

            if (response.statusCode() == 503) {
                log.info("HTTP response code 503: Napaka pri komunikaciji z microservice-izdelki");
                ErrorDTO error = new ErrorDTO(503, "Napaka pri komunikaciji z microservice-izdelki.");
                return new PairDTO<>(null, error);
            }

            JsonNode node = root.path("data").path("getIzdelek");
            IzdelekDTO izdelekDTO = mapper.readValue(node.toString(), IzdelekDTO.class);

            return new PairDTO<>(izdelekDTO, null);
        } catch (Exception e) {
            log.severe("Communication error: Napaka pri komunikaciji z microservice-izdelki. Napaka: " + e.getMessage());
            ErrorDTO error = new ErrorDTO(503, "Napaka pri komunikaciji z microservice-izdelki.");
            return new PairDTO<>(null, error);
        }
    }
}
