package si.nakupify.service.dto;

import java.util.List;

public class KosaricaDTO {

    private Long id_uporabnik;

    private String tenant;

    private List<ElementDTO> kosarica;

    public KosaricaDTO() {}

    public KosaricaDTO(Long id_uporabnik, String tenant, List<ElementDTO> kosarica) {
        this.id_uporabnik = id_uporabnik;
        this.tenant = tenant;
        this.kosarica = kosarica;
    }

    public Long getId_uporabnik() {
        return id_uporabnik;
    }

    public void setId_uporabnik(Long id_uporabnik) {
        this.id_uporabnik = id_uporabnik;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public List<ElementDTO> getKosarica() {
        return kosarica;
    }

    public void setKosarica(List<ElementDTO> kosarica) {
        this.kosarica = kosarica;
    }
}
