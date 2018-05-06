# Minimal offline PDF to ePUB converter for Android

ePUBator extract text from a PDF file and put it in a well formed (epubcheck compliant) ePUB file.

## What ePUBator does
- ePUBator is thinked for books (not for every type of pdf), but if you need a better result try something else like calibre.
- ePUBator doesn't need internet connection (doesn't send your docs somewhere on the net, doesn't have ads).
- ePUBator extracts text (no text from pictures or from raster PDF).
- ePUBator tries to extract images (only png, jpg and gif) but puts them at the page's end.
- ePUBator tries to extract the table of contents if present (or creates a dummy TOC).
- ePUBator doesn't extract the font size and style.
- ePUBator saves the ePUB file in the same folder of PDF file or in Download folder if PDF folder is not writable or if it is set in Settings.
- ePUBator works fine only with single column PDF (worse with multi column or tables).

## Known issues
In the event of crash, unexpected stoppages or insufficient memory try setting Page per file to 1 and/or disable the extraction of images and try again (it helps sometimes).

The creator of the app converted a lot of books (italians and some english) with ePUBator, but with some of them, he said the conversion failed (raster PDF, columns, out of order chars).

App has never been tested with files containing arabic or asian characters, right to left or vertical writing.

On some books, you can find wrong chars (e.g. the vowels with backticks in languages like the Italian), it seems like an iText's problem (another PDF library extract the same text correctly).

## Permissions required
- *READ_EXTERNAL_STORAGE* and *WRITE_EXTERNAL_STORAGE* for save the ePUB file
- *VIBRATE* for vibrate after conversion finishes

## Built with
PDF extraction based on [iText library](http://itextpdf.com/) released under the AGPL license.

## License
This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

## Download
You can download the old app from [Sourceforge](https://sourceforge.net/p/epubator/home/Home/).

If you want to use the new one, you have to build it through Android Studio.
Since I'm not the creator of the app, I cannot update the version of the one at the [Play Store](https://play.google.com/store/apps/details?id=it.iiizio.epubator).

## Creator
- [iiizio](https://sourceforge.net/u/iiizio/): Original creation of the app, integration with iText, created Sourceforge project, published it on Play Store...

## Contributors
- [abravodev](https://github.com/abravodev/): Redesigned the app, made major refactor of its structure, created Github project...