/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.parser;

import java.io.IOException;

import org.apache.poi.hpbf.extractor.PublisherTextExtractor;
import org.apache.poi.hpsf.SummaryInformation;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.StringUtils;

public class PublisherParser extends Parser {

	public static final String[] DEFAULT_MIMETYPES = { "application/x-mspublisher" };

	public static final String[] DEFAULT_EXTENSIONS = { "pub" };

	private static ParserFieldEnum[] fl = { ParserFieldEnum.parser_name,
			ParserFieldEnum.title, ParserFieldEnum.author,
			ParserFieldEnum.subject, ParserFieldEnum.content,
			ParserFieldEnum.lang };

	public PublisherParser() {
		super(fl);
	}

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.SIZE_LIMIT, "0", null, 20, 1);
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang)
			throws IOException {
		PublisherTextExtractor extractor = null;
		try {
			extractor = new PublisherTextExtractor(
					streamLimiter.getNewInputStream());
			SummaryInformation info = extractor.getSummaryInformation();
			ParserResultItem result = getNewParserResultItem();
			if (info != null) {
				result.addField(ParserFieldEnum.title, info.getTitle());
				result.addField(ParserFieldEnum.author, info.getAuthor());
				result.addField(ParserFieldEnum.subject, info.getSubject());
			}
			result.addField(ParserFieldEnum.content, StringUtils
					.replaceConsecutiveSpaces(extractor.getText(), " "));
			result.langDetection(10000, ParserFieldEnum.content);
		} finally {
			IOUtils.close(extractor);
		}
	}

}
