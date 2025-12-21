package si.nakupify.service.dto;

import java.sql.Date;
import java.util.List;

public class IzdelekDTO {

    private Long id_izdelek;

    private String naziv;

    private Float cena;

    public IzdelekDTO() {}

    public IzdelekDTO(Long id_izdelek, String naziv, Float cena) {
        this.id_izdelek = id_izdelek;
        this.naziv = naziv;
        this.cena = cena;
    }

    public Long getId_izdelek() {
        return id_izdelek;
    }

    public void setId_izdelek(Long id_izdelek) {
        this.id_izdelek = id_izdelek;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public Float getCena() {
        return cena;
    }

    public void setCena(Float cena) {
        this.cena = cena;
    }
}
