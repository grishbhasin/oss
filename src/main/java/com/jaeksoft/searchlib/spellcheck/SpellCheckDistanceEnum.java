/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.spellcheck;

import org.apache.lucene.search.spell.JaroWinklerDistance;
import org.apache.lucene.search.spell.NGramDistance;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.apache.lucene.search.spell.StringDistance;

import com.jaeksoft.searchlib.SearchLibException;

public enum SpellCheckDistanceEnum {

	JaroWinklerDistance(JaroWinklerDistance.class),

	LevensteinDistance(LevensteinDistance.class),

	NGramDistance(NGramDistance.class);

	private Class<? extends StringDistance> distanceClass;

	private SpellCheckDistanceEnum(Class<? extends StringDistance> distanceClass) {
		this.distanceClass = distanceClass;
	}

	public StringDistance getNewInstance() throws SearchLibException {
		try {
			return distanceClass.newInstance();
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		}
	}

	public static SpellCheckDistanceEnum find(String name) {
		for (SpellCheckDistanceEnum distance : values())
			if (distance.name().equalsIgnoreCase(name))
				return distance;
		return LevensteinDistance;
	}

}
