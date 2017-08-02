/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import com.jaeksoft.searchlib.util.LastModifiedAndSize;

public class ClientCatalogItem implements Comparable<ClientCatalogItem> {

	private final String indexName;

	private LastModifiedAndSize lastModifiedAndSize;

	public ClientCatalogItem(String indexName) {
		this.indexName = indexName == null ? null : indexName.intern();
		this.lastModifiedAndSize = null;
	}

	public String getIndexName() {
		return indexName;
	}

	public Client getClient() {
		try {
			return ClientCatalog.getClient(indexName);
		} catch (SearchLibException e) {
			Logging.error(e);
			return null;
		}
	}

	public Long getSize() {
		if (lastModifiedAndSize == null)
			return null;
		return lastModifiedAndSize.getSize();
	}

	public String getSizeString() {
		if (lastModifiedAndSize == null)
			return null;
		return FileUtils.byteCountToDisplaySize(lastModifiedAndSize.getSize());
	}

	public Integer getNumDocs() throws IOException, SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		if (!client.isOnline())
			return null;
		return client.getStatistics().getNumDocs();
	}

	public long getLastModified() {
		if (lastModifiedAndSize == null)
			return -1;
		return lastModifiedAndSize.getLastModified();
	}

	public Date getLastModifiedDate() {
		if (lastModifiedAndSize == null)
			return null;
		return new Date(lastModifiedAndSize.getLastModified());
	}

	public String getLastModifiedString() {
		Date dt = getLastModifiedDate();
		if (dt == null)
			return null;
		return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(dt);
	}

	public File getLastModifiedFile() {
		if (lastModifiedAndSize == null)
			return null;
		return lastModifiedAndSize.getLastModifiedFile();
	}

	@Override
	public int hashCode() {
		return indexName.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ClientCatalogItem))
			return false;
		ClientCatalogItem item = (ClientCatalogItem) o;
		return indexName.equals(item.indexName);
	}

	@Override
	public int compareTo(ClientCatalogItem o) {
		return indexName.compareTo(o.indexName);
	}

	public void computeInfos() throws SearchLibException {
		lastModifiedAndSize = ClientCatalog.getLastModifiedAndSize(indexName);
	}
}
