/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jaeksoft.searchlib.SearchLibException.WrongStatusCodeException;
import com.jaeksoft.searchlib.crawler.web.database.HeaderItem;
import com.jaeksoft.searchlib.crawler.web.spider.DownloadItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.util.LinkUtils;

public class SwiftToken {

	public static enum AuthType {
		KEYSTONE("Keystone"), IAM("IAM");

		private final String label;

		private AuthType(String label) {
			this.label = label;
		}

		public static AuthType find(String type) {
			for (AuthType authType : values())
				if (authType.name().equalsIgnoreCase(type))
					return authType;
			return null;
		}

		public String getLabel() {
			return label;
		}
	}

	public static final String X_Auth_Token = "X-Auth-Token";

	private final String internalURL;
	private final String publicURL;
	private final String authToken;
	private final String username;

	private final List<HeaderItem> authHeaders;

	public SwiftToken(HttpDownloader httpDownloader, String authUrl, String username, String password,
			AuthType authType, String tenant)
					throws URISyntaxException, ClientProtocolException, IOException, JSONException {

		authHeaders = new ArrayList<HeaderItem>(1);

		DownloadItem downloadItem = null;
		switch (authType) {
		case KEYSTONE:
			downloadItem = keystoneRequest(httpDownloader, authUrl, username, tenant, password);
			break;
		case IAM:
			downloadItem = iamRequest(httpDownloader, authUrl, username, tenant);
			break;
		}
		if (downloadItem == null)
			throw new ClientProtocolException("Authentication failed");

		try {
			downloadItem.checkNoErrorRange(200, 204);
		} catch (WrongStatusCodeException e) {
			throw new IOException(e);
		}

		String jsonString = downloadItem.getContentAsString();
		JSONObject json = new JSONObject(jsonString);

		if (json.has("error")) {
			JSONObject jsonError = json.getJSONObject("error");
			String msg = jsonError.has("message") ? jsonError.getString("message") : jsonError.toString();
			throw new IOException(msg);
		}

		JSONObject jsonAccess = json.getJSONObject("access");
		json = jsonAccess.getJSONObject("token");
		authToken = json.getString("id");
		JSONArray jsonServices = jsonAccess.getJSONArray("serviceCatalog");
		String intUrl = null;
		String pubUrl = null;
		for (int i = 0; i < jsonServices.length(); i++) {
			JSONObject jsonService = jsonServices.getJSONObject(i);
			String type = jsonService.getString("type");
			String name = jsonService.getString("name");
			if ("object-store".equals(type) && "swift".equals(name)) {
				JSONArray jsonEndpoints = jsonService.getJSONArray("endpoints");
				for (int j = 0; j < jsonEndpoints.length(); j++) {
					JSONObject jsonEndpoint = jsonEndpoints.getJSONObject(j);
					intUrl = jsonEndpoint.getString("internalURL");
					pubUrl = jsonEndpoint.getString("publicURL");
					if (intUrl != null && pubUrl != null)
						break;
				}
				break;
			}
		}
		internalURL = intUrl;
		publicURL = pubUrl;
		this.username = username;
		authHeaders.add(new HeaderItem(X_Auth_Token, authToken));
	}

	private DownloadItem keystoneRequest(HttpDownloader httpDownloader, String authUrl, String username,
			String tenantName, String password) throws JSONException, URISyntaxException, ClientProtocolException,
					UnsupportedEncodingException, IOException, IllegalStateException {
		JSONObject jsonPasswordCredentials = new JSONObject();
		jsonPasswordCredentials.put("username", username);
		jsonPasswordCredentials.put("password", password);
		JSONObject jsonAuth = new JSONObject();
		jsonAuth.put("passwordCredentials", jsonPasswordCredentials);
		jsonAuth.put("tenantName", tenantName);
		JSONObject json = new JSONObject();
		json.put("auth", jsonAuth);
		URI uri = new URI(authUrl + "/tokens");
		List<HeaderItem> headers = new ArrayList<HeaderItem>(1);
		headers.add(new HeaderItem("Accept", "application/json"));
		return httpDownloader.post(uri, null, headers, null,
				new StringEntity(json.toString(), ContentType.APPLICATION_JSON));
	}

	private DownloadItem iamRequest(HttpDownloader httpDownloader, String authUrl, String username, String tenantname)
			throws URISyntaxException, ClientProtocolException, IOException, IllegalStateException {
		username = LinkUtils.UTF8_URL_Encode(username);
		StringBuilder u = new StringBuilder(authUrl);
		u.append("/users/");
		u.append(username);
		u.append("/credentials/openstack?tenantname=");
		u.append(tenantname);
		URI uri = new URI(u.toString());
		List<HeaderItem> headers = new ArrayList<HeaderItem>(1);
		headers.add(new HeaderItem("Accept", "application/json"));
		return httpDownloader.get(uri, null, headers, null);
	}

	final public List<HeaderItem> getAuthTokenHeader(final List<HeaderItem> headerList) {
		if (headerList == null)
			return this.authHeaders;
		headerList.add(new HeaderItem(X_Auth_Token, authToken));
		return headerList;
	}

	final public URI getContainerURI(final String container) throws URISyntaxException {
		StringBuilder sb = new StringBuilder(publicURL != null ? publicURL : internalURL);
		if (!container.startsWith("/"))
			sb.append('/');
		sb.append(container);
		return new URI(sb.toString());
	}

	final public URI getPathURI(final String container, final String path) throws URISyntaxException {
		StringBuilder sb = new StringBuilder(publicURL != null ? publicURL : internalURL);
		if (!container.startsWith("/"))
			sb.append('/');
		sb.append(container);
		sb.append('/');
		sb.append(path);
		return new URI(sb.toString());
	}

	final public URI getURI(final String container, final String path, final boolean prefixAndDelimiter)
			throws URISyntaxException, UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder(publicURL != null ? publicURL : internalURL);
		if (!container.startsWith("/"))
			sb.append('/');
		sb.append(container);
		if (!prefixAndDelimiter && path != null && path.length() > 0) {
			String[] paths = StringUtils.split(path, '/');
			for (String p : paths) {
				sb.append('/');
				sb.append(LinkUtils.UTF8_URL_Encode(p));
			}
			if (path.endsWith("/"))
				sb.append('/');
		} else {
			sb.append('?');
			if (path != null && path.length() > 0) {
				sb.append("prefix=");
				sb.append(LinkUtils.UTF8_URL_Encode(path));
				sb.append('&');
			}
			sb.append("delimiter=/&format=json");
		}
		return new URI(sb.toString());
	}

	public String getUsername() {
		return username;
	}
}
