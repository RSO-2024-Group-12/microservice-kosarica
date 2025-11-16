package si.nakupify.service;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import si.nakupify.entity.Kosarica;

import java.util.List;

@ApplicationScoped
public class KosaricaRepository implements PanacheRepository<Kosarica> {

    public List<Kosarica> kosaricaUporabnik(Long id_uporabnik) {
        return list("id_uporabnik", id_uporabnik);
    }
}
