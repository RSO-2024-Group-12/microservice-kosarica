package si.nakupify.service.dto;

public class PairDTO<A, B> {

    private A value;

    private B error;

    public PairDTO() {}

    public PairDTO(A value, B error) {
        this.value = value;
        this.error = error;
    }

    public A getValue() {
        return value;
    }

    public void setValue(A value) {
        this.value = value;
    }

    public B getError() {
        return error;
    }

    public void setError(B error) {
        this.error = error;
    }
}
