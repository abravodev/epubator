package it.iiizio.epubator.presentation.presenters;

import it.iiizio.epubator.domain.constants.ImageTypes;
import it.iiizio.epubator.domain.constants.PreferencesKeys;
import it.iiizio.epubator.domain.utils.FileHelper;
import it.iiizio.epubator.infrastructure.providers.PreferenceProvider;
import it.iiizio.epubator.infrastructure.providers.StorageProvider;

public class MainPresenterImpl implements MainPresenter {

	//<editor-fold desc="Attributes">
	private final PreferenceProvider viewPreferencesProvider;
    private final PreferenceProvider sharedPreferencesProvider;
    private final StorageProvider storageProvider;
	//</editor-fold>

	//<editor-fold desc="Constructors">
	public MainPresenterImpl(PreferenceProvider viewPreferencesProvider, PreferenceProvider sharedPreferencesProvider,
			 StorageProvider storageProvider) {
		this.viewPreferencesProvider = viewPreferencesProvider;
		this.sharedPreferencesProvider = sharedPreferencesProvider;
		this.storageProvider = storageProvider;
	}
	//</editor-fold>

	@Override
    public String getCoverFileWithTheSameName(String filename) {
		String coverFile = "";
		if(filename == null){
			return coverFile;
		}
		String name = FileHelper.getPathWithoutExtension(filename);

        if(existImageWithSameName(name, ImageTypes.PNG)) {
            coverFile = imageWithSameName(name,ImageTypes.PNG);
        } else if(existImageWithSameName(name, ImageTypes.JPG)) {
            coverFile = imageWithSameName(name, ImageTypes.JPG);
        } else if(existImageWithSameName(name , ImageTypes.JPEG)) {
            coverFile = imageWithSameName(name, ImageTypes.JPEG);
        }

        return coverFile;
    }

    @Override
    public boolean showInitialDialog() {
        return viewPreferencesProvider.getBoolean(PreferencesKeys.FIRST_TIME_APP, true);
    }

	@Override
	public void initialDialogRead() {
		viewPreferencesProvider.save(PreferencesKeys.FIRST_TIME_APP, false);
	}

	@Override
	public void updateRecentFolder(String filename) {
		if(filename == null){
			return;
		}
		String path = filename.substring(0, filename.lastIndexOf('/', filename.length()) + 1);
		sharedPreferencesProvider.save(PreferencesKeys.PATH, path);
	}

	@Override
	public String getRecentFolder() {
		String recentFolder = sharedPreferencesProvider.getString(PreferencesKeys.PATH);
		if(recentFolder == null){
			recentFolder = storageProvider.getExternalStorageDirectory();
		}
		return recentFolder;
	}

	@Override
	public boolean userPrefersToChoosePicture() {
		return sharedPreferencesProvider.getBoolean(PreferencesKeys.CHOOSE_PICTURE);
	}

	private boolean existImageWithSameName(String filename, String imageType){
		String imageName = imageWithSameName(filename, imageType);
		return storageProvider.exists(imageName);
	}

	private String imageWithSameName(String filename, String imageType){
		return String.format("%s.%s", filename, imageType);
	}
}
