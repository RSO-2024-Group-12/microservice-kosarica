package si.nakupify.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

import java.sql.Timestamp;

@Entity
public class Kosarica extends PanacheEntity {

    public Long id_uporabnik;

    public Long id_izdelek;

    public Float cena;

    public Integer kolicina;

    public Timestamp dodano;

    public Timestamp rezervirano;

    public Kosarica() {}

    public Kosarica(Long id_uporabnik, Long id_izdelek, Float cena, Integer kolicina) {
        this.id_uporabnik = id_uporabnik;
        this.id_izdelek = id_izdelek;
        this.cena = cena;
        this.kolicina = kolicina;
        this.dodano = new Timestamp(System.currentTimeMillis());
        this.rezervirano = new Timestamp(System.currentTimeMillis() + 20 * 60 * 1000);
    }

}
