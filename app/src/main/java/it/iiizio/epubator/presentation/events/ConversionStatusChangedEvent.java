package it.iiizio.epubator.presentation.events;

public class ConversionStatusChangedEvent {

	private final int conversionStatus;

	public ConversionStatusChangedEvent(int conversionStatus) {
		this.conversionStatus = conversionStatus;
	}

	public int getConversionStatus() {
		return conversionStatus;
	}
}
