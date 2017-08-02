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
package com.jaeksoft.searchlib.analysis.filter;

import java.io.IOException;
import java.net.URL;

import org.apache.lucene.analysis.TokenStream;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.video.YouTube;
import com.jaeksoft.searchlib.util.video.YouTubeItem;

public class YouTubeTokenFilter extends AbstractTermFilter {

	private int youtubeData;
	private boolean faultTolerant;
	private HttpDownloader httpDownloader = null;

	protected YouTubeTokenFilter(TokenStream input, int youtubeData,
			HttpDownloader httpDownloader, boolean faultTolerant) {
		super(input);
		this.youtubeData = youtubeData;
		this.httpDownloader = httpDownloader;
		this.faultTolerant = faultTolerant;
	}

	@Override
	public final boolean incrementToken() throws IOException {
		for (;;) {
			if (!input.incrementToken())
				return false;
			String term = termAtt.toString();
			try {
				URL url = LinkUtils.newEncodedURL(term);
				YouTubeItem youtubeItem = YouTube.getInfo(url, httpDownloader);
				switch (youtubeData) {
				case 0:
					term = youtubeItem.getTitle();
					break;
				case 1:
					term = youtubeItem.getDescription();
					break;
				case 2:
					term = youtubeItem.toJson(url);
					break;
				default:
					term = null;
					break;
				}
				if (term == null || term.length() == 0)
					return false;
				createToken(term);
				return true;
			} catch (Exception e) {
				if (faultTolerant) {
					Logging.warn(e);
					return false;
				}
				if (e instanceof IOException)
					throw (IOException) e;
				else
					throw new IOException(e);
			}
		}
	}
}
