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

package com.jaeksoft.searchlib.sort;

import com.jaeksoft.searchlib.result.collector.CollectorInterface;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;

public abstract class AbstractDocIdSorter extends SorterAbstract {

	final protected int[] ids;

	protected AbstractDocIdSorter(final CollectorInterface collector)
			throws NoCollectorException {
		super(collector);
		DocIdInterface docIdCollector = collector
				.getCollector(DocIdInterface.class);
		if (docIdCollector == null)
			throw new NoCollectorException("Wrong collector ", collector);
		ids = docIdCollector.getIds();
	}

	@Override
	final public boolean isScore() {
		return false;
	}

	@Override
	final public boolean isDistance() {
		return false;
	}

	@Override
	public String toString(final int pos) {
		StringBuilder sb = new StringBuilder("DocId: ");
		sb.append(ids[pos]);
		return sb.toString();
	}
}
