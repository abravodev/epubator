package it.iiizio.epubator.presentation.events;

public class ConversionCanceledEvent {

    private final int result;

    public ConversionCanceledEvent(int result) {
        this.result = result;
    }

    public int getResult() {
        return result;
    }
}
