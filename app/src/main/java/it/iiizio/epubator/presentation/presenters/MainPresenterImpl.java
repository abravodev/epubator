package it.iiizio.epubator.presentation.presenters;

import java.io.File;

import it.iiizio.epubator.domain.constants.PreferencesKeys;
import it.iiizio.epubator.domain.utils.FileHelper;
import it.iiizio.epubator.infrastructure.providers.PreferenceProvider;
import it.iiizio.epubator.infrastructure.providers.StorageProvider;

public class MainPresenterImpl implements MainPresenter {

    private final PreferenceProvider viewPreferencesProvider;
    private final PreferenceProvider sharedPreferencesProvider;
    private final StorageProvider storageProvider;

	public MainPresenterImpl(PreferenceProvider viewPreferencesProvider, PreferenceProvider sharedPreferencesProvider,
			 StorageProvider storageProvider) {
		this.viewPreferencesProvider = viewPreferencesProvider;
		this.sharedPreferencesProvider = sharedPreferencesProvider;
		this.storageProvider = storageProvider;
	}

	@Override
    public String getCoverFileWithTheSameName(String filename) {
        String name = FileHelper.getPathWithoutExtension(filename);
        String coverFile = "";

        if(new File(name + ".png").exists()) {
            coverFile = name + ".png";
        } else if(new File(name + ".jpg").exists()) {
            coverFile = name + ".jpg";
        } else if(new File(name + ".jpeg").exists()) {
            coverFile = name + ".jpeg";
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
	public boolean userPrefersToUsePicture() {
		return sharedPreferencesProvider.getBoolean(PreferencesKeys.CHOOSE_PICTURE);
	}

}
