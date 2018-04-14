package it.iiizio.epubator.views.events;

public class ConversionFinishedEvent {

    private final int result;
    private final boolean requestAction;

    public ConversionFinishedEvent(int result, boolean requestAction) {
        this.result = result;
        this.requestAction = requestAction;
    }

    public ConversionFinishedEvent(int result) {
        this(result, false);
    }


    public int getResult() {
        return result;
    }

    public boolean actionRequested(){
        return requestAction;
    }
}
