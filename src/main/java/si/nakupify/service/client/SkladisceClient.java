package si.nakupify.service.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import si.nakupify.service.dto.RequestDTO;
import si.nakupify.service.dto.ResponseDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;

@ApplicationScoped
public class SkladisceClient {

    private HttpClient client;
    private ObjectMapper mapper;

    @ConfigProperty(name="skladisce.url")
    private String skladisceUrl;

    private Logger log = Logger.getLogger(SkladisceClient.class.getName());

    @PostConstruct
    public void init() {
        client = HttpClient.newBuilder().build();
        mapper = new ObjectMapper();
    }

    public ResponseDTO postRequestDTO(RequestDTO requestDTO) {
        try {
            String payload = mapper.writeValueAsString(requestDTO);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(skladisceUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = mapper.readTree(response.body());

            if (response.statusCode() == 404) {
                log.info("HTTP response code 404: Zaloga izdelka z id=" + requestDTO.getId_product() + " ne obstaja!");
                return null;
            }

            return new ResponseDTO(node.path("id_request").asText(), node.path("status").asBoolean());
        } catch (Exception e) {
            log.severe("Communication error: Napaka pri komunikaciji z microservice-skladisce. Napaka: " + e.getMessage());
            return null;
        }
    }

}
