package si.nakupify.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

import java.sql.Timestamp;

@Entity
public class Kosarica extends PanacheEntity {

    public Long id_uporabnik;

    public Long id_izdelek;

    public Integer kolicina;

    public Timestamp cas_dodajanja;

    public Kosarica() {}

    public Kosarica(Long id_uporabnik, Long id_izdelek, Integer kolicina) {
        this.id_uporabnik = id_uporabnik;
        this.id_izdelek = id_izdelek;
        this.kolicina = kolicina;
        this.cas_dodajanja = new Timestamp(System.currentTimeMillis());
    }

}
