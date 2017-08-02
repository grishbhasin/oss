/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.crawler.web;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.AbstractPatternNameValueManager;
import com.jaeksoft.searchlib.crawler.web.database.CookieItem;

public class CookiesController extends AbstractNamedValueController<CookieItem> {

	public CookiesController() throws SearchLibException {
		super();
	}

	@Override
	protected AbstractPatternNameValueManager<CookieItem> getManager(
			Client client) throws SearchLibException {
		return client.getWebCookieManager();
	}

	@Override
	protected CookieItem newItem() {
		return new CookieItem();
	}

}
