package it.iiizio.epubator.domain.utils;

import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

import java.io.IOException;

import it.iiizio.epubator.domain.callbacks.ImageRenderedCallback;
import it.iiizio.epubator.domain.constants.ImageTypes;

public class ImageRenderListener implements RenderListener {

	private final ImageRenderedCallback imageRenderedCallback;

	public ImageRenderListener(ImageRenderedCallback imageRenderedCallback) {
		this.imageRenderedCallback = imageRenderedCallback;
	}

	@Override
	public void renderImage(ImageRenderInfo renderInfo) {
		try {
			PdfImageObject image = renderInfo.getImage();
			if(isNotValidImage(image)){
				return;
			}

			String imageType = image.getFileType();
			if (imageType.equalsIgnoreCase(ImageTypes.JPG)) {
				imageType = ImageTypes.JPEG;
			}

			String imageName = String.format("image%s.%s", renderInfo.getRef().getNumber(), imageType);
			imageRenderedCallback.imageRendered(imageName, image.getImageAsBytes());
		} catch (IOException e) {
			System.err.println("Failed to extract image (ImageRenderListener) " + e.getMessage());
		} catch (OutOfMemoryError e) {
			System.err.println("Out of memory in image extraction (ImageRenderListener) " + e.getMessage());
		} catch (NullPointerException e) {
			System.err.println("Null pointer exception in image extraction (ImageRenderListener) " + e.getMessage());
		}
	}

	private boolean isNotValidImage(PdfImageObject image){
		return image == null || !isValidImage(image);
	}

	private boolean isValidImage(PdfImageObject image){
		String imageType = image.getFileType();
		return imageType.equalsIgnoreCase(ImageTypes.PNG)
			|| imageType.equalsIgnoreCase(ImageTypes.GIF)
			|| imageType.equalsIgnoreCase(ImageTypes.JPG);
	}

	@Override
	public void renderText(TextRenderInfo renderInfo) { }

	@Override
	public void beginTextBlock() { }

	@Override
	public void endTextBlock() { }
}
