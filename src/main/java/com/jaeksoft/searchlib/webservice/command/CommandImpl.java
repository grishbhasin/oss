/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2011-2017 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.command;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;
import com.jaeksoft.searchlib.webservice.RestApplication;

import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class CommandImpl extends CommonServices implements RestCommand {

	private Client getClient(String use, String login, String key) {
		try {
			Client client = getLoggedClientAnyRole(use, login, key, Role.INDEX_UPDATE);
			ClientFactory.INSTANCE.properties.checkApi();
			return client;
		} catch (IOException | InterruptedException e) {
			throw new WebServiceException(e);
		}
	}

	public CommonResult truncate(String use, String login, String key) {
		try {
			Client client = getClient(use, login, key);
			client.deleteAll();
			return new CommonResult(true, "truncate");
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		}
	}

	public CommonResult merge(String use, String login, String key, String index) {
		try {
			Client client = getClient(use, login, key);
			Client sourceClient = getLoggedClientAnyRole(index, login, key, Role.GROUP_INDEX);
			client.mergeData(sourceClient);
			return new CommonResult(true, "merge");
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		}
	}

	public CommonResult online(String use, String login, String key) {
		try {
			Client client = getClient(use, login, key);
			client.setOnline(true);
			return new CommonResult(true, "online");
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		}
	}

	public CommonResult offline(String use, String login, String key) {
		try {
			Client client = getClient(use, login, key);
			client.setOnline(false);
			return new CommonResult(true, "offline");
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		}
	}

	public CommonResult reload(String use, String login, String key) {
		try {
			Client client = getClient(use, login, key);
			client.reload();
			return new CommonResult(true, "reload");
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult onlineJSON(String use, String login, String key) {
		return online(use, login, key);
	}

	@Override
	public CommonResult onlineXML(String use, String login, String key) {
		return online(use, login, key);
	}

	public static String getOnlineXML(User user, Client client) throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/command/online/{index}/xml", user, client);
	}

	public static String getOnlineJSON(User user, Client client) throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/command/online/{index}/json", user, client);
	}

	@Override
	public CommonResult offlineJSON(String use, String login, String key) {
		return offline(use, login, key);
	}

	@Override
	public CommonResult offlineXML(String use, String login, String key) {
		return offline(use, login, key);
	}

	public static String getOfflineXML(User user, Client client) throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/command/offline/{index}/xml", user, client);
	}

	public static String getOfflineJSON(User user, Client client) throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/command/offline/{index}/json", user, client);
	}

	@Override
	public CommonResult reloadJSON(String use, String login, String key) {
		return reload(use, login, key);
	}

	@Override
	public CommonResult reloadXML(String use, String login, String key) {
		return reload(use, login, key);
	}

	public static String getReloadXML(User user, Client client) throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/command/reload/{index}/xml", user, client);
	}

	public static String getReloadJSON(User user, Client client) throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/command/reload/{index}/json", user, client);
	}

	@Override
	public CommonResult truncateJSON(String use, String login, String key) {
		return truncate(use, login, key);
	}

	@Override
	public CommonResult truncateXML(String use, String login, String key) {
		return truncate(use, login, key);
	}

	public static String getTruncateXML(User user, Client client) throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/command/truncate/{index}/xml", user, client);
	}

	public static String getTruncateJSON(User user, Client client) throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/command/truncate/{index}/json", user, client);
	}

	@Override
	public CommonResult mergeJSON(String use, String login, String key, String index) {
		return merge(use, login, key, index);
	}

	@Override
	public CommonResult mergeXML(String use, String login, String key, String index) {
		return merge(use, login, key, index);
	}

	public static String getMergeXML(User user, Client client, String index) throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/command/merge/{index}/xml", user, client, "index", index);
	}

	public static String getMergeJSON(User user, Client client, String index) throws UnsupportedEncodingException {
		return RestApplication.getRestURL("/command/merge/{index}/json", user, client, "index", index);
	}
}
