package it.iiizio.epubator.domain.entities;

public class FrontCoverDetails {

	private static final int DEFAULT_WIDTH = 300;
	private static final int DEFAULT_HEIGHT = 410;
	private static final int DEFAULT_BORDER = 10;
	private static final int DEFAULT_FONT_SIZE = 48;

	private final int width;
	private final int height;
	private final int border;
	private final int fontSize;

	public FrontCoverDetails(){
		width = DEFAULT_WIDTH;
		height = DEFAULT_HEIGHT;
		border = DEFAULT_BORDER;
		fontSize = DEFAULT_FONT_SIZE;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getBorder() {
		return border;
	}

	public int getFontSize() {
		return fontSize;
	}
}
