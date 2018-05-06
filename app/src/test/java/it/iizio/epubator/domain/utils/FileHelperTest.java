package it.iizio.epubator.domain.utils;

import org.junit.Test;

import it.iiizio.epubator.domain.utils.FileHelper;

import static junit.framework.Assert.assertEquals;

public class FileHelperTest {

	@Test
	public void getPathWithoutExtension_filenameWithoutExtension_returnsTheSame(){
		// Arrange
		String anyFilenameWithoutExtension = "file";

		// Act
		String filenameWithoutExtension = FileHelper.getPathWithoutExtension(anyFilenameWithoutExtension);

		// Assert
		assertEquals(anyFilenameWithoutExtension, filenameWithoutExtension);
	}
}
