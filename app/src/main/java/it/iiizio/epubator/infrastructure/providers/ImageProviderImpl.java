package it.iiizio.epubator.infrastructure.providers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.google.common.base.Strings;

import java.io.ByteArrayOutputStream;

import it.iiizio.epubator.R;
import it.iiizio.epubator.domain.entities.FrontCoverDetails;

public class ImageProviderImpl implements ImageProvider {

	private final Context context;

	public ImageProviderImpl(Context context) {
		this.context = context;
	}

	@Override
	public byte[] addSelectedCoverImage(String coverImageFilename, FrontCoverDetails coverDetails) {
		if(Strings.isNullOrEmpty(coverImageFilename)){
			return null;
		}

		Bitmap bitmap = Bitmap.createBitmap(coverDetails.getWidth(), coverDetails.getHeight(), Bitmap.Config.RGB_565);
		Canvas coverImage = new Canvas(bitmap);

		final BitmapFactory.Options options = getBitmapOptions(coverImageFilename, coverDetails.getWidth(), coverDetails.getHeight());
		Bitmap coverImageFile = BitmapFactory.decodeFile(coverImageFilename, options);

		if(coverImageFile == null){
			return null;
		}

		coverImage.drawBitmap(coverImageFile,
				null,
				new Rect(0, 0, coverDetails.getWidth(), coverDetails.getHeight()),
				new Paint(Paint.FILTER_BITMAP_FLAG));

		return saveBitmapAsPng(bitmap);
	}

	@Override
	public byte[] addDefaultCover(String[] titleWords, boolean showLogoOnCover, FrontCoverDetails coverDetails) {
		Bitmap bitmap = Bitmap.createBitmap(coverDetails.getWidth(), coverDetails.getHeight(), Bitmap.Config.RGB_565);
		Canvas coverImage = new Canvas(bitmap);

		coverImage = applyGrayRectangle(coverImage, coverDetails.getWidth(), coverDetails.getHeight());
		if (showLogoOnCover) {
			paintLogoOnTheCover(coverDetails, coverImage);
		}

		paintTitleOnTheCover(titleWords, coverDetails, coverImage);
		return saveBitmapAsPng(bitmap);
	}

	private void paintTitleOnTheCover(String[] words, FrontCoverDetails coverDetails, Canvas coverImage) {
		Paint paint = new Paint();
		paint.setTextSize(coverDetails.getFontSize());
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);

		float newline = paint.getFontSpacing();
		float x = coverDetails.getBorder();
		float y = newline;

		for (String word: words) {
			float wordLength = paint.measureText(word + " ");

			// Line wrap
			if ((x > coverDetails.getBorder()) && ((x + wordLength) > coverDetails.getWidth())) {
				x = coverDetails.getBorder();
				y += newline;
			}

			// Word wrap
			while ((x + wordLength) > coverDetails.getWidth()) {
				int maxChar = (int) (word.length() * (coverDetails.getWidth() - coverDetails.getBorder() - x) / paint.measureText(word));
				coverImage.drawText(word.substring(0, maxChar), x, y, paint);
				word = word.substring(maxChar);
				wordLength = paint.measureText(word + " ");
				x = coverDetails.getBorder();
				y += newline;
			}

			coverImage.drawText(word, x, y, paint);
			x += wordLength;
		}
	}

	/**
	 * It prints the app logo on the bottom right corner of the coverImage
	 * @param coverDetails
	 * @param coverImage
	 */
	private void paintLogoOnTheCover(FrontCoverDetails coverDetails, Canvas coverImage) {
		Bitmap coverImageFile = getAppLogo();
		int margin = 5;
		int leftStart = coverDetails.getWidth() - (coverImageFile.getWidth() + margin);
		int topStart = coverDetails.getHeight() - (coverImageFile.getHeight() + margin);
		coverImage.drawBitmap(coverImageFile,
				leftStart, topStart,
				new Paint(Paint.FILTER_BITMAP_FLAG));
	}

	private byte[] saveBitmapAsPng(Bitmap bitmap) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
		return outputStream.toByteArray();
	}

	private Canvas applyGrayRectangle(Canvas canvas, float width, float height) {
		Paint paint = new Paint();
		paint.setColor(Color.LTGRAY);
		canvas.drawRect(0, 0, width, height, paint);
		return canvas;
	}

	private Bitmap getAppLogo() {
		return BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
	}

	private BitmapFactory.Options getBitmapOptions(String coverImageFilename, int maxWidth, int maxHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(coverImageFilename, options);

		// Get image
		options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
		options.inJustDecodeBounds = false;
		return options;
	}

	/**
	 * calculateInSampleSize from Android Developers site
	 * @see <a href="https://developer.android.com/training/displaying-bitmaps/load-bitmap.html"></a>
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
}
