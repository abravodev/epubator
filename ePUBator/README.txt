Minimal offline PDF to ePUB converter for Android

ePUBator extract text from a PDF file and put it in a well formed (epubcheck compliant) ePUB file.

PDF extraction based on iText library <http://itextpdf.com/> released under the AGPL license. 

- ePUBator doesn't need internet connection.
- ePUBator doesn't send your docs somewhere on the net.
- ePUBator doesn't have ads.
- ePUBator extract text (no text in pictures or in raster PDF).
- ePUBator try to extract images (only png, jpg and gif) but puts them at the page's end.
- ePUBator try to extract the table of contents if present (or creates a dummy TOC).
- ePUBator doesn't extract the font size and style.
- ePUBator put the ePUB file in the same folder of PDF file.
- ePUBator uses the PDF filename to generate ePUB filename and the title in the frontpage.
- ePUBator works fine with single column PDF (can work bad with multi column or tables).
- ePUBator can fail extraction (5 of 358 books with v0.8.1 on my Atrix).
- ePUBator IS MADE FOR BOOKS, NOT FOR EVERY TYPE OF PDF (DON'T ASK IT TOO MUCH).

WARNING!!!
In the event of crash, unexpected stoppages or insufficient memory try setting Page per file to 1 and/or disable the extraction of images and try again (sometimes helps).

I converted a lot of books (italians and some english) with ePUBator, but with someone the conversion can fail (raster PDF, columns, out of order chars).

I never try with arabic or asian characters, right to left or vertical writing and I don't know if (and how) the iText library can manage those pdf. I'm sorry.

Permissions required:
- WRITE_EXTERNAL_STORAGE for save the ePUB file

Legal stuff:
This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

For comment / feedback / bug / suggestion / localization / donations write to: iiizio AT users.sf.net

You can find sources and all apk files on sourceforge: https://sourceforge.net/p/epubator/home/Home/

History:
v0.9  (2012-09-26)
- Fixed some bugs
- Updated iText lib to 5.3.3

v0.8.1  (2012-07-24)
- Fixed null string bug

v0.8  (2012-07-23)
- Added TOC extraction
- Fixed creator bug
- Code optimization
- Updated iText lib to 5.3.0

v0.7  (2012-05-21)
- Fixed title bug (bad ePUB)
- Updated iText lib to 5.2.1
- Some Lint optimizations

v0.6  (2012-03-04)
- Added images in preview
- Added progress bar
- Added some prefs
- Improved file chooser
- Some bug fix

v0.5  (2012-01-09)
- Added images extraction
- Added preferences
- Improved memory management
- Some little fix

v0.4  (2011-12-24)
- Added frontpage
- Added landscape layout
- Added shows extraction failure in progress dialog
- Fixed filechooser bug (sdcard not readable)
- Fixed preview bug
- Updated iText lib to 5.1.3

v0.3  (2011-11-24)
- Added preview
- Added notification
- Added possibility to stop conversion
- Enabled 'Move to SD'
- Differentiate error and page lost marker
- Code optimization

v0.2  (2011-11-08)
- Added 'keep errored file' dialog
- Added marker on error
- Fixed a buffer overflow and some minor bugs

v0.1  (2011-11-01)
- Initial release


----------------------------------------------------------------------------------


Convertitore da PDF a ePUB minimale per Android

ePUBator estrae il testo da un file PDF e lo inserisce in un file ePUB correttamente formattato (conforme con epubcheck).

L'estrazione dal PDF è basata sulla libreria iText <http://itextpdf.com/> rilasciata sotto licenza AGPL. 

- ePUBator non necessita di una connessione internet.
- ePUBator non invia i tuoi documenti da qualche parte sulla rete.
- ePUBator non ha pubblicità.
- ePUBator estrae il testo (nessun testo dalle figure o da PDF raster).
- ePUBator cerca di estrarre le immagini (solo png, jpg e gif) ma le mette alla fine della pagina.
- ePUBator cerca di estrarre l'indice dal PDF se presente (o ne crea uno fittizio).
- ePUBator non estrae lo stile e dimensioni dei caratteri.
- ePUBator mette il file ePUB nella stessa cartella del file PDF.
- ePUBator usa il nome del file PDF per generare il nome del file ePUB e il titolo di copertina.
- ePUBator funziona bene con i PDF a singola colonna (può essere pessimo con quelli multicolonna o le tabelle).
- ePUBator può fallire l'estrazione (5 of 358 libri con la v0.8.1 sul mio Atrix).
- ePUBator E' FATTO PER I LIBRI, NON PER OGNI TIPO DI PDF (NON CHIEDETEGLI TROPPO).

ATTENZIONE!!!
Nel caso di crash, interruzioni inaspettate o memoria insufficente prova ad impostare Pagine per file a 1 e/o disabilitare l'estrazione delle immagini e riprova (a volte aiuta).

Ho convertito parecchi libri italiani (e alcuni in inglese) con ePUBator, ma con qualcuno la conversione fallisce (PDF scannerizzati, colonne, caratteri in ordine errato).

Non ho mai provato pdf con caratteri arabi o asiatici, scrittura da destra a sinistra o verticale e non so se (e come) la libreria iText li gestisce. Mi dispiace.

Autorizzazioni richieste:
- WRITE_EXTERNAL_STORAGE per salvare il file ePUB

Note legali:
Questo programma è software libero: è possibile ridistribuirlo e/o modificarlo secondo i termini della GNU General Public License come pubblicata dalla Free Software Foundation, sia la versione 3 della licenza, o (a propria scelta) una versione successiva. 

Questo programma è distribuito nella speranza che possa essere utile, ma SENZA ALCUNA GARANZIA, nemmeno la garanzia implicita di COMMERCIABILITÀ o IDONEITÀ PER UN PARTICOLARE SCOPO. Vedere la GNU General Public License per ulteriori dettagli. 

Per commenti / feedback / bug / suggerimenti / localizzazioni / donazioni scrivi a: iiizio AT users.sf.net

Puoi trovare i sorgenti e tutti i file apk su sourceforge: https://sourceforge.net/p/epubator/home/Home/

Storico:
v0.9  (26-09-2012)
- Corretti alcuni bug
- Aggiornata iText lib alla 5.3.3

v0.8.1  (24-07-2012)
- Risolto bug stringa inesistente

v0.8  (23-07-2012)
- Aggiunta l'estrazione del indice
- Risolto bug del creatore
- Ottimizazione del codice
- Aggiornata iText lib alla 5.3.0

v0.7  (21-05-2012)
- Risolto bug del titolo
- Aggiornata iText lib alla 5.2.1
- Alcune ottimizazioni suggerite da Lint

v0.6  (04-03-2012)
- Aggiunte immagini nell'anteprima
- Aggiunta la barra di avanzamento
- Aggiunte alcune preferenze
- Migliorato il selettore dei file
- Risolto qualche bug

v0.5  (09-01-2012)
- Aggiunta l'estrazione delle immagini
- Aggiunte impostazioni
- Migliorata la gestione della memoria
- Alcune piccole correzioni

v0.4  (24-12-2011)
- Aggiunta frontpage
- Aggiunto layout orizzontale
- Aggiunta visualizzazione degli errori di estrazione nel progress dialog
- Corretto bug del selettore dei file (sdcard non leggibile)
- Corretto bug dell'anteprima
- Aggiornata iText lib alla 5.1.3

v0.3  (24-11-2011)
- Aggiunta anteprima
- Aggiunta notifica
- Aggiunta la possibilità di fermare la conversione
- Abilitato 'Muovi su SD'
- Differenziato l'indicatore di errori e pagine perse
- Ottimizazione del codice

v0.2  (08-11-2011)
- Aggiunto dialogo 'conserva file errorato'
- Aggiunto marcatore in caso di errore
- Corretto un buffer overflow e alcuni bug meno importanti

v0.1  (01-11-2011)
- Rilascio iniziale
