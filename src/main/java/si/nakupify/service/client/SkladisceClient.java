package si.nakupify.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import si.nakupify.service.dto.ErrorDTO;
import si.nakupify.service.dto.PairDTO;
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

    public PairDTO<ResponseDTO, ErrorDTO> postRequestDTO(RequestDTO requestDTO) {
        try {
            String payload = mapper.writeValueAsString(requestDTO);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(skladisceUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                log.info("HTTP response code 404: Zaloge za izdelek z id=" + requestDTO.getId_product() + " ni bilo mogoče najti");
                ErrorDTO error = mapper.readValue(response.body(), ErrorDTO.class);
                return new PairDTO<>(null, error);
            }

            ResponseDTO responseDTO = mapper.readValue(response.body(), ResponseDTO.class);

            return new PairDTO<>(responseDTO, null);
        } catch (Exception e) {
            log.severe("Communication error: Napaka pri komunikaciji z microservice-skladisce. Napaka: " + e.getMessage());
            ErrorDTO error = new ErrorDTO(503, "Napaka pri komunikaciji z microservice-skladisce.");
            return new PairDTO<>(null, error);
        }
    }
}
