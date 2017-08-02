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
package com.jaeksoft.searchlib.util.video;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.LinkUtils;

public class Vimeo {

	private final static String API_URL = "http://vimeo.com/api/v2/video/";

	private final static Pattern[] idPatterns = { Pattern.compile("/([0-9]+)"),
			Pattern.compile("/video/([0-9]+)") };

	public static VimeoItem getInfo(URL url, HttpDownloader httpDownloader)
			throws MalformedURLException, IOException, URISyntaxException,
			JSONException, IllegalStateException, SearchLibException {
		String videoId = getVideoId(url);
		if (videoId == null)
			throw new IOException("No video ID found: " + url);
		VimeoItem vimeoItem = VimeoItemCache.getItem(videoId);
		if (vimeoItem != null) {
			if (Logging.isDebug)
				Logging.debug("Vimeo cache");
			return vimeoItem;
		}
		StringBuilder videoApiURL = new StringBuilder();
		videoApiURL.append(API_URL);
		videoApiURL.append(videoId);
		videoApiURL.append(".json");

		DownloadItem downloadItem = httpDownloader.get(
				new URI(videoApiURL.toString()), null);
		InputStream vimeoResponse = null;
		try {
			vimeoResponse = downloadItem.getContentInputStream();
			if (vimeoResponse == null)
				throw new IOException(
						"No respond returned from Dailymotion API: "
								+ videoApiURL);
			vimeoItem = new VimeoItem(vimeoResponse, videoId);
			VimeoItemCache.addItem(videoId, vimeoItem);
			return vimeoItem;
		} finally {
			IOUtils.close(vimeoResponse);
		}
	}

	/*
	 * This method is to extract the Video id from Vimeo url
	 * http://vimeo.com/18609766
	 */
	private static String getVideoId(URL url) throws URISyntaxException {
		URI uri = url.toURI();
		// check clip_id in http://vimeo.com/moogaloop.swf?clip_id=VIDEO_ID
		List<NameValuePair> pairs = URLEncodedUtils.parse(uri, "UTF-8");
		for (NameValuePair pair : pairs)
			if ("clip_id".equals(pair.getName()))
				return pair.getValue();

		String path = uri.getPath();
		for (Pattern pattern : idPatterns) {
			synchronized (pattern) {
				Matcher urlMatcher = pattern.matcher(path);
				if (urlMatcher.matches())
					return urlMatcher.group(1);
			}
		}
		return null;
	}

	public final static void main(String[] args) throws MalformedURLException,
			IOException, URISyntaxException, JSONException {
		String[] urls = {
				"http://vimeo.com/18609766",
				"http://player.vimeo.com/video/18609766",
				"http://vimeo.com/moogaloop.swf?clip_id=18609766&amp;server=vimeo.com&amp;color=00adef&amp;fullscreen=1" };
		for (String u : urls) {
			URL url = LinkUtils.newEncodedURL(u);
			System.out.println(getVideoId(url));
		}

	}
}
