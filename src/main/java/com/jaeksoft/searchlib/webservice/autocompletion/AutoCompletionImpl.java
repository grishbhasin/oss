/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2013-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaeksoft.searchlib.webservice.autocompletion;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.autocompletion.AutoCompletionItem;
import com.jaeksoft.searchlib.autocompletion.AutoCompletionManager;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.webservice.CommonListResult;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class AutoCompletionImpl extends CommonServices implements RestAutoCompletion {

	private AutoCompletionItem getAutoCompItem(AutoCompletionManager manager, String name)
			throws SearchLibException, IOException {
		AutoCompletionItem autoCompItem = manager.getItem(name);
		if (autoCompItem == null)
			throw new CommonServiceException(Status.NOT_FOUND, "Autocompletion item not found: " + name);
		return autoCompItem;
	}

	@Override
	public CommonResult set(String index, String login, String key, String name, List<String> fields, Integer rows) {
		if ((fields == null || fields.size() == 0) && rows == null)
			return build(index, login, key, name);
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			AutoCompletionManager manager = client.getAutoCompletionManager();
			AutoCompletionItem updateCompItem = manager.getItem(name);
			AutoCompletionItem autoCompItem = updateCompItem == null ?
					new AutoCompletionItem(client, name) :
					updateCompItem;
			if (fields != null)
				autoCompItem.setFields(fields);
			if (rows != null)
				autoCompItem.setRows(rows);
			if (updateCompItem != null)
				updateCompItem.save();
			else
				manager.add(autoCompItem);
			StringBuilder sb = new StringBuilder("Autocompletion item ");
			sb.append(name);
			sb.append(updateCompItem != null ? " updated." : " inserted");
			return new CommonResult(true, sb.toString());
		} catch (SearchLibException | IOException | InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonResult build(String index, String login, String key, String name) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			AutoCompletionItem autoCompItem = getAutoCompItem(client.getAutoCompletionManager(), name);
			CommonResult result = new CommonResult(true, null);
			autoCompItem.build(86400, 1000, result);
			return result;
		} catch (InterruptedException | IOException | SearchLibException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public AutoCompletionResult query(String index, String login, String key, String name, String prefix,
			Integer rows) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			AutoCompletionItem autoCompItem = getAutoCompItem(client.getAutoCompletionManager(), name);
			return new AutoCompletionResult(autoCompItem.search(prefix, rows));
		} catch (SearchLibException | IOException | InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public AutoCompletionResult queryPost(String index, String login, String key, String name, String prefix,
			Integer rows) {
		return query(index, login, key, name, prefix, rows);
	}

	@Override
	public CommonResult delete(String index, String login, String key, String name) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			AutoCompletionManager manager = client.getAutoCompletionManager();
			AutoCompletionItem autoCompItem = getAutoCompItem(manager, name);
			manager.delete(autoCompItem);
			return new CommonResult(true, "Autocompletion item " + name + " deleted");
		} catch (SearchLibException | InterruptedException | IOException e) {
			throw new CommonServiceException(e);
		}
	}

	@Override
	public CommonListResult<String> list(String index, String login, String key) {
		try {
			Client client = getLoggedClient(index, login, key, Role.INDEX_QUERY);
			ClientFactory.INSTANCE.properties.checkApi();
			AutoCompletionManager manager = client.getAutoCompletionManager();
			Collection<AutoCompletionItem> items = manager.getItems();
			CommonListResult<String> result = new CommonListResult<String>(items.size());
			for (AutoCompletionItem item : items)
				result.items.add(item.getName());
			result.computeInfos();
			return result;
		} catch (IOException | SearchLibException | InterruptedException e) {
			throw new CommonServiceException(e);
		}
	}
}
