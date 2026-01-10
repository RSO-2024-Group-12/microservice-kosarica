package si.nakupify.service.dto;

import java.io.Serializable;

public class RequestDTO implements Serializable {

    private String id_request;

    private String type;

    private Long id_product;

    private Long id_user;

    private Integer quantityAdd;

    private Integer quantityRemove;

    public RequestDTO() {}

    public RequestDTO(String id_request, String type, Long id_product, Long id_user, Integer quantityAdd, Integer quantityRemove) {
        this.id_request = id_request;
        this.type = type;
        this.id_product = id_product;
        this.id_user = id_user;
        this.quantityAdd = quantityAdd;
        this.quantityRemove = quantityRemove;
    }

    public String getId_request() {
        return id_request;
    }

    public void setId_request(String id_request) {
        this.id_request = id_request;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId_product() {
        return id_product;
    }

    public void setId_product(Long id_product) {
        this.id_product = id_product;
    }

    public Long getId_user() {
        return id_user;
    }

    public void setId_user(Long id_user) {
        this.id_user = id_user;
    }

    public Integer getQuantityAdd() {
        return quantityAdd;
    }

    public void setQuantityAdd(Integer quantityAdd) {
        this.quantityAdd = quantityAdd;
    }

    public Integer getQuantityRemove() {
        return quantityRemove;
    }

    public void setQuantityRemove(Integer quantityRemove) {
        this.quantityRemove = quantityRemove;
    }
}
