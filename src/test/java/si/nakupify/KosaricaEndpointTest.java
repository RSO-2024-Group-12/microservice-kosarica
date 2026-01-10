package si.nakupify;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import si.nakupify.service.KosaricaService;
import si.nakupify.service.dto.ElementDTO;
import si.nakupify.service.dto.ErrorDTO;
import si.nakupify.service.dto.KosaricaDTO;
import si.nakupify.service.dto.PairDTO;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

@QuarkusTest
public class KosaricaEndpointTest {

    @InjectMock
    KosaricaService kosaricaService;

    private KosaricaDTO kosaricaDTO(Long id, List<ElementDTO> items) {
        KosaricaDTO kosaricaDTO = new KosaricaDTO();
        kosaricaDTO.setId_uporabnik(id);
        kosaricaDTO.setTenant("org1");
        kosaricaDTO.setKosarica(items);
        return kosaricaDTO;
    }

    private ErrorDTO errorDTO(int code, String message) {
        return new ErrorDTO(code, message);
    }

    @Test
    void getKosaricaUporabnika_test() {
        List<ElementDTO> kosarica = List.of(new ElementDTO(1L, 1L, "Test", 9.99F, 2));
        when(kosaricaService.pridobiKosarico(5L, "org1")).thenReturn(new PairDTO<>(kosaricaDTO(5L, kosarica), null));

        given()
                .accept(ContentType.JSON)
        .when()
                .get("/v1/kosarica/5")
        .then()
                .statusCode(200)
                .body("id_uporabnik", equalTo(5))
                .body("tenant", equalTo("org1"))
                .body("kosarica", hasSize(1));

        verify(kosaricaService).pridobiKosarico(5L, "org1");
    }

    @Test
    void createKosarica_test() {
        List<ElementDTO> kosarica = List.of(new ElementDTO(1L, 1L, "Test", 9.99F, 2),
                new ElementDTO(2L, 2L, "Test", 99.99F, 5));
        when(kosaricaService.dodajKosarico(any(), any())).thenReturn(new PairDTO<>(kosaricaDTO(5L, kosarica), null));

        String requestBody = """
        {
            "id_uporabnik": 5,
            "kosarica": [
                {
                    "id_izdelek": 2,
                    "naziv": "Test",
                    "cena": 99.99,
                    "kolicina": 5
                }
            ]
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/v1/kosarica")
        .then()
                .statusCode(201)
                .body("id_uporabnik", equalTo(5))
                .body("tenant", equalTo("org1"))
                .body("kosarica", hasSize(2));

        verify(kosaricaService).dodajKosarico(any(), any());
    }

    @Test
    void updateKosarica_test() {
        List<ElementDTO> kosarica = List.of(new ElementDTO(1L, 1L, "Test", 9.99F, 5));
        when(kosaricaService.posodobiKosarico(any(), any())).thenReturn(new PairDTO<>(kosaricaDTO(5L, kosarica), null));

        String requestBody = """
        {
            "id_uporabnik": 5,
            "kosarica": [
                {
                    "id_kosarica": 1,
                    "id_izdelek": 1,
                    "naziv": "Test",
                    "cena": 9.99,
                    "kolicina": 5
                }
            ]
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .put("/v1/kosarica")
        .then()
                .statusCode(200)
                .body("id_uporabnik", equalTo(5))
                .body("tenant", equalTo("org1"))
                .body("kosarica", hasSize(1));

        verify(kosaricaService).posodobiKosarico(any(), any());
    }

    @Test
    void deleteKosarica_test() {
        when(kosaricaService.izbrisiKosarico(any(), any())).thenReturn(new PairDTO<>(kosaricaDTO(5L, new ArrayList<>()), null));

        given()
                .accept(ContentType.JSON)
        .when()
                .delete("/v1/kosarica/5")
        .then()
                .statusCode(204);

        verify(kosaricaService).izbrisiKosarico(5L, "org1");
    }
}
