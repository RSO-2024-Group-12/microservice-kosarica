package si.nakupify.service.dto;

public class ErrorDTO {

    private Integer errorCode;

    private String error;

    public ErrorDTO() {}

    public ErrorDTO(Integer errorCode, String error) {
        this.errorCode = errorCode;
        this.error = error;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
