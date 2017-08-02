/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.crawler.file;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.FieldMap;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.util.map.SourceField;
import com.jaeksoft.searchlib.util.map.TargetField;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

@AfterCompose(superclass = true)
public class MappingFileController extends CrawlerController {

	private transient SchemaField selectedUrlField;

	private transient SchemaField selectedIndexField;

	public MappingFileController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() {
		selectedUrlField = null;
		selectedIndexField = null;
	}

	public List<SchemaField> getUrlFieldList() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			List<SchemaField> list = client.getFileManager().getDbClient()
					.getSchema().getFieldList().getList();
			if (list.size() > 0 && selectedUrlField == null)
				selectedUrlField = list.get(0);
			return list;
		}
	}

	@Override
	public void eventSchemaChange(Client client) throws SearchLibException {
		reload();
	}

	public List<SchemaField> getIndexFieldList() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			List<SchemaField> list = client.getSchema().getFieldList()
					.getList();
			if (list.size() > 0 && selectedIndexField == null)
				selectedIndexField = list.get(0);
			return list;
		}
	}

	public void setSelectedUrlField(SchemaField field) {
		synchronized (this) {
			selectedUrlField = field;
		}
	}

	public SchemaField getSelectedUrlField() {
		synchronized (this) {
			return selectedUrlField;
		}
	}

	public void setSelectedIndexField(SchemaField field) {
		synchronized (this) {
			selectedIndexField = field;
		}
	}

	public SchemaField getSelectedIndexField() {
		synchronized (this) {
			return selectedIndexField;
		}
	}

	public FieldMap getFieldMap() {
		try {
			Client client = getClient();
			if (client == null)
				return null;
			return client.getFileCrawlerFieldMap();
		} catch (SearchLibException e) {
			throw new RuntimeException(e);
		}
	}

	@Command
	public void onAdd() throws SearchLibException,
			TransformerConfigurationException, SAXException, IOException,
			XPathExpressionException, ParserConfigurationException {
		if (!isFileCrawlerParametersRights())
			throw new SearchLibException("Not allowed");
		if (selectedUrlField == null || selectedIndexField == null)
			return;
		FieldMap fieldMap = getFieldMap();
		fieldMap.add(new SourceField(selectedUrlField.getName()),
				new TargetField(selectedIndexField.getName()));
		fieldMap.store();
		reload();
	}

	@Command
	public void onLinkRemove(
			@BindingParam("link") GenericLink<SourceField, TargetField> link)
			throws SearchLibException, TransformerConfigurationException,
			SAXException, IOException, XPathExpressionException,
			ParserConfigurationException {
		if (!isFileCrawlerParametersRights())
			throw new SearchLibException("Not allowed");
		FieldMap fieldMap = getFieldMap();
		fieldMap.remove(link);
		fieldMap.store();
		reload();
	}

}
