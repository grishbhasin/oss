/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.index;

import com.jaeksoft.searchlib.util.StringUtils;

public class FieldContentCacheKey implements Comparable<FieldContentCacheKey> {

	final public String fieldName;

	final public Integer docId;

	public FieldContentCacheKey(final String fieldName, final int docId) {
		this.fieldName = fieldName;
		this.docId = docId;
	}

	@Override
	final public int compareTo(final FieldContentCacheKey o) {
		int c;
		if ((c = docId.compareTo(o.docId)) != 0)
			return c;
		if ((c = fieldName.compareTo(o.fieldName)) != 0)
			return c;
		return 0;
	}

	@Override
	final public String toString() {
		return StringUtils.fastConcat(Integer.toString(docId), ":", fieldName);
	}
}
