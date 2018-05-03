package it.iiizio.epubator.domain.exceptions;

public class ConversionException extends Exception {

    private final int status;

    public ConversionException(int status) {
        super();
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
