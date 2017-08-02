/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2010-2014 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.replication;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.process.ThreadItem;
import com.jaeksoft.searchlib.util.*;
import com.jaeksoft.searchlib.web.PushServlet;
import com.jaeksoft.searchlib.web.controller.CommonController;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class ReplicationItem extends ThreadItem<ReplicationItem, ReplicationThread> {

	private final ReadWriteLock rwl = new ReadWriteLock();

	private String name = null;

	private URL instanceUrl = null;

	private String cachedUrl = null;

	private String login = null;

	private String indexName = null;

	private String apiKey = null;

	private ReplicationType replicationType;

	private int secTimeOut = 120;

	public final static String[] NOT_PUSHED_DATA_FOLDERS = { "index" };

	public final static String[] NOT_PUSHED_DATA_PATH = { "screenshot" };

	public final static String[] NOT_PUSHED_PATH =
			{ "replication.xml", "replication_old.xml", "jobs.xml", "jobs_old.xml", "report", "statstore" };

	public final static String[] NOT_PUSHED_PATH_NODB = { "web_crawler_url", "file_crawler_url" };

	public final static String[] NOT_PUSHED_INDEX = { "index" };

	public ReplicationItem(ReplicationMaster crawlMaster) {
		super(crawlMaster);
		replicationType = ReplicationType.MAIN_INDEX;
	}

	public ReplicationItem() {
		this((ReplicationMaster) null);
	}

	public ReplicationItem(ReplicationItem item) {
		super(item.threadMaster);
		this.copy(item);
	}

	public ReplicationItem(ReplicationMaster crawlMaster, String indexName, String url)
			throws MalformedURLException, URISyntaxException {
		this(crawlMaster);
		this.name = null;
		URL u = new URL(url);
		setInstanceUrl(
				u.getProtocol() + "://" + u.getHost() + (u.getPort() == -1 ? StringUtils.EMPTY : ':' + u.getPort()) + u
						.getPath());
		setIndexName(indexName);
		String userInfo = u.getUserInfo();
		if (userInfo != null && !userInfo.isEmpty()) {
			String[] auth = StringUtils.split(userInfo, ':');
			setLogin(auth[0]);
			if (auth.length > 1)
				setApiKey(auth[1]);
		}
		setReplicationType(ReplicationType.BACKUP_INDEX);
		setSecTimeOut(60000);
		updateName();
	}

	public ReplicationItem(ReplicationMaster crawlMaster, Node node) throws MalformedURLException, URISyntaxException {
		this(crawlMaster);
		this.name = null;
		String url = XPathParser.getAttributeString(node, "instanceUrl");
		if (url != null && url.length() > 0)
			setInstanceUrl(url);
		setIndexName(XPathParser.getAttributeString(node, "indexName"));
		setLogin(XPathParser.getAttributeString(node, "login"));
		String encodedApiKey = XPathParser.getAttributeString(node, "apiKey");
		if (encodedApiKey != null && encodedApiKey.length() > 0)
			setApiKey(StringUtils.base64decode(encodedApiKey));
		setReplicationType(ReplicationType.find(XPathParser.getAttributeString(node, "replicationType")));
		setSecTimeOut(XPathParser.getAttributeValue(node, "timeOut"));
		updateName();
	}

	private void updateName() throws MalformedURLException, URISyntaxException {
		String u = getInstanceUrl();
		if (!u.endsWith("/"))
			u += '/';
		u += getIndexName();
		name = u;
	}

	public String getName() {
		rwl.r.lock();
		try {
			return name;
		} finally {
			rwl.r.unlock();
		}
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException, UnsupportedEncodingException {
		rwl.r.lock();
		try {
			String encodedApiKey =
					(apiKey != null && apiKey.length() > 0) ? new String(StringUtils.base64encode(apiKey)) : "";
			xmlWriter.startElement("replicationItem", "instanceUrl", instanceUrl.toExternalForm(), "indexName",
					indexName, "login", login, "apiKey", encodedApiKey, "replicationType", replicationType.name(),
					"timeOut", Integer.toString(secTimeOut));
			xmlWriter.endElement();
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param url the instanceUrl to set
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public void setInstanceUrl(String url) throws MalformedURLException, URISyntaxException {
		rwl.w.lock();
		try {
			this.instanceUrl = LinkUtils.newEncodedURL(url);
			updateName();
			this.cachedUrl = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the instanceUrl
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public String getInstanceUrl() throws MalformedURLException, URISyntaxException {
		rwl.r.lock();
		try {
			if (instanceUrl != null)
				return instanceUrl.toExternalForm();
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (instanceUrl != null)
				return instanceUrl.toExternalForm();
			instanceUrl = LinkUtils.newEncodedURL(CommonController.getBaseUrl().toString());
			return instanceUrl.toExternalForm();
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @param indexName the indexName to set
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public void setIndexName(String indexName) throws MalformedURLException, URISyntaxException {
		rwl.w.lock();
		try {
			this.indexName = indexName;
			updateName();
			this.cachedUrl = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the indexName
	 */
	public String getIndexName() {
		rwl.r.lock();
		try {
			return indexName;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param apiKey the apiKey to set
	 */
	public void setApiKey(String apiKey) {
		rwl.w.lock();
		try {
			this.apiKey = apiKey;
			this.cachedUrl = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the apiKey
	 */
	public String getApiKey() {
		rwl.r.lock();
		try {
			return apiKey;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param login the login to set
	 */
	public void setLogin(String login) {
		rwl.w.lock();
		try {
			this.login = login;
			this.cachedUrl = null;
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the login
	 */
	public String getLogin() {
		rwl.r.lock();
		try {
			return login;
		} finally {
			rwl.r.unlock();
		}
	}

	public void copy(ReplicationItem item) {
		rwl.w.lock();
		try {
			item.copyTo(this);
			this.name = item.name;
			this.indexName = item.indexName;
			this.instanceUrl = item.instanceUrl;
			this.login = item.login;
			this.apiKey = item.apiKey;
			this.replicationType = item.replicationType;
			this.secTimeOut = item.secTimeOut;
			this.cachedUrl = null;
		} finally {
			rwl.w.unlock();
		}
	}

	public String getCachedUrl() throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
		rwl.r.lock();
		try {
			if (cachedUrl != null)
				return cachedUrl;
		} finally {
			rwl.r.unlock();
		}
		rwl.w.lock();
		try {
			if (cachedUrl != null)
				return cachedUrl;
			cachedUrl = PushServlet.getCachedUrl(this);
			return cachedUrl;
		} finally {
			rwl.w.unlock();
		}
	}

	public File getDirectory(Config config) throws SearchLibException {
		rwl.r.lock();
		try {
			switch (replicationType) {
			case MAIN_INDEX:
			case BACKUP_INDEX:
			case SCHEMA_ONLY:
				return config.getDirectory();
			case WEB_CRAWLER_URL_DATABASE:
				return config.getUrlManager().getDbClient().getDirectory();
			case FILE_CRAWLER_URI_DATABASE:
				return config.getFileManager().getDbClient().getDirectory();
			default:
				throw new SearchLibException("Unsupported replication");
			}
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @return the replicationType
	 */
	public ReplicationType getReplicationType() {
		rwl.r.lock();
		try {
			return replicationType;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param replicationType the replicationType to set
	 */
	public void setReplicationType(ReplicationType replicationType) {
		rwl.w.lock();
		try {
			this.replicationType = replicationType;
		} finally {
			rwl.w.unlock();
		}
	}

	@Override
	public int compareTo(ReplicationItem item) {
		return getName().compareTo(item.getName());
	}

	/**
	 * @return the secTimeOut
	 */
	public int getSecTimeOut() {
		rwl.r.lock();
		try {
			return secTimeOut;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param secTimeOut the secTimeOut to set
	 */
	public void setSecTimeOut(int secTimeOut) {
		rwl.w.lock();
		try {
			this.secTimeOut = secTimeOut == 0 ? 120 : secTimeOut;
		} finally {
			rwl.w.unlock();
		}
	}

}
