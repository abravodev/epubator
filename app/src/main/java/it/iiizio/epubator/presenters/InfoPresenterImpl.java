package it.iiizio.epubator.presenters;

import java.io.IOException;
import java.io.InputStream;

public class InfoPresenterImpl implements InfoPresenter {

    @Override
    public String getInfo(InputStream is) {
        StringBuilder sb = new StringBuilder();
        int i;
        try {
            i = is.read();
            while (i != -1) {
                sb.append((char) i);
                i = is.read();
            }
        } catch (IOException e) {
            // Auto-generated catch block
            e.printStackTrace();
        }

        return sb.toString();
    }
}
