/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.model;

import java.util.List;

import org.zkoss.zkplus.databind.BindingListModel;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Panelchildren;
import org.zkoss.zul.event.ListDataListener;

import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.schema.FieldValueItem;

public class FieldContentModel implements BindingListModel<FieldValueItem> {

	private FieldContent fieldContent;

	protected FieldContentModel(FieldContent fieldContent) {
		this.fieldContent = fieldContent;
	}

	@Override
	public int indexOf(Object data) {
		if (fieldContent == null)
			return -1;
		List<FieldValueItem> values = fieldContent.getValues();
		if (values == null)
			return -1;
		int i = 0;
		for (FieldValueItem valueItem : values)
			if (valueItem == data)
				return i;
			else
				i++;
		return -1;
	}

	@Override
	public void addListDataListener(ListDataListener arg0) {
	}

	@Override
	public FieldValueItem getElementAt(int index) {
		if (fieldContent == null)
			return null;
		List<FieldValueItem> values = fieldContent.getValues();
		if (values == null)
			return null;
		return values.get(index);
	}

	@Override
	public int getSize() {
		if (fieldContent == null)
			return 0;
		List<FieldValueItem> values = fieldContent.getValues();
		if (values == null)
			return 0;
		return values.size();
	}

	@Override
	public void removeListDataListener(ListDataListener arg0) {
	}

	public String getField() {
		return fieldContent.getField();
	}

	public static Panelchildren createIndexDocumentComponent(
			FieldContent[] fieldContents) {
		Panelchildren component = new Panelchildren();
		for (FieldContent fieldContent : fieldContents) {
			Panel panel = new Panel();
			panel.setTitle(fieldContent.getField());
			panel.setCollapsible(true);
			Panelchildren panelchildren = new Panelchildren();
			Grid grid = new Grid();
			grid.setMold("paging");
			grid.setModel(new FieldContentModel(fieldContent));
			grid.setParent(panelchildren);
			panelchildren.setParent(panel);
			panel.setParent(component);
		}
		return component;
	}
}
