/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

public class AscDistanceSorter extends AbstractDistanceSorter {

	public AscDistanceSorter(final CollectorInterface collector)
			throws NoCollectorException {
		super(collector);
	}

	@Override
	final public int compare(final int pos1, final int pos2) {
		float d1 = distances[pos1];
		float d2 = distances[pos2];
		if (d1 > d2)
			return 1;
		else if (d1 < d2)
			return -1;
		else
			return 0;
	}

}
