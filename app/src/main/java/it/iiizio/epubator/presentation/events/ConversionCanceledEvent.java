package it.iiizio.epubator.presentation.events;

public class ConversionCanceledEvent {

    private final int result;
    private final String progress;

	public ConversionCanceledEvent(int result) {
		this(result, null);
	}

    public ConversionCanceledEvent(int result, String progress) {
        this.result = result;
        this.progress = progress;
    }

    public int getResult() {
        return result;
    }

    public String getProgress() {
        return progress;
    }
}
