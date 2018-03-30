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

package it.iiizio.epubator;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

public class WriteZip {
	static ZipOutputStream zipOut = null;
	static final int BUFFER = 1024;
	static byte data[] = new byte[BUFFER];

	// Create file
	static boolean create(String filename) {
		try {
			zipOut = new ZipOutputStream(new FileOutputStream(new File(filename)));
		}
		catch (Exception e) {
			System.err.println("Failed to open zip file" + e.getMessage());
			return true;
		}
		return false;
	}

	// Add text entry
	static boolean addText(String filename, String text, boolean store) {
		CRC32 crc32 = new CRC32();
		byte[] data = text.getBytes();

		try {
			ZipEntry zipEntry = new ZipEntry(filename);
			zipOut.setMethod(ZipOutputStream.DEFLATED);
			if (store) {
				zipOut.setMethod(ZipOutputStream.STORED);
				zipEntry.setSize((long) data.length);
				crc32.reset();
				crc32.update(data);
				zipEntry.setCrc(crc32.getValue());
			}

			zipOut.putNextEntry(zipEntry);
			zipOut.write(data);
			zipOut.closeEntry();
		}
		catch (Exception e) {
			System.err.println("Failed to add textfile to zip" + e.getMessage());
			return true;
		}
		return false;
	}

	// Add image entry
	static boolean addImage(String filename, byte[] image) {
		try {
			ZipEntry zipEntry = new ZipEntry(filename);
			zipOut.setMethod(ZipOutputStream.DEFLATED);
			zipOut.putNextEntry(zipEntry);
			zipOut.write(image);
			zipOut.closeEntry();
		}
		catch (ZipException e) {
			return !e.getMessage().startsWith("Entry already exists");
		}
		catch (Exception e) {
			return true;
		}
		return false;
	}

	// Close file
	static boolean close() {
		try {
			zipOut.flush();
			zipOut.close();
		}
		catch (Exception e) {
			System.err.println("Failed to close zip file" + e.getMessage());
			return true;
		}
		return false;
	}
}
