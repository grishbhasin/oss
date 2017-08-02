/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.crawler.filecrawler;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.jaeksoft.searchlib.crawler.file.process.fileInstances.swift.SwiftToken.AuthType;
import com.jaeksoft.searchlib.webservice.CommonListResult;
import com.jaeksoft.searchlib.webservice.CommonResult;

@Path("/crawler/file")
public interface RestFileCrawler {

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/run/once/{index}/xml")
	public CommonResult runOnceXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/run/once/{index}/json")
	public CommonResult runOnceJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/run/forever/{index}/xml")
	public CommonResult runForeverXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/run/forever/{index}/json")
	public CommonResult runForeverJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/stop/{index}/xml")
	public CommonResult stopXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/stop/{index}/json")
	public CommonResult stopJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/status/{index}/xml")
	public CommonResult statusXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/status/{index}/json")
	public CommonResult statusJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@PUT
	@Produces(MediaType.APPLICATION_XML)
	@Path("/repository/inject/localfile/{index}/xml")
	public CommonResult injectLocalFileRepositoryXML(
			@PathParam("index") String use, @QueryParam("login") String login,
			@QueryParam("key") String key, @QueryParam("path") String filePath,
			@QueryParam("ignoreHiddenFile") Boolean ignoreHiddenFile,
			@QueryParam("includeSubDirectory") Boolean withSubDirectory,
			@QueryParam("enabled") Boolean enabled,
			@QueryParam("delay") int delay);

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/repository/inject/localfile/{index}/json")
	public CommonResult injectLocalFileRepositoryJSON(
			@PathParam("index") String use, @QueryParam("login") String login,
			@QueryParam("key") String key, @QueryParam("path") String filePath,
			@QueryParam("ignoreHiddenFile") Boolean ignoreHiddenFile,
			@QueryParam("includeSubDirectory") Boolean withSubDirectory,
			@QueryParam("enabled") Boolean enabled,
			@QueryParam("delay") int delay);

	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	@Path("/repository/remove/localfile/{index}/xml")
	public CommonResult removeLocalFileRepositoryXML(
			@PathParam("index") String use, @QueryParam("login") String login,
			@QueryParam("key") String key, @QueryParam("path") String path);

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/repository/remove/localfile/{index}/json")
	public CommonResult removeLocalFileRepositoryJSON(
			@PathParam("index") String use, @QueryParam("login") String login,
			@QueryParam("key") String key, @QueryParam("path") String path);

	@PUT
	@Produces(MediaType.APPLICATION_XML)
	@Path("/repository/inject/smb/{index}/xml")
	public CommonResult injectSmbRepositoryXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("path") String path,
			@QueryParam("ignoreHiddenFile") Boolean ignoreHiddenFile,
			@QueryParam("includeSubDirectory") Boolean withSubDirectory,
			@QueryParam("enabled") Boolean enabled,
			@QueryParam("delay") int delay,
			@QueryParam("username") String username,
			@QueryParam("password") String password,
			@QueryParam("domain") String domain, @QueryParam("host") String host);

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/repository/inject/smb/{index}/json")
	public CommonResult injectSmbRepositoryJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("path") String path,
			@QueryParam("ignoreHiddenFile") Boolean ignoreHiddenFile,
			@QueryParam("includeSubDirectory") Boolean withSubDirectory,
			@QueryParam("enabled") Boolean enabled,
			@QueryParam("delay") int delay,
			@QueryParam("username") String username,
			@QueryParam("password") String password,
			@QueryParam("domain") String domain, @QueryParam("host") String host);

	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	@Path("/repository/remove/smb/{index}/xml")
	public CommonResult removeSmbRepositoryXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("path") String path,
			@QueryParam("username") String username,
			@QueryParam("domain") String domain, @QueryParam("host") String host);

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/repository/remove/smb/{index}/json")
	public CommonResult removeSmbRepositoryJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("path") String path,
			@QueryParam("username") String username,
			@QueryParam("domain") String domain, @QueryParam("host") String host);

	@PUT
	@Produces(MediaType.APPLICATION_XML)
	@Path("/repository/inject/ftp/{index}/xml")
	public CommonResult injectFtpRepositoryXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("path") String path,
			@QueryParam("ignoreHiddenFile") Boolean ignoreHiddenFile,
			@QueryParam("includeSubDirectory") Boolean withSubDirectory,
			@QueryParam("enabled") Boolean enabled,
			@QueryParam("delay") int delay,
			@QueryParam("username") String username,
			@QueryParam("password") String password,
			@QueryParam("host") String host, @QueryParam("ssl") boolean ssl);

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/repository/inject/ftp/{index}/json")
	public CommonResult injectFtpRepositoryJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("path") String path,
			@QueryParam("ignoreHiddenFile") Boolean ignoreHiddenFile,
			@QueryParam("includeSubDirectory") Boolean withSubDirectory,
			@QueryParam("enabled") Boolean enabled,
			@QueryParam("delay") int delay,
			@QueryParam("username") String username,
			@QueryParam("password") String password,
			@QueryParam("host") String host, @QueryParam("ssl") boolean ssl);

	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	@Path("/repository/remove/ftp/{index}/xml")
	public CommonResult removeFtpRepositoryXML(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("path") String path,
			@QueryParam("username") String username,
			@QueryParam("host") String host, @QueryParam("ssl") boolean ssl);

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/repository/remove/ftp/{index}/json")
	public CommonResult removeFtpRepositoryJSON(@PathParam("index") String use,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("path") String path,
			@QueryParam("username") String username,
			@QueryParam("host") String host, @QueryParam("ssl") boolean ssl);

	@PUT
	@Produces(MediaType.APPLICATION_XML)
	@Path("/repository/inject/swift/{index}/xml")
	public CommonResult injectSwiftRepositoryXML(
			@PathParam("index") String use, @QueryParam("login") String login,
			@QueryParam("key") String key, @QueryParam("path") String path,
			@QueryParam("ignoreHiddenFile") Boolean ignoreHiddenFile,
			@QueryParam("includeSubDirectory") Boolean withSubDirectory,
			@QueryParam("enabled") Boolean enabled,
			@QueryParam("delay") int delay,
			@QueryParam("username") String username,
			@QueryParam("password") String password,
			@QueryParam("tenant") String tenant,
			@QueryParam("container") String container,
			@QueryParam("authUrl") String authUrl,
			@QueryParam("authType") AuthType authType);

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/repository/inject/swift/{index}/json")
	public CommonResult injectSwiftRepositoryJSON(
			@PathParam("index") String use, @QueryParam("login") String login,
			@QueryParam("key") String key, @QueryParam("path") String path,
			@QueryParam("ignoreHiddenFile") Boolean ignoreHiddenFile,
			@QueryParam("includeSubDirectory") Boolean withSubDirectory,
			@QueryParam("enabled") Boolean enabled,
			@QueryParam("delay") int delay,
			@QueryParam("username") String username,
			@QueryParam("password") String password,
			@QueryParam("tenant") String tenant,
			@QueryParam("container") String container,
			@QueryParam("authUrl") String authUrl,
			@QueryParam("authType") AuthType authType);

	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	@Path("/repository/remove/swift/{index}/xml")
	public CommonResult removeSwiftRepositoryXML(
			@PathParam("index") String use, @QueryParam("login") String login,
			@QueryParam("key") String key, @QueryParam("path") String path,
			@QueryParam("username") String username,
			@QueryParam("container") String container);

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/repository/remove/swift/{index}/json")
	public CommonResult removeSwiftRepositoryJSON(
			@PathParam("index") String use, @QueryParam("login") String login,
			@QueryParam("key") String key, @QueryParam("path") String path,
			@QueryParam("username") String username,
			@QueryParam("container") String container);

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/index/{index_name}")
	public CommonListResult<FilePathResult> getList(
			@PathParam("index_name") String use,
			@QueryParam("login") String login, @QueryParam("key") String key);

}
