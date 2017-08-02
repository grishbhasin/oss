/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2009-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.schema;

import org.apache.lucene.document.Field.Index;

public enum Indexed {

	YES(
			"The content of the field is indexed, allowing queries to be executed within that field."),

	NO(
			"The content of the field is not indexed, and queries cannot be executed within that field.");

	final public String description;
	final public String value;

	private Indexed(String description) {
		this.description = description;
		this.value = name().toLowerCase();
	}

	final public String getDescription() {
		return description;
	}

	final public String getValue() {
		return value;
	}

	final public static Indexed fromValue(String value) {
		for (Indexed fs : values())
			if (fs.name().equalsIgnoreCase(value))
				return fs;
		return Indexed.NO;
	}

	final public Index getLuceneIndex(String indexAnalyzer) {
		if (this == NO)
			return Index.NO;
		return indexAnalyzer == null ? Index.NOT_ANALYZED : Index.ANALYZED;
	}

}
