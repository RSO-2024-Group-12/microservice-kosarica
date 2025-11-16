package si.nakupify.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import si.nakupify.entity.Kosarica;
import si.nakupify.service.dto.KosaricaDTO;
import si.nakupify.service.dto.ElementDTO;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class KosaricaService {

    @Inject
    KosaricaRepository kosaricaRepository;

    private Logger log = Logger.getLogger(KosaricaService.class.getName());

    @PostConstruct
    private void init() {
        log.info("Inicializacija mikrostoritve košarica.");
        System.out.println("Inicializacija mikrostoritve košarica.");
    }

    @PreDestroy
    private void destroy() {
        log.info("Ustavitev mikrostoritve košarica.");
        System.out.println("Ustavitev mikrostoritve košarica.");
    }

    public KosaricaDTO pridobiKosaricoUporabnika(Long id_uporabnik) {
        List<Kosarica> kosaricaUporabnika = kosaricaRepository.kosaricaUporabnik(id_uporabnik);
        List<ElementDTO> elementDTOS = new ArrayList<>();

        for (Kosarica kosarica : kosaricaUporabnika) {
            long diff = Math.abs(kosarica.cas_dodajanja.getTime() - new Timestamp(System.currentTimeMillis()).getTime());

            if (diff < 3600000) {
                ElementDTO elementDTO = new ElementDTO();
                elementDTO.setId_kosarica(kosarica.id);
                elementDTO.setId_izdelek(kosarica.id_izdelek);
                elementDTO.setKolicina(kosarica.kolicina);

                elementDTOS.add(elementDTO);

            } else {
                izbrisiKosarico(kosarica.id);
            }
        }

        return new KosaricaDTO(id_uporabnik, elementDTOS);
    }

    public boolean validirajKosarico(ElementDTO elementDTO) {
        if (elementDTO.getId_kosarica() == null || elementDTO.getId_izdelek() == null || elementDTO.getKolicina() == null) {
            log.info("Podani manjkajoči podatki!");
            return false;
        }

        return true;
    }

    @Transactional
    public KosaricaDTO dodajVKosarico(Long id_uporabnik, ElementDTO elementDTO) {
        if (!validirajKosarico(elementDTO)) {
            return null;
        }

        Kosarica kosarica = new Kosarica(id_uporabnik, elementDTO.getId_izdelek(), elementDTO.getKolicina());
        kosaricaRepository.persist(kosarica);

        return pridobiKosaricoUporabnika(id_uporabnik);
    }

    @Transactional
    public KosaricaDTO posodobiKosarico(ElementDTO elementDTO) {
        if (!validirajKosarico(elementDTO)) {
            KosaricaDTO response = new KosaricaDTO();
            response.setId_uporabnik((long) -1);
            return response;
        }

        Kosarica kosarica = kosaricaRepository.findById(elementDTO.getId_kosarica());
        if (kosarica == null) {
            log.info("Elementa košarice z id " + elementDTO.getId_kosarica() + " ni bilo mogoče najti!");
            KosaricaDTO response = new KosaricaDTO();
            response.setId_uporabnik((long) -2);
            return response;
        }

        Long id_uporabnik = kosarica.id_uporabnik;
        kosarica.kolicina = elementDTO.getKolicina();
        kosarica.cas_dodajanja = new Timestamp(System.currentTimeMillis());

        return pridobiKosaricoUporabnika(id_uporabnik);
    }

    @Transactional
    public KosaricaDTO izbrisiKosaricoUporabnika(Long id_uporabnik) {
        List<Kosarica> kosaricaUporabnika = kosaricaRepository.kosaricaUporabnik(id_uporabnik);

        for (Kosarica kosarica : kosaricaUporabnika) {
            kosaricaRepository.deleteById(kosarica.id);
        }

        return pridobiKosaricoUporabnika(id_uporabnik);
    }

    @Transactional
    public KosaricaDTO izbrisiKosarico(Long id) {
        Kosarica kosarica = kosaricaRepository.findById(id);
        if (kosarica == null) {
            log.info("Elementa košarice z id " + id + " ni bilo mogoče najti!");
            return null;
        }

        Long id_uporanik = kosarica.id_uporabnik;
        kosaricaRepository.deleteById(id);

        return pridobiKosaricoUporabnika(id_uporanik);
    }
}
