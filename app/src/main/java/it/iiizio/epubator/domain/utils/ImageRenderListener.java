package it.iiizio.epubator.domain.utils;

import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageRenderListener implements RenderListener {

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
					if (!ZipWriter.addImage("OEBPS/" + imageName, baos.toByteArray())) {
						PdfReadHelper.addImage(imageName);
					}
				}

			}
		} catch (IOException e) {
			System.err.println("Failed to extract image (ImageRenderListener) " + e.getMessage());
		} catch (OutOfMemoryError e) {
			System.err.println("Out of memory in image extraction (ImageRenderListener) " + e.getMessage());
		} catch (NullPointerException e) {
			System.err.println("Null pointer exception in image extraction (ImageRenderListener) " + e.getMessage());
		}
	}

	public void renderText(TextRenderInfo renderInfo) {
	}

	public void beginTextBlock() {
	}

	public void endTextBlock() {
	}
}
