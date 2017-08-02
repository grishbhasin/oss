/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.parser.htmlParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.DomUtils;

public class StrictXhtmlParser extends HtmlDocumentProvider {

	public StrictXhtmlParser() {
		super(HtmlParserEnum.StrictXhtmlParser);
	}

	@Override
	protected DomHtmlNode getDocument(String charset, InputStream inputStream)
			throws SAXException, IOException, ParserConfigurationException {
		InputSource inputSource = new InputSource(inputStream);
		inputSource.setEncoding(charset);
		return new DomHtmlNode(DomUtils.readXml(inputSource, true));
	}

	@Override
	protected HtmlNodeAbstract<?> getDocument(String htmlSource)
			throws IOException, ParserConfigurationException, SAXException {
		return new DomHtmlNode(DomUtils.readXml(new InputSource(
				new StringReader(htmlSource)), true));
	}

	@Override
	public boolean isXPathSupported() {
		return true;
	}

}
