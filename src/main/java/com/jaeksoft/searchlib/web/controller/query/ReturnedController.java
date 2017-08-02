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

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.RequestInterfaces;
import com.jaeksoft.searchlib.request.RequestTypeEnum;
import com.jaeksoft.searchlib.request.ReturnField;
import com.jaeksoft.searchlib.request.ReturnFieldList;
import com.jaeksoft.searchlib.schema.Indexed;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.Stored;

public class ReturnedController extends AbstractQueryController {

	private transient String selectedReturn;

	private transient List<String> fieldLeft;

	public ReturnedController() throws SearchLibException {
		super(RequestTypeEnum.SearchRequest,
				RequestTypeEnum.SearchFieldRequest,
				RequestTypeEnum.MoreLikeThisRequest,
				RequestTypeEnum.DocumentsRequest);
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedReturn = null;
		fieldLeft = null;
	}

	public void setSelectedReturn(String value) {
		synchronized (this) {
			selectedReturn = value;
		}
	}

	public String getSelectedReturn() {
		synchronized (this) {
			return selectedReturn;
		}
	}

	private RequestInterfaces.ReturnedFieldInterface getReturnedFieldInterface()
			throws SearchLibException {
		AbstractRequest req = getAbstractRequest();
		if (req instanceof RequestInterfaces.ReturnedFieldInterface)
			return (RequestInterfaces.ReturnedFieldInterface) req;
		return null;
	}

	@Command
	public void onReturnAdd() throws SearchLibException {
		synchronized (this) {
			if (selectedReturn == null)
				return;
			getReturnedFieldInterface().getReturnFieldList().put(
					new ReturnField(selectedReturn));
			reload();
		}
	}

	@Command
	public void onReturnRemove(@BindingParam("field") ReturnField field)
			throws SearchLibException {
		synchronized (this) {
			getReturnedFieldInterface().getReturnFieldList().remove(
					field.getName());
			reload();
		}
	}

	public boolean isFieldLeft() throws SearchLibException {
		synchronized (this) {
			List<String> list = getReturnFieldLeft();
			if (list == null)
				return false;
			return list.size() > 0;
		}
	}

	public List<String> getReturnFieldLeft() throws SearchLibException {
		synchronized (this) {
			Client client = getClient();
			if (client == null)
				return null;
			RequestInterfaces.ReturnedFieldInterface rfi = getReturnedFieldInterface();
			if (rfi == null)
				return null;
			if (fieldLeft != null)
				return fieldLeft;
			fieldLeft = new ArrayList<String>();
			ReturnFieldList fields = rfi.getReturnFieldList();
			for (SchemaField field : client.getSchema().getFieldList())
				if (field.checkStored(Stored.YES, Stored.COMPRESS)
						|| field.checkIndexed(Indexed.YES))
					if (fields.get(field.getName()) == null) {
						if (selectedReturn == null)
							selectedReturn = field.getName();
						fieldLeft.add(field.getName());
					}
			return fieldLeft;
		}
	}

	@Override
	public void reload() throws SearchLibException {
		synchronized (this) {
			selectedReturn = null;
			fieldLeft = null;
			super.reload();
		}
	}

	@Override
	public void eventSchemaChange(Client client) throws SearchLibException {
		reload();
	}
}
