/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.converter;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

public class DurationConverter implements Converter<Object, Object, Component> {

	@Override
	public Object coerceToBean(Object value, Component component,
			BindContext ctx) {
		return IGNORED_VALUE;
	}

	@Override
	public Object coerceToUi(Object value, Component component, BindContext ctx) {
		if (value == null)
			return IGNORED_VALUE;
		long l = -1;
		if (value instanceof Long)
			l = ((Long) value) / 1000;
		else if (value instanceof Integer)
			l = (Integer) value;
		if (l == -1)
			return IGNORED_VALUE;
		return String.format("%d:%02d:%02d", l / 3600, (l % 3600) / 60,
				(l % 60));
	}
}
