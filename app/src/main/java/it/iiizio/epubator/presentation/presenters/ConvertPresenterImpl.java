package it.iiizio.epubator.presentation.presenters;

import it.iiizio.epubator.domain.constants.DecisionOnConversionError;
import it.iiizio.epubator.domain.constants.PreferencesKeys;
import it.iiizio.epubator.domain.entities.ConversionPreferences;
import it.iiizio.epubator.domain.entities.ConversionSettings;
import it.iiizio.epubator.domain.services.ConversionService;
import it.iiizio.epubator.infrastructure.providers.PreferenceProvider;
import it.iiizio.epubator.infrastructure.providers.StorageProvider;

public class ConvertPresenterImpl implements ConvertPresenter {

	private final PreferenceProvider preferenceProvider;
	private final ConversionService conversionService;
	private final StorageProvider storageProvider;

	public ConvertPresenterImpl(PreferenceProvider preferenceProvider, ConversionService conversionService, StorageProvider storageProvider) {
		this.preferenceProvider = preferenceProvider;
		this.conversionService = conversionService;
		this.storageProvider = storageProvider;
	}

	@Override
    public boolean deleteTemporalFile(ConversionSettings settings) {
		storageProvider.remove(settings.tempFilename);
        if (storageProvider.exists(settings.oldFilename)) {
        	return storageProvider.rename(settings.oldFilename, settings.epubFilename);
        }
        return false;
    }

    @Override
    public void saveEpub(ConversionSettings settings) {
		storageProvider.rename(settings.tempFilename, settings.epubFilename);
		storageProvider.remove(settings.oldFilename);
    }

	@Override
	public ConversionSettings getConversionSettings(String pdfFilename, String coverImage) {
		ConversionPreferences preferences = getConversionPreferences();
		String temporalPath = storageProvider.getExternalCacheDirectory();
		String outputDirectory = conversionService.getOutputDirectory(pdfFilename, preferences.saveOnDownloadDirectory);
		return new ConversionSettings(preferences, outputDirectory, temporalPath, pdfFilename, coverImage);
	}

	private ConversionPreferences getConversionPreferences() {
		ConversionPreferences preferences = new ConversionPreferences();

		preferences.includeImages = preferenceProvider.getBoolean(PreferencesKeys.ADD_EXTRACTED_IMAGES_FROM_PDF, true);
		preferences.repeatedImages = preferenceProvider.getBoolean(PreferencesKeys.ADD_REPEATED_IMAGES);
		preferences.pagesPerFile = preferenceProvider.getParsedString(PreferencesKeys.PAGES_PER_FILE, 5);
		preferences.onError = preferenceProvider.getParsedString(PreferencesKeys.OPTION_WHEN_ERROR_IN_CONVERSION, DecisionOnConversionError.KEEP_ITEM);
		preferences.addMarkers = preferenceProvider.getBoolean(PreferencesKeys.MARK_ERRORS, true);
		preferences.tocFromPdf = preferenceProvider.getBoolean(PreferencesKeys.TRY_TO_EXTRACT_TOC_FROM_PDF, true);
		preferences.showLogoOnCover = preferenceProvider.getBoolean(PreferencesKeys.HAVE_LOGO_ON_COVER, true);
		preferences.saveOnDownloadDirectory = preferenceProvider.getBoolean(PreferencesKeys.SAVE_ALWAYS_ON_DOWNLOAD_DIRECTORY);

		return preferences;
	}
}
