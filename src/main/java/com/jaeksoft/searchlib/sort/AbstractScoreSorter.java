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

import com.jaeksoft.searchlib.result.collector.CollectorInterface;
import com.jaeksoft.searchlib.result.collector.ScoreInterface;

public abstract class AbstractScoreSorter extends SorterAbstract {

	final protected float[] scores;

	protected AbstractScoreSorter(final CollectorInterface collector)
			throws NoCollectorException {
		super(collector);
		ScoreInterface scoreCollector = collector
				.getCollector(ScoreInterface.class);
		if (scoreCollector == null)
			throw new NoCollectorException("Wrong collector ", collector);
		scores = scoreCollector.getScores();
	}

	@Override
	final public boolean isScore() {
		return true;
	}

	@Override
	final public boolean isDistance() {
		return false;
	}

	@Override
	public String toString(final int pos) {
		StringBuilder sb = new StringBuilder("Score: ");
		sb.append(scores[pos]);
		return sb.toString();
	}

}
