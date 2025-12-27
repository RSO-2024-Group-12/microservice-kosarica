package si.nakupify.service.dto;

import java.io.Serializable;

public class ResponseDTO implements Serializable {

    private String id_request;

    private Boolean status;

    public ResponseDTO() {}

    public ResponseDTO(String id_request, Boolean status) {
        this.id_request = id_request;
        this.status = status;
    }

    public String getId_request() {
        return id_request;
    }

    public void setId_request(String id_request) {
        this.id_request = id_request;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
