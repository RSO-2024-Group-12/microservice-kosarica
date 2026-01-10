package si.nakupify;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import si.nakupify.entity.Kosarica;
import si.nakupify.service.KosaricaService;
import si.nakupify.service.client.IzdelekClient;
import si.nakupify.service.client.SkladisceClient;
import si.nakupify.service.dto.*;
import si.nakupify.service.repository.KosaricaRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
public class KosaricaServiceTest {

    @InjectSpy
    KosaricaService kosaricaService;

    @InjectMock
    KosaricaRepository kosaricaRepository;

    @InjectMock
    IzdelekClient izdelekClient;

    @InjectMock
    SkladisceClient skladisceClient;

    Vertx vertxMock;

    @BeforeEach
    void setup() {
        vertxMock = Mockito.mock(Vertx.class);
        kosaricaService.setVertx(vertxMock);
    }

    private Kosarica kosaricaEntity(Long id, Long id_uporabnik, Long id_izdelek) {
        Kosarica kosarica = new Kosarica();
        kosarica.id = id;
        kosarica.id_uporabnik = id_uporabnik;
        kosarica.id_izdelek = id_izdelek;
        kosarica.tenant = "org1";
        kosarica.cena = 9.99F;
        kosarica.kolicina = 1;
        return kosarica;
    }

    private KosaricaDTO makeKosaricaDTO(Long id, List<ElementDTO> items) {
        KosaricaDTO kosaricaDTO = new KosaricaDTO();

        kosaricaDTO.setId_uporabnik(id);
        kosaricaDTO.setTenant("org1");
        kosaricaDTO.setKosarica(items);

        return kosaricaDTO;
    }

    @Test
    void pridobiKosarico_test() {
        List<Kosarica> kosarica = List.of(kosaricaEntity(1L, 5L, 1L));
        when(kosaricaRepository.kosaricaUporabnik(5L, "org1")).thenReturn(kosarica);

        IzdelekDTO izdelekDTO = new IzdelekDTO(1L, "Test", 9.99F);
        when(izdelekClient.getIzdelekDTO(1L)).thenReturn(new PairDTO<>(izdelekDTO, null));

        PairDTO<KosaricaDTO, ErrorDTO> result = kosaricaService.pridobiKosarico(5L, "org1");

        assertNotNull(result);
        assertNotNull(result.getValue());
        assertNull(result.getError());

        KosaricaDTO kosaricaDTO = result.getValue();

        assertEquals(5L, kosaricaDTO.getId_uporabnik());
        assertEquals("org1", kosaricaDTO.getTenant());
        assertEquals(1, kosaricaDTO.getKosarica().size());

        verify(kosaricaRepository).kosaricaUporabnik(5L, "org1");
        verify(izdelekClient).getIzdelekDTO(1L);
    }

    @Test
    void dodajKosarico_test() {
        ResponseDTO responseDTO = new ResponseDTO("test", true);
        when(skladisceClient.postRequestDTO(any())).thenReturn(new PairDTO<>(responseDTO, null));

        doAnswer(invocation -> {
            Kosarica kosarica = invocation.getArgument(0);
            kosarica.id = 5L;
            return null;
        }).when(kosaricaRepository).persist(any(Kosarica.class));

        when(vertxMock.setTimer(anyLong(), any())).thenReturn(1L);

        KosaricaDTO mockDTO = new KosaricaDTO();
        mockDTO.setId_uporabnik(5L);
        mockDTO.setTenant("org1");
        mockDTO.setKosarica(List.of(new ElementDTO(5L, 1L, "Test", 9.99F, 1)));

        doReturn(new PairDTO<>(mockDTO, null)).when(kosaricaService).pridobiKosarico(anyLong(), any());

        List<ElementDTO> items = List.of(new ElementDTO(null, 1L, "Test", 9.99F, 1));
        PairDTO<KosaricaDTO, ErrorDTO> result = kosaricaService.dodajKosarico(makeKosaricaDTO(5L, items), "org1");

        assertNotNull(result);
        assertNotNull(result.getValue());
        assertNull(result.getError());

        KosaricaDTO kosaricaDTO = result.getValue();

        assertEquals(5L, kosaricaDTO.getId_uporabnik());
        assertEquals("org1", kosaricaDTO.getTenant());
        assertEquals(1, kosaricaDTO.getKosarica().size());

        verify(skladisceClient).postRequestDTO(any());
        verify(kosaricaRepository).persist(any(Kosarica.class));
        verify(vertxMock).setTimer(anyLong(), any());
        verify(kosaricaService).pridobiKosarico(5L, "org1");
    }

    @Test
    void posodobiKosarico_test() {
        when(kosaricaRepository.findById(5L)).thenReturn(kosaricaEntity(5L, 5L, 1L));

        ResponseDTO responseDTO = new ResponseDTO("test", true);
        when(skladisceClient.postRequestDTO(any())).thenReturn(new PairDTO<>(responseDTO, null));

        KosaricaDTO mockDTO = new KosaricaDTO();
        mockDTO.setId_uporabnik(5L);
        mockDTO.setTenant("org1");
        mockDTO.setKosarica(List.of(new ElementDTO(5L, 1L, "Test", 9.99F, 5)));

        doReturn(new PairDTO<>(mockDTO, null)).when(kosaricaService).pridobiKosarico(anyLong(), any());

        List<ElementDTO> items = List.of(new ElementDTO(5L, 1L, "Test", 9.99F, 5));
        PairDTO<KosaricaDTO, ErrorDTO> result = kosaricaService.posodobiKosarico(makeKosaricaDTO(5L, items), "org1");

        assertNotNull(result);
        assertNotNull(result.getValue());
        assertNull(result.getError());

        KosaricaDTO kosaricaDTO = result.getValue();

        assertEquals(5L, kosaricaDTO.getId_uporabnik());
        assertEquals("org1", kosaricaDTO.getTenant());
        assertEquals(1, kosaricaDTO.getKosarica().size());

        verify(kosaricaRepository).findById(5L);
        verify(skladisceClient).postRequestDTO(any());
        verify(kosaricaService).pridobiKosarico(5L, "org1");
    }

    @Test
    void izbrisiKosarico_test() {
        KosaricaDTO mockDTO = new KosaricaDTO();
        mockDTO.setId_uporabnik(5L);
        mockDTO.setKosarica(Collections.emptyList());

        doReturn(new PairDTO<>(mockDTO, null)).when(kosaricaService).pridobiKosarico(anyLong(), any());

        PairDTO<KosaricaDTO, ErrorDTO> result = kosaricaService.izbrisiKosarico(5L, "org1");

        assertNotNull(result);
        assertNotNull(result.getValue());
        assertNull(result.getError());
        assertEquals(5L, result.getValue().getId_uporabnik());
        assertTrue(result.getValue().getKosarica().isEmpty());

        verify(kosaricaRepository).odstraniKosaricoUporabnika(5L);
        verify(kosaricaService).pridobiKosarico(5L, "org1");
    }
}
