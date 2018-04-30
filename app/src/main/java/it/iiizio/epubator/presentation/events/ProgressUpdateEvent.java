package it.iiizio.epubator.presentation.events;

public class ProgressUpdateEvent {

    private final String message;

    public ProgressUpdateEvent(String message) {
        this.message = message;
    }

    public ProgressUpdateEvent() {
        this.message = "";
    }

    public String getMessage() {
        return message;
    }
}
