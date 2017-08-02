/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.function.expression;

import com.jaeksoft.searchlib.function.token.DigitToken;

public class FloatExpression extends Expression {

	private float value;

	protected FloatExpression(char[] chars, int pos) {
		super(null);
		char[] addchars = { '.' };
		DigitToken token = new DigitToken(chars, pos, addchars);
		nextPos = pos + token.size;
		value = token.value;
	}

	@Override
	protected float getValue(float subQueryScore, float valSrcScore) {
		return value;
	}

	@Override
	protected float getValue(float subQueryScore, float[] valSrcScores) {
		return value;
	}

	@Override
	public String toString() {
		return Float.toString(value);
	}
}
