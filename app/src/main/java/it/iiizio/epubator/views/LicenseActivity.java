/*
Copyright (C)2011 Ezio Querini <iiizio AT users.sf.net>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.iiizio.epubator.views;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.InputStream;

import it.iiizio.epubator.R;
import it.iiizio.epubator.presenters.LicensePresenter;
import it.iiizio.epubator.presenters.LicensePresenterImpl;

public class LicenseActivity extends Activity {

	private LicensePresenter presenter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.infoview);
		presenter = new LicensePresenterImpl();

		// Get license from raw
		TextView infoTv = (TextView)findViewById(R.id.infoview);
		infoTv.setTextSize(18);
		InputStream is = this.getResources().openRawResource(R.raw.license);
		String licenseInfo = presenter.getLicenseInfo(is);
		infoTv.setText(licenseInfo);
	}
}
