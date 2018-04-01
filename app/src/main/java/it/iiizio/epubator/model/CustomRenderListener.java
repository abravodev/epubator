package it.iiizio.epubator.model;

import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

// RenderListener helper class
public class CustomRenderListener implements RenderListener {
	public void renderImage(ImageRenderInfo renderInfo) {
		try {
			// Get image
			PdfImageObject image = renderInfo.getImage();
			if (image != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				baos.write(image.getImageAsBytes());
				baos.flush();
				baos.close();

				// Check image type
				String imageType = image.getFileType();
				if (imageType == "png" || imageType == "gif" || imageType == "jpg") {
					if (imageType == "jpg") {
						imageType = "jpeg";
					}

					// Save to ePUB
					String imageName = String.format("image%s.%s", renderInfo.getRef().getNumber(), imageType);
					if (!WriteZip.addImage("OEBPS/" + imageName, baos.toByteArray())) {
						ReadPdf.imageList.add(imageName);
					}
				}

			}
		} catch (IOException e) {
			System.err.println("Failed to extract image (CustomRenderListener) " + e.getMessage());
		} catch (OutOfMemoryError e) {
			System.err.println("Out of memory in image extraction (CustomRenderListener) " + e.getMessage());
		} catch (NullPointerException e) {
			System.err.println("Null pointer exception in image extraction (CustomRenderListener) " + e.getMessage());
		}
	}

	// Nothing to do, just required methods
	public void renderText(TextRenderInfo renderInfo) {
	}

	public void beginTextBlock() {
	}

	public void endTextBlock() {
	}
}
