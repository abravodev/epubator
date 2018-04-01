package it.iiizio.epubator.presenters;

import java.io.IOException;
import java.io.InputStream;

public class LicensePresenterImpl implements LicensePresenter {
    @Override
    public String getLicenseInfo(InputStream is) {
        try {
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            // Remove unwanted newlines
            return new String(buffer, "utf-8").replaceAll("(?<!\n)\n(?!\n)", " ");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}
