package si.nakupify.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import si.nakupify.entity.Kosarica;
import si.nakupify.service.client.IzdelekClient;
import si.nakupify.service.dto.IzdelekDTO;
import si.nakupify.service.dto.KosaricaDTO;
import si.nakupify.service.dto.ElementDTO;
import si.nakupify.service.repository.KosaricaRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class KosaricaService {

    @Inject
    KosaricaRepository kosaricaRepository;

    @Inject
    IzdelekClient izdelekClient;

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

    @Scheduled(every="60s")
    @Transactional
    public void schedule() {
        kosaricaRepository.odstraniPretekle();
    }

    public KosaricaDTO pridobiKosarico(Long id_uporabnik) {
        List<Kosarica> kosaricaUporabnika = kosaricaRepository.kosaricaUporabnik(id_uporabnik);
        List<ElementDTO> elementDTOS = new ArrayList<>();

        for (Kosarica kosarica : kosaricaUporabnika) {
            IzdelekDTO izdelekDTO = izdelekClient.getIzdelekDTO(kosarica.id_izdelek);
            ElementDTO elementDTO = new ElementDTO(kosarica.id, izdelekDTO.getId_izdelek(), izdelekDTO.getNaziv(), kosarica.cena, kosarica.kolicina);
            elementDTOS.add(elementDTO);
        }

        return new KosaricaDTO(id_uporabnik, elementDTOS);
    }

    @Transactional
    public KosaricaDTO dodajKosarico(KosaricaDTO kosaricaDTO) {
        Long id_uporabnik = kosaricaDTO.getId_uporabnik();

        for (ElementDTO elementDTO : kosaricaDTO.getKosarica()) {
            Kosarica kosarica = new Kosarica(id_uporabnik, elementDTO.getId_izdelek(), elementDTO.getCena(), elementDTO.getKolicina());
            kosaricaRepository.persist(kosarica);
        }

        return pridobiKosarico(id_uporabnik);
    }

    @Transactional
    public KosaricaDTO posodobiKosarico(KosaricaDTO kosaricaDTO) {
        Long id_uporabnik = kosaricaDTO.getId_uporabnik();

        for (ElementDTO elementDTO : kosaricaDTO.getKosarica()) {
            Kosarica kosarica = kosaricaRepository.findById(elementDTO.getId_kosarica());
            if (kosarica == null) {
                log.info("Elementa košarice z id " + elementDTO.getId_kosarica() + " ni bilo mogoče najti!");
                return null;
            }

            if (elementDTO.getKolicina() == 0) {
                kosaricaRepository.deleteById(elementDTO.getId_kosarica());
            }

            kosarica.kolicina = elementDTO.getKolicina();
            kosarica.dodano = new Timestamp(System.currentTimeMillis());
            kosarica.rezervirano = new Timestamp(System.currentTimeMillis() + 10 * 60 * 1000);
        }

        return pridobiKosarico(id_uporabnik);
    }

    @Transactional
    public KosaricaDTO izbrisiKosarico(Long id_uporabnik) {
        kosaricaRepository.odstraniKosaricoUporabnika(id_uporabnik);
        return pridobiKosarico(id_uporabnik);
    }

}
