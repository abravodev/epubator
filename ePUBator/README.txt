Minimal offline PDF to ePUB converter for Android

ePUBator extract text from a PDF file and put it in a well formed (epubcheck compliant) ePUB file.

PDF extraction based on iText library <http://itextpdf.com/>. 

- ePUBator doesn't need internet connection.
- ePUBator doesn't send your docs somewhere on the net.
- ePUBator doesn't have ads.
- ePUBator extract text (no text in pictures).
- ePUBator try to extract images (only png, jpg and gif) but puts them at the page's end.
- ePUBator doesn't extract the table of contents but creates a dummy index.
- ePUBator works fine with single column PDF (can work bad with multi column or tables).
- ePUBator can fail extraction (17 of 100 books tested: it looks like a iText problem).

WARNING!!!
In the event of crash, unexpected stoppages or insufficient memory try setting Page per file to 1 and/or disable the extraction of images and try again (sometimes helps).

For comment/feedback/bug/suggestion/localization write to: iiizio AT users.sf.net

History:
v0.6
- Added images in preview
- Added progress bar
- Added some prefs
- Improved file chooser
- Some bug fix
v0.5
- Added images extraction
- Added preferences
- Improved memory management
- Some little fix
v0.4
- Added frontpage
- Added landscape layout
- Added shows extraction failure in progress dialog
- Fixed filechooser bug (sdcard not readable)
- Fixed preview bug
- Updated iText lib to 5.1.3
v0.3
- Added preview
- Added notification
- Added possibility to stop conversion
- Enabled 'Move to SD'
- Differentiate error and page lost marker
- Code optimization
v0.2
- Added 'keep errored file' dialog
- Added marker on error
- Fixed a buffer overflow and some minor bugs
v0.1
- Initial release

#########################################################################################################


Convertitore da PDF a ePUB minimale per Android

ePUBator estrae il testo da un file PDF e lo inserisce in un file ePUB correttamente formattato (conforme con epubcheck).

L'estrazione dal PDF è basata sulla libreria iText <http://itextpdf.com/>. 

- ePUBator non necessita di una connessione internet.
- ePUBator non invia i tuoi documenti da qualche parte sulla rete.
- ePUBator non ha pubblicità.
- ePUBator estrae il testo (nessun testo dalle figure).
- ePUBator cerca di estrarre le immagini (solo png, jpg e gif) ma le mette alla fine della pagina.
- ePUBator non estrae l'indice del PDF ma ne crea uno fittizio.
- ePUBator funziona bene con i PDF a singola colonna (può essere pessimo con quelli multicolonna o le tabelle).
- ePUBator può fallire l'estrazione (17 libri su 100 provati: sembra sia un problema di iText).

ATTENZIONE!!!
Nel caso di crash, interruzioni inaspettate o memoria insufficente prova ad impostare Pagine per file a 1 e/o disabilitare l'estrazione delle immagini e riprova (a volte aiuta).

Per commenti/feedback/bug/suggerimenti/localizzazioni scrivi a: iiizio AT users.sf.net

Storico:
v0.6
- Aggiunte immagini nell'anteprima
- Aggiunta la barra di avanzamento
- Aggiunte alcune preferenze
- Migliorato il selettore dei file
- Risolto qualche bug
v0.5
- Aggiunta l'estrazione delle immagini
- Aggiunte impostazioni
- Migliorata la gestione della memoria
- Alcune piccole correzioni
v0.4
- Aggiunta frontpage
- Aggiunto layout orizzontale
- Aggiunta visualizzazione degli errori di estrazione nel progress dialog
- Corretto bug del selettore dei file (sdcard non leggibile)
- Corretto bug dell'anteprima
- Aggiornata iText lib alla 5.1.3
v0.3
- Aggiunta anteprima
- Aggiunta notifica
- Aggiunta la possibilità di fermare la conversione
- Abilitato 'Muovi su SD'
- Differenziato l'indicatore di errori e pagine perse
- Ottimizazione del codice
v0.2
- Aggiunto dialogo 'conserva file errorato'
- Aggiunto marcatore in caso di errore
- Corretto un buffer overflow e alcuni bug meno importanti
v0.1
- Rilascio iniziale
