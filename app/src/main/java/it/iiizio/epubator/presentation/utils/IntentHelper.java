package it.iiizio.epubator.presentation.utils;

import android.content.Intent;
import android.net.Uri;

import java.io.File;

public class IntentHelper {

	public static Intent openDocument(String path, String fileType, String title){
		File directory = new File(path);
		Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
		intent.setDataAndType(Uri.fromFile(directory), fileType);
		return Intent.createChooser(intent, title);
	}

	public static Intent openMarket(String publisher){
		String marketUrl = String.format("market://search?q=pub:%s", publisher);
		return new Intent(Intent.ACTION_VIEW, Uri.parse(marketUrl));
	}
}
