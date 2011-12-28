Minimal offline PDF to ePUB converter for Android

ePUBator extract text from a PDF file and put it in a well formed (epubcheck compliant) ePUB file.
PDF extraction based on iText library <http://itextpdf.com/>. 

- ePUBator doesn't need internet connection.
- ePUBator doesn't have ads.
- ePUBator extract only text (no pictures, no text in pictures).
- ePUBator works fine with single column PDF (can work bad with multi column or tables).
- ePUBator can fail extraction (12 of 60 books tested: I'm still working on it but it looks like a iText problem). 
- ePUBator can crash (out of memory crash resolved, I hope). 

For comment/feedback/bug/suggestion write to: iiizio AT users.sf.net

History:
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
- ePUBator non ha pubblicità.
- ePUBator estrae solo il testo (nessuna figura, nessun testo dalle figure).
- ePUBator funziona bene con i PDF a singola colonna (può essere pessimo con quelli multicolonna o le tabelle).
- ePUBator può fallire l'estrazione (12 libri su 60 provati: ci stò lavorando su ma sembra un problema di iText).
- ePUBator può piantarsi (risolto il blocco per memoria insufficente, spero).

Per commenti/feedback/bug/suggerimenti scrivi a: iiizio AT users.sf.net

Storico:
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
