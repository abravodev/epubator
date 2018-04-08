package it.iiizio.epubator.views.events;

public class ConversionFinishedEvent {

    private final int result;

    public ConversionFinishedEvent(int result) {
        this.result = result;
    }

    public int getResult() {
        return result;
    }
}
