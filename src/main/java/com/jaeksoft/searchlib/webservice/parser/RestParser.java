/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014-2015 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.parser;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.webservice.CommonListResult;
import com.jaeksoft.searchlib.webservice.NameLinkItem;

public interface RestParser {

	@GET
	@Path("/parser")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CommonListResult<NameLinkItem> list(@Context UriInfo uriInfo,
			@QueryParam("login") String login, @QueryParam("key") String key);

	@GET
	@Path("/parser/{parser_name}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public ParserItemResult get(@Context UriInfo uriInfo,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@PathParam("parser_name") String parser_name);

	@PUT
	@Path("/parser/{parser_name}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public ParserDocumentsResult put(@Context UriInfo uriInfo,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@PathParam("parser_name") String parser_name,
			@QueryParam("lang") LanguageEnum language,
			@QueryParam("path") String path, InputStream inputStream);

	@PUT
	@Path("/parser")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public ParserDocumentsResult putMagic(@Context UriInfo uriInfo,
			@QueryParam("login") String login, @QueryParam("key") String key,
			@QueryParam("lang") LanguageEnum language,
			@QueryParam("name") String fileName,
			@QueryParam("type") String mimeType,
			@QueryParam("path") String path, InputStream inputStream);

}
