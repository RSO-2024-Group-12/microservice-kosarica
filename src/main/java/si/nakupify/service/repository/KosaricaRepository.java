package si.nakupify.service.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import si.nakupify.entity.Kosarica;

import java.sql.Timestamp;
import java.util.List;

@ApplicationScoped
public class KosaricaRepository implements PanacheRepository<Kosarica> {

    public List<Kosarica> kosaricaUporabnik(Long id_uporabnik) {
        return list("id_uporabnik", id_uporabnik);
    }

    public void odstraniKosaricoUporabnika(Long id_uporabnik) {
        delete("id_uporabnik", id_uporabnik);
    }

    public void odstraniPretekle() {
        delete("rezervirano < ?1", new Timestamp(System.currentTimeMillis()));
    }
}
