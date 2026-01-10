package si.nakupify.service;

import io.vertx.core.Vertx;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import si.nakupify.entity.Kosarica;
import si.nakupify.service.client.IzdelekClient;
import si.nakupify.service.client.SkladisceClient;
import si.nakupify.service.dto.*;
import si.nakupify.service.repository.KosaricaRepository;

import java.util.*;
import java.util.logging.Logger;

@ApplicationScoped
public class KosaricaService {

    @Inject
    KosaricaRepository kosaricaRepository;

    @Inject
    IzdelekClient izdelekClient;

    @Inject
    SkladisceClient skladisceClient;

    @Inject
    Vertx vertx;

    private Logger log = Logger.getLogger(KosaricaService.class.getName());

    @PostConstruct
    private void init() {
        log.info("Inicializacija microservice-kosarica.");
    }

    @PreDestroy
    private void destroy() {
        log.info("Ustavitev microservice-kosarica.");
    }

    public void setVertx(Vertx vertx) {
        this.vertx = vertx;
    }

    public RequestDTO createRequest(String type, Long id_product, Long id_user, Integer add, Integer remove) {
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setId_request(UUID.randomUUID().toString());
        requestDTO.setType(type);
        requestDTO.setId_product(id_product);
        requestDTO.setId_user(id_user);
        requestDTO.setQuantityAdd(add);
        requestDTO.setQuantityRemove(remove);
        return requestDTO;
    }

    public PairDTO<KosaricaDTO, ErrorDTO> pridobiKosarico(Long id_uporabnik) {
        List<Kosarica> kosaricaUporabnika = kosaricaRepository.kosaricaUporabnik(id_uporabnik);
        List<ElementDTO> elementDTOS = new ArrayList<>();

        for (Kosarica kosarica : kosaricaUporabnika) {
            ElementDTO elementDTO = new ElementDTO();
            elementDTO.setId_kosarica(kosarica.id);
            elementDTO.setCena(kosarica.cena);
            elementDTO.setKolicina(kosarica.kolicina);

            PairDTO<IzdelekDTO, ErrorDTO> pair = izdelekClient.getIzdelekDTO(kosarica.id_izdelek);
            IzdelekDTO izdelekDTO = pair.getValue();
            ErrorDTO error = pair.getError();

            if (error != null) {
                return new PairDTO<>(null, error);
            }

            elementDTO.setId_izdelek(izdelekDTO.getId_izdelek());
            elementDTO.setNaziv(izdelekDTO.getNaziv());

            elementDTOS.add(elementDTO);
        }

        return new PairDTO<>(new KosaricaDTO(id_uporabnik, elementDTOS), null);
    }

    @Transactional
    public PairDTO<KosaricaDTO, ErrorDTO> dodajKosarico(KosaricaDTO kosaricaDTO) {
        ElementDTO elementDTO = kosaricaDTO.getKosarica().get(0);

        Kosarica kosarica = new Kosarica();
        kosarica.id_uporabnik = kosaricaDTO.getId_uporabnik();
        kosarica.id_izdelek = elementDTO.getId_izdelek();
        kosarica.cena = elementDTO.getCena();
        kosarica.kolicina = elementDTO.getKolicina();

        RequestDTO requestDTO = createRequest("RESERVATION_ADDED", elementDTO.getId_izdelek(),
                kosaricaDTO.getId_uporabnik(), elementDTO.getKolicina(), 0);
        PairDTO<ResponseDTO, ErrorDTO> pair = skladisceClient.postRequestDTO(requestDTO);
        ResponseDTO responseDTO = pair.getValue();
        ErrorDTO error = pair.getError();

        if (error != null) {
            return new PairDTO<>(null, error);
        }

        if (!responseDTO.getStatus()) {
            ErrorDTO errorDTO = new ErrorDTO(409, "Ni bilo možno dodati izdelka v košarico zaradi premalo zaloge!");
            return new PairDTO<>(null, errorDTO);
        }

        kosaricaRepository.persist(kosarica);

        vertx.setTimer((20 * 60 * 1000), t -> {
           Kosarica expired = kosaricaRepository.findById(kosarica.id);
           if (expired != null) {
               RequestDTO requestExpiredDTO = createRequest("RESERVATION_EXPIRED", expired.id_izdelek,
                       expired.id_uporabnik,0, expired.kolicina);
               skladisceClient.postRequestDTO(requestExpiredDTO);

               kosaricaRepository.deleteById(expired.id);
           }
        });

        return pridobiKosarico(kosaricaDTO.getId_uporabnik());
    }

    @Transactional
    public PairDTO<KosaricaDTO, ErrorDTO> posodobiKosarico(KosaricaDTO kosaricaDTO) {
        ElementDTO elementDTO = kosaricaDTO.getKosarica().get(0);

        Kosarica kosarica = kosaricaRepository.findById(elementDTO.getId_kosarica());
        if (kosarica == null) {
            log.info("Not Found Error: Košarice z id=" + elementDTO.getId_kosarica() + " ni bilo mogoče najti");
            ErrorDTO notFoundError = new ErrorDTO(404, "Košarice s podanim id_kosarica ni bilo mogoče najti!");
            return new PairDTO<>(null, notFoundError);
        }

        String str = (elementDTO.getKolicina() == 0) ? "RESERVATION_REMOVED" : "RESERVATION_UPDATED";
        RequestDTO requestDTO = createRequest(str, elementDTO.getId_izdelek(),
                kosaricaDTO.getId_uporabnik(), elementDTO.getKolicina(), kosarica.kolicina);

        PairDTO<ResponseDTO, ErrorDTO> pair = skladisceClient.postRequestDTO(requestDTO);
        ResponseDTO responseDTO = pair.getValue();
        ErrorDTO error = pair.getError();

        if (error != null) {
            return new PairDTO<>(null, error);
        }

        if (!responseDTO.getStatus()) {
            ErrorDTO errorDTO = new ErrorDTO(409, "Ni bilo možno dodati izdelka v košarico zaradi premalo zaloge!");
            return new PairDTO<>(null, errorDTO);
        }

        kosarica.kolicina = elementDTO.getKolicina();

        return pridobiKosarico(kosaricaDTO.getId_uporabnik());
    }

    @Transactional
    public PairDTO<KosaricaDTO, ErrorDTO> izbrisiKosarico(Long id_uporabnik) {
        kosaricaRepository.odstraniKosaricoUporabnika(id_uporabnik);
        return pridobiKosarico(id_uporabnik);
    }

}
