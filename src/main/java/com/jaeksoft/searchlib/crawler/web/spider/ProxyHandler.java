/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.web.spider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.RandomUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;

import com.jaeksoft.searchlib.crawler.web.database.WebPropertyManager;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.StringUtils;

public class ProxyHandler {

	final private List<HttpHost> proxyList;

	final private Set<String> exclusionSet;

	final private String username;

	final private String password;

	public ProxyHandler(WebPropertyManager webPropertyManager) throws IOException {
		if (!webPropertyManager.getProxyEnabled().getValue()) {
			proxyList = null;
			exclusionSet = null;
			username = null;
			password = null;
			return;
		}
		String proxyHost = webPropertyManager.getProxyHost().getValue();
		int proxyPort = webPropertyManager.getProxyPort().getValue();
		if (proxyHost == null || proxyHost.trim().length() == 0 || proxyPort == 0) {
			proxyList = null;
			exclusionSet = null;
			username = null;
			password = null;
			return;
		}
		username = webPropertyManager.getProxyUsername().getValue();
		password = webPropertyManager.getProxyPassword().getValue();

		proxyList = new ArrayList<HttpHost>();
		String[] proxyArray = StringUtils.splitLines(proxyHost);
		for (String proxy : proxyArray) {
			if (proxy == null)
				continue;
			proxy = proxy.trim();
			proxyList.add(new HttpHost(proxy, proxyPort, "http"));
		}
		exclusionSet = new TreeSet<String>();
		String line;
		StringReader sr = null;
		BufferedReader br = null;
		try {
			sr = new StringReader(webPropertyManager.getProxyExclusion().getValue());
			br = new BufferedReader(sr);
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0)
					exclusionSet.add(line);
			}
		} finally {
			IOUtils.close(br, sr);
		}
	}

	public HttpHost getAnyProxy() {
		if (proxyList == null)
			return null;
		return proxyList.get(RandomUtils.nextInt(0, proxyList.size()));
	}

	public boolean isProxy(URI uri) {
		if (proxyList == null || uri == null)
			return false;
		return !exclusionSet.contains(uri.getHost());
	}

	public void applyProxy(RequestConfig.Builder configBuilder, HttpHost proxy,
			CredentialsProvider credentialsProvider) {
		configBuilder.setProxy(proxy);
		if (!StringUtils.isEmpty(username))
			credentialsProvider.setCredentials(new AuthScope(proxy),
					new UsernamePasswordCredentials(username, password));
	}

}
