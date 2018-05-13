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

package it.iiizio.epubator.domain.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

public class ZipWriterServiceImpl implements ZipWriterService  {

	private ZipOutputStream zipOutputStream;

	@Override
	public boolean create(String filename) {
		try {
			zipOutputStream = new ZipOutputStream(new FileOutputStream(new File(filename)));
		} catch (FileNotFoundException e) {
			System.err.println("Failed to open zip file" + e.getMessage());
			return true;
		}
		return false;
	}

	@Override
	public boolean addText(String filename, String text){
		return addText(filename, text, false);
	}

	@Override
	public boolean addText(String filename, String text, boolean store) {
		CRC32 crc32 = new CRC32();
		byte[] data = text.getBytes();

		try {
			ZipEntry zipEntry = new ZipEntry(filename);
			zipOutputStream.setMethod(ZipOutputStream.DEFLATED);

			if (store) {
				zipOutputStream.setMethod(ZipOutputStream.STORED);
				zipEntry.setSize((long) data.length);
				crc32.reset();
				crc32.update(data);
				zipEntry.setCrc(crc32.getValue());
			}

			zipOutputStream.putNextEntry(zipEntry);
			zipOutputStream.write(data);
			zipOutputStream.closeEntry();
		} catch (Exception e) {
			System.err.println("Failed to add textfile to zip" + e.getMessage());
			return true;
		}
		return false;
	}

	@Override
	public boolean addImage(String filename, byte[] image) {
		try {
			ZipEntry zipEntry = new ZipEntry(filename);
			zipOutputStream.setMethod(ZipOutputStream.DEFLATED);
			zipOutputStream.putNextEntry(zipEntry);
			zipOutputStream.write(image);
			zipOutputStream.closeEntry();
		} catch (ZipException e) {
			return !e.getMessage().startsWith("Entry already exists");
		} catch (Exception e) {
			return true;
		}
		return false;
	}

	@Override
	public boolean close() {
		try {
			zipOutputStream.flush();
			zipOutputStream.close();
		} catch (Exception e) {
			System.err.println("Failed to close zip file" + e.getMessage());
			return true;
		}
		return false;
	}
}
