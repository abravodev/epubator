package it.iiizio.epubator.views.events;

public class ProgressUpdateEvent {

    private final String message;

    public ProgressUpdateEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
