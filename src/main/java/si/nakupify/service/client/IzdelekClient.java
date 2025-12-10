package si.nakupify.service.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logmanager.Level;
import si.nakupify.service.KosaricaService;
import si.nakupify.service.dto.IzdelekDTO;

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

    public IzdelekDTO getIzdelekDTO(Long id_izdelek) {
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
            JsonNode node = root.path("data").path("getIzdelek");

            //Napaka handler

            return new IzdelekDTO(node.path("id_izdelek").asLong(), node.path("naziv").asText(), (float) node.path("cena").asDouble());
        } catch (Exception e) {
            log.log(Level.SEVERE, "Napaka pri komunikaciji z mikrostoritvijo Katalog izdelek. Napaka: ", e.getMessage());
            return new IzdelekDTO();
        }
    }
}
