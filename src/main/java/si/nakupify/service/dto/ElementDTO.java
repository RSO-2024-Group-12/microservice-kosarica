package si.nakupify.service.dto;

import java.sql.Timestamp;

public class ElementDTO {

    private Long id_kosarica;

    private Long id_izdelek;

    private Integer kolicina;

    public ElementDTO() {}

    public ElementDTO(Long id_kosarica, Long id_izdelek, Integer kolicina) {
        this.id_kosarica = id_kosarica;
        this.id_izdelek = id_izdelek;
        this.kolicina = kolicina;
    }

    public Long getId_kosarica() {
        return id_kosarica;
    }

    public void setId_kosarica(Long id_kosarica) {
        this.id_kosarica = id_kosarica;
    }

    public Long getId_izdelek() {
        return id_izdelek;
    }

    public void setId_izdelek(Long id_izdelek) {
        this.id_izdelek = id_izdelek;
    }

    public Integer getKolicina() {
        return kolicina;
    }

    public void setKolicina(Integer kolicina) {
        this.kolicina = kolicina;
    }
}
