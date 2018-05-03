package it.iiizio.epubator.presentation.events;

import it.iiizio.epubator.domain.entities.ConversionSettings;

public class ConversionFinishedEvent extends ConversionStatusChangedEvent {

    private final boolean requestAction;
    private final ConversionSettings settings;

    public ConversionFinishedEvent(int result, ConversionSettings settings, boolean requestAction) {
        super(result);
        this.requestAction = requestAction;
        this.settings = settings;
    }

    public ConversionFinishedEvent(int result, ConversionSettings settings) {
        this(result, settings, false);
    }

    public int getResult() {
        return getConversionStatus();
    }

    public boolean actionRequested(){
        return requestAction;
    }

    public ConversionSettings getSettings() {
        return settings;
    }
}
