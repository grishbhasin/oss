/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/
package com.jaeksoft.searchlib.util.video;

import com.jaeksoft.searchlib.util.IOUtils;
import org.json.JSONException;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class VimeoItem {

	private final String title;
	private final String description;
	private final String videoId;
	private final String thumbnail;

	public VimeoItem(InputStream vimeoResponse, String videoId) throws JSONException, IOException {
		String jsonText = IOUtils.toString(vimeoResponse, "UTF-8");
		org.json.JSONArray jsonArray = new org.json.JSONArray(jsonText);
		org.json.JSONObject jsonObject = jsonArray.getJSONObject(0);
		this.title = jsonObject.getString("title");
		this.description = jsonObject.getString("description");
		this.videoId = videoId;
		this.thumbnail = jsonObject.getString("thumbnail_small");

	}

	final public String getTitle() {
		return title;
	}

	public String getVideoId() {
		return videoId;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	@Override
	final public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Title: ");
		sb.append(title);
		sb.append(" - Description: ");
		sb.append(description);
		sb.append(" - VideoId: ");
		sb.append(videoId);
		sb.append(" - thumbnail: ");
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	final public String toJson(URL url) {
		JSONObject json = new JSONObject();
		json.put("url", url.toExternalForm());
		json.put("title", title);
		json.put("description", description);
		json.put("videoId", videoId);
		json.put("thumbnail", thumbnail);
		return json.toJSONString();
	}

	public String getDescription() {
		return description;
	}

}
