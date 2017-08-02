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

package com.jaeksoft.searchlib.web;

import java.net.URI;
import java.net.URISyntaxException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;

public class ActionServlet extends AbstractServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -369063857059673597L;

	@Override
	protected void doRequest(ServletTransaction transaction) throws ServletException {
		try {
			Client client = transaction.getClient();
			String action = transaction.getParameterString("action");
			User user = transaction.getLoggedUser();
			if (user != null && !user.hasRole(client.getIndexName(), Role.INDEX_UPDATE))
				throw new SearchLibException("Not permitted");
			if ("deleteAll".equalsIgnoreCase(action))
				client.deleteAll();
			else if ("reload".equalsIgnoreCase(action))
				client.reload();
			else if ("close".equalsIgnoreCase(action))
				ClientCatalog.closeIndex(client.getIndexName());
			else if ("online".equalsIgnoreCase(action))
				client.setOnline(true);
			else if ("offline".equalsIgnoreCase(action))
				client.setOnline(false);
			transaction.addXmlResponse("Status", "OK");
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	public static void optimize(URI uri, String indexName, String login, String apikey, int secTimeOut)
			throws SearchLibException, URISyntaxException {
		call(secTimeOut, buildUri(uri, "/action", indexName, login, apikey, "action=optimize"));
	}

	public static void reload(URI uri, String indexName, String login, String apikey, int secTimeOut)
			throws SearchLibException, URISyntaxException {
		call(secTimeOut, buildUri(uri, "/action", indexName, login, apikey, "action=reload"));
	}

	public static void close(URI uri, String indexName, String login, String apikey, int secTimeOut)
			throws SearchLibException, URISyntaxException {
		call(secTimeOut, buildUri(uri, "/action", indexName, login, apikey, "action=close"));
	}

	public static void online(URI uri, String indexName, String login, String apikey, int secTimeOut)
			throws SearchLibException, URISyntaxException {
		call(secTimeOut, buildUri(uri, "/action", indexName, login, apikey, "action=online"));
	}

	public static void offline(URI uri, String indexName, String login, String apikey, int secTimeOut)
			throws SearchLibException, URISyntaxException {
		call(secTimeOut, buildUri(uri, "/action", indexName, login, apikey, "action=offline"));
	}

}
