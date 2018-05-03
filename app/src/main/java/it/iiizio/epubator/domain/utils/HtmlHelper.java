package it.iiizio.epubator.domain.utils;

public class HtmlHelper {

	/**
	 * stringToHTMLString found on the web, no license indicated
	 * @see <a href="http://www.rgagnon.com/javadetails/java-0306.html"></a>
	 * @author S. Bayer.
	 * @param string
	 * @return
	 */
	public static String stringToHTMLString(String string) {
        StringBuilder sb = new StringBuilder(); // changed StringBuffer to StringBuilder to prevent buffer overflow (iiizio)
        // true if last char was blank
        boolean lastWasBlankChar = false;
        int len = string.length();
        char c;

        for (int i = 0; i < len; i++)
        {
            c = string.charAt(i);
            if (c == ' ') {
                // blank gets extra work,
                // this solves the problem you get if you replace all
                // blanks with &nbsp;, if you do that you loss
                // word breaking
                if (lastWasBlankChar) {
                    lastWasBlankChar = false;
                    sb.append("&nbsp;");
                }
                else {
                    lastWasBlankChar = true;
                    sb.append(' ');
                }
            }
            else {
                lastWasBlankChar = false;
                //
                // HTML Special Chars
                if (c == '"')
                    sb.append("&quot;");
                else if (c == '&')
                    sb.append("&amp;");
                else if (c == '<')
                    sb.append("&lt;");
                else if (c == '>')
                    sb.append("&gt;");
                else if (c == '%') // Android browser doesn't like % (iiizio)
                    sb.append("&#37;");
                else if (c == '\n')
                    // Handle Newline
                    sb.append("\n<br/>");
                else {
                    int ci = 0xffff & c;
                    if (ci < 160 ) {
                        // nothing special only 7 Bit
                        sb.append(c);
                    } else {
                        // Not 7 Bit use the unicode system
                        sb.append("&#");
                        sb.append(Integer.valueOf(ci).toString());
                        sb.append(';');
                    }
                }
            }
        }
        return sb.toString();
    }

    public static String getBasicHtml(String title, String body) {
        StringBuilder html = new StringBuilder();
        html.append("  <!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \n");
        html.append("  \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n");
        html.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        html.append("<head>\n");
        html.append("  <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>\n");
        html.append("  <meta name=\"generator\" content=\"ePUBator - Minimal offline PDF to ePUB converter for Android\"/>\n");
        html.append("  <title>" + title + "</title>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append(body);
        html.append("\n</body>\n");
        html.append("</html>\n");
        return html.toString();
    }
}
