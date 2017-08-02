/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeSource;

import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.schema.FieldValueOriginEnum;

public class FieldContentPopulateFilter extends
		org.apache.lucene.analysis.TokenFilter {

	protected AttributeSource.State current = null;
	private CharTermAttribute termAtt;
	private FieldContent fieldContent;

	protected FieldContentPopulateFilter(FieldContent fieldContent,
			TokenStream input) {
		super(input);
		this.fieldContent = fieldContent;
		this.termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);
	}

	@Override
	public final boolean incrementToken() throws IOException {
		current = captureState();
		if (!input.incrementToken())
			return false;
		fieldContent.add(new FieldValueItem(FieldValueOriginEnum.EXTERNAL,
				termAtt.toString()));
		return true;
	}

}
