/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.query.search;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.json.JSONException;
import org.json.JSONObject;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class FacetFieldItem {

	final public String term;

	final public long count;

	public FacetFieldItem() {
		term = null;
		count = 0;
	}

	public FacetFieldItem(long count, String term) {
		this.term = term;
		this.count = count;
	}

	public FacetFieldItem(JSONObject json) throws JSONException {
		this(json.getLong("count"), json.getString("term"));
	}
}
