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
package com.jaeksoft.searchlib.webservice.query.morelikethis;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.jaeksoft.searchlib.request.MoreLikeThisRequest;
import com.jaeksoft.searchlib.webservice.CommonResult;

@XmlRootElement(name = "result")
@XmlAccessorType(XmlAccessType.FIELD)
public class MoreLikeThisTemplateResult extends CommonResult {

	final public MoreLikeThisQuery query;

	public MoreLikeThisTemplateResult() {
		query = null;
	}

	public MoreLikeThisTemplateResult(MoreLikeThisRequest request) {
		super(true, null);
		this.query = new MoreLikeThisQuery(request);
	}
}
