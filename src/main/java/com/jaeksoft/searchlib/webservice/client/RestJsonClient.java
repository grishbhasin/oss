/**
 * License Agreement for OpenSearchServer
 * <p/>
 * Copyright (C) 2013-2016 Emmanuel Keller / Jaeksoft
 * <p/>
 * http://www.open-search-server.com
 * <p/>
 * This file is part of OpenSearchServer.
 * <p/>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.webservice.client;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftToken.AuthType;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class RestJsonClient {

	public final String oss_url;
	public final String oss_login;
	public final String oss_key;
	private final HttpDownloader downloader;

	public RestJsonClient(HttpDownloader downloader, String oss_url, String oss_login, String oss_key)
			throws IOException {
		this.downloader = downloader == null ? new HttpDownloader("RestJsonOssClient", false, null, 600) : downloader;
		this.oss_url = oss_url;
		this.oss_login = oss_login;
		this.oss_key = oss_key;
	}

	public RestJsonClient(String oss_url, String oss_login, String oss_key) throws IOException {
		this(null, oss_url, oss_login, oss_key);
	}

	public RestJsonClient(String oss_url) throws IOException {
		this(null, oss_url, null, null);
	}

	public boolean checkIndexExists(String indexName)
			throws ClientProtocolException, IllegalStateException, IOException, SearchLibException, URISyntaxException,
			JSONException {
		JsonTransaction transaction = new JsonTransaction(this, "/index/exists/json", null);
		transaction.addParam("name", indexName);
		return transaction.get(downloader).getJSONObject("result").getBoolean("info");
	}

	public void createIndex(String indexName, String templateName)
			throws URISyntaxException, ClientProtocolException, IllegalStateException, IOException, SearchLibException,
			JSONException {
		JsonTransaction transaction = new JsonTransaction(this, "/index/create/json", null);
		transaction.addParam("name", indexName);
		transaction.addParam("template", templateName);
		transaction.post(downloader);
	}

	public void fileCrawlerRunOnce(String indexName)
			throws ClientProtocolException, IllegalStateException, IOException, SearchLibException, URISyntaxException,
			JSONException {
		JsonTransaction transaction = new JsonTransaction(this, "/crawler/file/run/once/{index}/json", indexName);
		transaction.get(downloader);
	}

	public void injectSwiftRepository(HttpDownloader downloader, String indexName, String authURL, AuthType authType,
			String username, String password, String tenant, String container, String path, boolean ignoreHiddenFile,
			boolean includeSubDirectory, boolean enabled, int delay)
			throws ClientProtocolException, IllegalStateException, IOException, SearchLibException, URISyntaxException,
			JSONException {
		JsonTransaction transaction = new JsonTransaction(this, "/crawler/file/repository/inject/swift/{index}/json",
				indexName);
		transaction.addParam("path", "");
		transaction.addParam("ignoreHiddenFile", Boolean.toString(ignoreHiddenFile));
		transaction.addParam("includeSubDirectory", Boolean.toString(includeSubDirectory));
		transaction.addParam("enabled", Boolean.toString(enabled));
		transaction.addParam("delay", Integer.toString(delay));
		transaction.addParam("username", username);
		transaction.addParam("password", password);
		transaction.addParam("tenant", tenant);
		transaction.addParam("container", container);
		transaction.addParam("authUrl", authURL);
		transaction.addParam("authType", authType.name());
		transaction.put(downloader);
	}

	public JSONObject search(String indexName, String template, String query, Integer start, Integer rows,
			LanguageEnum lang, List<String> sorts, List<String> filters)
			throws ClientProtocolException, IllegalStateException, IOException, SearchLibException, URISyntaxException,
			JSONException {
		JsonTransaction transaction = new JsonTransaction(this, "/select/search/{index}/json", indexName);
		transaction.addParam("template", template);
		transaction.addParam("query", query);
		if (start != null)
			transaction.addParam("start", start.toString());
		if (rows != null)
			transaction.addParam("rows", rows.toString());
		if (lang != null)
			transaction.addParam("lang", lang.name());
		if (sorts != null)
			for (String sort : sorts)
				transaction.addParam("sort", sort);
		if (filters != null)
			for (String filter : filters)
				transaction.addParam("filter", filter);
		return transaction.get(downloader);
	}

	public JSONObject autocompletion_query(String indexName, String prefix, Long rows)
			throws ClientProtocolException, IllegalStateException, IOException, SearchLibException, URISyntaxException,
			JSONException {
		JsonTransaction transaction = new JsonTransaction(this, "/autocompletion/query/{index}/json", indexName);
		transaction.addParam("prefix", prefix);
		if (rows != null)
			transaction.addParam("rows", Long.toString(rows));
		return transaction.get(downloader);
	}
}
