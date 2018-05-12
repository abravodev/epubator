package it.iiizio.epubator.domain.utils;

public class StringUtils {

	public static boolean isNullOrEmpty(String text){
		return text == null || text.equalsIgnoreCase("");
	}

}
