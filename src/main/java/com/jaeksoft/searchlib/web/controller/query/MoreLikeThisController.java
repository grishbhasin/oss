/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.query;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.MoreLikeThisRequest;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.request.ReturnField;
import com.jaeksoft.searchlib.request.ReturnFieldList;
import com.jaeksoft.searchlib.schema.SchemaField;

public class MoreLikeThisController extends AbstractQueryController {

	private transient List<String> fieldsLeft;

	private transient List<String> stopWordsList;

	private transient String selectedField;

	public MoreLikeThisController() throws SearchLibException {
		super(RequestTypeEnum.MoreLikeThisRequest);
	}

	@Override
	protected void reset() throws SearchLibException {
		fieldsLeft = null;
		stopWordsList = null;
		selectedField = null;
	}

	public List<String> getFieldsLeft() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			MoreLikeThisRequest request = (MoreLikeThisRequest) getRequest();
			if (request == null)
				return null;
			if (fieldsLeft != null)
				return fieldsLeft;
			fieldsLeft = new ArrayList<String>();
			ReturnFieldList fields = request.getFieldList();
			for (SchemaField field : client.getSchema().getFieldList())
				if (fields.get(field.getName()) == null) {
					if (selectedField == null)
						selectedField = field.getName();
					fieldsLeft.add(field.getName());
				}
			return fieldsLeft;
		}
	}

	/**
	 * @return the selectedField
	 */
	public String getSelectedField() {
		return selectedField;
	}

	/**
	 * @param selectedField
	 *            the selectedField to set
	 */
	public void setSelectedField(String selectedField) {
		this.selectedField = selectedField;
	}

	@Command
	@NotifyChange("*")
	public void onAddField() throws SearchLibException {
		((MoreLikeThisRequest) getRequest()).getFieldList().put(
				new ReturnField(selectedField));
		fieldsLeft = null;
	}

	@Command
	@NotifyChange("*")
	public void onRemoveField(@BindingParam("mltField") ReturnField field)
			throws SearchLibException {
		((MoreLikeThisRequest) getRequest()).getFieldList().remove(
				field.getName());
		fieldsLeft = null;
	}

	public List<String> getStopWordsList() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			if (stopWordsList != null)
				return stopWordsList;
			stopWordsList = new ArrayList<String>();
			stopWordsList.add("");
			String[] list = client.getStopWordsManager().getList(false);
			if (list != null)
				for (String s : list)
					stopWordsList.add(s);
			return stopWordsList;
		}
	}

	@Override
	public void eventSchemaChange(Client client) throws SearchLibException {
		reset();
		reload();
	}

}
