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

package it.iiizio.epubator.domain.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XMLParser {

	private final DocumentBuilderFactory documentBuilderFactory;

	public XMLParser() {
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
	}

	public Document getDomElement(String xml){
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			InputSource inputSource = new InputSource();
			inputSource.setCharacterStream(new StringReader(xml));
			return documentBuilder.parse(inputSource);
		} catch (ParserConfigurationException e) {
			System.err.println("Error: " + e.getMessage());
			return null;
		} catch (SAXException e) {
			System.err.println("Error: " + e.getMessage());
			return null;
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
			return null;
		}
	}

	public String getValue(Element item, String str) {
		return item.getAttribute(str);
	}

	public final String getElementValue(Node elem) {
		if(elem == null){
			return "";
		}

		NodeList childNodes = elem.getChildNodes();
		for(int i=0; i<childNodes.getLength(); i++){
			Node child = childNodes.item(i);
			if(child.getNodeType() == Node.TEXT_NODE){
				return child.getNodeValue();
			}
		}

		return "";
	}	
}
