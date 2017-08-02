/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.zk.ui.Executions;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;

@AfterCompose(superclass = true)
public class IndexController extends CommonController {

	public IndexController() throws SearchLibException, InterruptedException {
		super();
		if (isLogged())
			return;
		String username = getExecutionParameter("username");
		String password = getExecutionParameter("password");
		if (username != null && password != null) {
			User user = ClientCatalog.authenticate(username, password);
			if (user != null) {
				getSession().setAttribute(ScopeAttribute.LOGGED_USER.name(),
						user);
				return;
			}
			Thread.sleep(2000);
			Executions.sendRedirect("/auth.zul");
			return;
		}
		Executions.sendRedirect("/login.html");
	}

	@Override
	protected void reset() {
	}

	public String getIndexTitle() throws SearchLibException {
		String indexName = getIndexName();
		if (indexName == null)
			return "Indices";
		return " Index: " + indexName;
	}

	public boolean isCrawlerRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(),
				Role.GROUP_WEB_CRAWLER, Role.GROUP_FILE_CRAWLER,
				Role.GROUP_DATABASE_CRAWLER);
	}

	public boolean isSchedulerRights() throws SearchLibException {
		return isQueryRights();
	}

	public boolean isReplicationRights() throws SearchLibException {
		return isQueryRights();
	}

	public boolean isRuntimeRights() throws SearchLibException {
		return isAdminOrMonitoringOrNoUser() || isInstanceValid();
	}

	public boolean isPrivilegeRights() throws SearchLibException {
		if (isAdmin())
			return true;
		if (isNoUserList())
			return true;
		return false;
	}

}
