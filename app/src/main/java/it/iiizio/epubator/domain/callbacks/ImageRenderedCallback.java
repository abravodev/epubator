package it.iiizio.epubator.domain.callbacks;

public interface ImageRenderedCallback {

	void imageRendered(String imageName, byte[] image);
}
