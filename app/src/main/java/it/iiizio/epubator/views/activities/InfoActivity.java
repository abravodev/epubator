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

package it.iiizio.epubator.views.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.InputStream;

import it.iiizio.epubator.R;
import it.iiizio.epubator.presenters.InfoPresenter;
import it.iiizio.epubator.presenters.InfoPresenterImpl;

public class InfoActivity extends Activity {

	private InfoPresenter presenter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		presenter = new InfoPresenterImpl();

		setupTextInfo();
	}

	private void setupTextInfo(){
		TextView infoTv = (TextView)findViewById(R.id.infoview);
		infoTv.setTextSize(18);
		InputStream is = this.getResources().openRawResource(R.raw.info);
		String info = presenter.getInfo(is);
		infoTv.setText(info);
	}
}
