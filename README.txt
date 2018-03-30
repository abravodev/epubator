Minimal offline PDF to ePUB converter for Android - ©2011 Ezio Querini

ePUBator extract text from a PDF file and put it in a well formed (epubcheck compliant) ePUB file.

PDF extraction based on iText library <http://itextpdf.com/> released under the AGPL license. 

- ePUBator IS THINKED FOR BOOKS (NOT FOR EVERY TYPE OF PDF), BUT IF YOU NEED A BETTER RESULT TRY SOMETHING ELSE LIKE CALIBRE.
- ePUBator doesn't need internet connection (doesn't send your docs somewhere on the net, doesn't have ads).
- ePUBator extracts text (no text from pictures or from raster PDF).
- ePUBator tries to extract images (only png, jpg and gif) but puts them at the page's end.
- ePUBator tries to extract the table of contents if present (or creates a dummy TOC).
- ePUBator doesn't extract the font size and style.
- ePUBator saves the ePUB file in the same folder of PDF file or in Download folder if PDF folder is not writable or if it is set in Settings.
- ePUBator works fine only with single column PDF (worse with multi column or tables).
- ePUBator can fail extraction (5 of 358 books with v0.8.1 on my Atrix).

WARNING!!!
In the event of crash, unexpected stoppages or insufficient memory try setting Page per file to 1 and/or disable the extraction of images and try again (sometimes helps).

I converted a lot of books (italians and some english) with ePUBator, but with someone the conversion can fail (raster PDF, columns, out of order chars).

I never try with arabic or asian characters, right to left or vertical writing and I don't know if (and how) the iText library can manage those pdf. I'm sorry.

On some books we can find wrong chars (e.g. Øø instead of éù), it seems a iText's problem (another PDF library extract the same text correctly). I'm looking for a better one, free and Android compatible.

Permissions required:
- READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE for save the ePUB file

Legal stuff:
This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

For comment / feedback / bug / suggestion / localization write to: iiizio AT users.sf.net

For sources / all apk files / donations go to sourceforge: https://sourceforge.net/p/epubator/home/Home/

History:
v0.12  (2015-07-19)
- Added cover image
- Added save in Download
- Added quick help
- Added share ePUB
- Updated iText lib and support library
- Fixed some bugs

v0.11  (2013-04-07)
- Added open with ePUBator
- Added recent folder selector
- Added dummy entry for first page
- Added support library
- Updated iText lib to 5.4.0
- Some minor fix and improvements

v0.10  (2012-12-22)
- Improved TOC extraction 
- Fixed close on rotate
- Updated iText lib to 5.3.5

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


Convertitore da PDF a ePUB minimale per Android - ©2011 Ezio Querini

ePUBator estrae il testo da un file PDF e lo inserisce in un file ePUB correttamente formattato (conforme con epubcheck).

L'estrazione dal PDF è basata sulla libreria iText <http://itextpdf.com/> rilasciata sotto licenza AGPL. 

- ePUBator E' PENSATO PER I LIBRI (NON PER OGNI TIPO DI PDF), MA SE VOLETE UN RISULTATO MIGLIORE PROVATE QUALCOSA D'ALTRO COME CALIBRE.
- ePUBator non necessita di una connessione internet (non invia i tuoi documenti da qualche parte sulla rete, non ha pubblicità).
- ePUBator estrae il testo (nessun testo dalle figure o da PDF raster).
- ePUBator cerca di estrarre le immagini (solo png, jpg e gif) ma le mette alla fine della pagina.
- ePUBator cerca di estrarre l'indice dal PDF se presente (o ne crea uno fittizio).
- ePUBator non estrae lo stile e le dimensioni dei caratteri.
- ePUBator salva il file ePUB nella stessa cartella del file PDF o nella cartella Download se la cartella del PDF non è scrivibile o se impostato in Preferenze.
- ePUBator funziona bene solo con i PDF a singola colonna (peggio con quelli multicolonna o le tabelle).
- ePUBator può fallire l'estrazione (5 of 358 libri con la v0.8.1 sul mio Atrix).

ATTENZIONE!!!
Nel caso di crash, interruzioni inaspettate o memoria insufficente prova ad impostare Pagine per file a 1 e/o disabilitare l'estrazione delle immagini e riprova (a volte aiuta).

Ho convertito parecchi libri italiani (e alcuni in inglese) con ePUBator, ma con qualcuno la conversione fallisce (PDF scannerizzati, colonne, caratteri in ordine errato).

Non ho mai provato pdf con caratteri arabi o asiatici, scrittura da destra a sinistra o verticale e non so se (e come) la libreria iText li gestisce. Mi dispiace.

In alcuni libri si possono trovare caratteri errati (ad esempio Øø invece di éù), sembra sia un problema di iText (un'altra libreria PDF estrae lo stesso testo correttamente). Ne sto cercando una migliore, gratuita e compatibile con Android.

Autorizzazioni richieste:
- READ_EXTERNAL_STORAGE e WRITE_EXTERNAL_STORAGE per salvare il file ePUB

Note legali:
Questo programma è software libero: è possibile ridistribuirlo e/o modificarlo secondo i termini della GNU General Public License come pubblicata dalla Free Software Foundation, sia la versione 3 della licenza, o (a propria scelta) una versione successiva. 

Questo programma è distribuito nella speranza che possa essere utile, ma SENZA ALCUNA GARANZIA, nemmeno la garanzia implicita di COMMERCIABILITÀ o IDONEITÀ PER UN PARTICOLARE SCOPO. Vedere la GNU General Public License per ulteriori dettagli. 

Per commenti / feedback / bug / suggerimenti / localizzazioni scrivi a: iiizio AT users.sf.net

Per sorgenti / tutti i file apk / donazioni vai su sourceforge: https://sourceforge.net/p/epubator/home/Home/

Storico:
v0.12  (19-07-2015)         
- Aggiunta immagine sulla copertina
- Aggiunto salva in Download
- Aggiunta guida rapida
- Aggiunto condividi ePUB
- Aggiornate iText lib e libreria di supporto
- Correzioni varie

v0.11  (07-04-2013)
- Aggiunto apri con ePUBator
- Aggiunto selettore delle cartelle recenti
- Aggiunto capitolo fittizio per la prima pagina
- Aggiunta libreria di supporto
- Aggiornata iText lib alla 5.4.0
- Alcune correzioni e miglioramenti minori

v0.10  (22-12-2012)
- Migliorata l'estrazione del indice
- Corretta chiusura alla rotazione
- Aggiornata iText lib alla 5.3.5

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
