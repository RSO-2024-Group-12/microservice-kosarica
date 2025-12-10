package si.nakupify.service;

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
import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class KosaricaService {

    @Inject
    KosaricaRepository kosaricaRepository;

    @Inject
    IzdelekClient izdelekClient;
    @Inject
    SkladisceClient skladisceClient;

    private Logger log = Logger.getLogger(KosaricaService.class.getName());

    @PostConstruct
    private void init() {
        log.info("Inicializacija microservice-kosarica.");
    }

    @PreDestroy
    private void destroy() {
        log.info("Ustavitev microservice-kosarica.");
    }

    @Scheduled(every="60s")
    @Transactional
    public void schedule() {
        //Za popravit
        log.info("");
        kosaricaRepository.odstraniPretekle();
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

    public KosaricaDTO pridobiKosarico(Long id_uporabnik) {
        List<Kosarica> kosaricaUporabnika = kosaricaRepository.kosaricaUporabnik(id_uporabnik);
        List<ElementDTO> elementDTOS = new ArrayList<>();

        for (Kosarica kosarica : kosaricaUporabnika) {
            ElementDTO elementDTO = new ElementDTO();
            elementDTO.setId_kosarica(kosarica.id);
            elementDTO.setCena(kosarica.cena);
            elementDTO.setKolicina(kosarica.kolicina);

            IzdelekDTO izdelekDTO = izdelekClient.getIzdelekDTO(kosarica.id_izdelek);
            elementDTO.setId_izdelek(izdelekDTO.getId_izdelek());
            elementDTO.setNaziv(izdelekDTO.getNaziv());

            elementDTOS.add(elementDTO);
        }

        return new KosaricaDTO(id_uporabnik, elementDTOS);
    }

    @Transactional
    public KosaricaDTO dodajKosarico(KosaricaDTO kosaricaDTO) {
        ElementDTO elementDTO = kosaricaDTO.getKosarica().get(0);

        Kosarica kosarica = new Kosarica();
        kosarica.id_uporabnik = kosaricaDTO.getId_uporabnik();
        kosarica.id_izdelek = elementDTO.getId_izdelek();
        kosarica.cena = elementDTO.getCena();
        kosarica.kolicina = elementDTO.getKolicina();

        RequestDTO requestDTO = createRequest("RESERVATION_ADDED", elementDTO.getId_izdelek(),
                kosaricaDTO.getId_uporabnik(), elementDTO.getKolicina(), 0);
        ResponseDTO responseDTO = skladisceClient.postRequestDTO(requestDTO);

        if (!responseDTO.getStatus()) {
            return null;
        }

        kosaricaRepository.persist(kosarica);

        return pridobiKosarico(kosaricaDTO.getId_uporabnik());
    }

    @Transactional
    public KosaricaDTO posodobiKosarico(KosaricaDTO kosaricaDTO) {
        ElementDTO elementDTO = kosaricaDTO.getKosarica().get(0);

        Kosarica kosarica = kosaricaRepository.findById(elementDTO.getId_kosarica());
        if (kosarica == null) {
            log.info("Not Found Error: Elementa košarice z id=" + elementDTO.getId_kosarica() + " ni bilo mogoče najti!");
            return null;
        }

        RequestDTO requestDTO;
        if (elementDTO.getKolicina() == kosarica.kolicina) {
            requestDTO = createRequest("RESERVATION_REMOVED", elementDTO.getId_izdelek(),
                    kosaricaDTO.getId_uporabnik(), 0, elementDTO.getKolicina());
        } else {
            requestDTO = createRequest("RESERVATION_UPDATED", elementDTO.getId_izdelek(),
                    kosaricaDTO.getId_uporabnik(), elementDTO.getKolicina(), kosarica.kolicina);
        }

        ResponseDTO responseDTO = skladisceClient.postRequestDTO(requestDTO);

        if (!responseDTO.getStatus()) {
            return null;
        }

        kosarica.kolicina = elementDTO.getKolicina();

        return pridobiKosarico(kosaricaDTO.getId_uporabnik());
    }

    @Transactional
    public KosaricaDTO izbrisiKosarico(Long id_uporabnik) {
        kosaricaRepository.odstraniKosaricoUporabnika(id_uporabnik);
        return pridobiKosarico(id_uporabnik);
    }

}
