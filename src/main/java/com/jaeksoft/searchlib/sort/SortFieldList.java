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

package com.jaeksoft.searchlib.sort;

import java.io.IOException;

import com.jaeksoft.searchlib.index.ReaderAbstract;
import com.jaeksoft.searchlib.result.collector.CollectorInterface;
import com.jaeksoft.searchlib.schema.AbstractFieldList;
import com.jaeksoft.searchlib.sort.SorterAbstract.NoCollectorException;

public class SortFieldList extends AbstractFieldList<SortField> {

	public SortFieldList() {
		super(true);
	}

	public SortFieldList(final SortFieldList sortFieldList) {
		super(sortFieldList);
	}

	final public boolean isScore() {
		if (size() == 0)
			return true;
		for (SortField field : this)
			if (field.isScore())
				return true;
		return false;
	}

	final public boolean isDistance() {
		if (size() == 0)
			return false;
		for (SortField field : this)
			if (field.isDistance())
				return true;
		return false;
	}

	public SorterAbstract getSorter(final CollectorInterface collector,
			final ReaderAbstract reader) throws IOException,
			NoCollectorException {
		if (collector.getSize() == 0)
			return null;
		if (size() == 0)
			return new DescScoreSorter(collector);
		return new SortListSorter(this, collector, reader);
	}

}
