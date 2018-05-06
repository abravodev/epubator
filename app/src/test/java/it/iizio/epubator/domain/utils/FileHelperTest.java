package it.iizio.epubator.domain.utils;

import org.junit.jupiter.api.Test;

import it.iiizio.epubator.domain.utils.FileHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
