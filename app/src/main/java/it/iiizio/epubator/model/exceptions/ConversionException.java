package it.iiizio.epubator.model.exceptions;

public class ConversionException extends Exception {

    private int status;

    public ConversionException(int status) {
        super();
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
