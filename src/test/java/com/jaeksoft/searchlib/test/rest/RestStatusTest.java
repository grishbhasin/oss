/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2016-2017 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.test.rest;

import com.jaeksoft.searchlib.webservice.CommonResult;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

/**
 * Created by aureliengiudici on 19/04/2016.
 */
public class RestStatusTest extends CommonRestAPI {
	public final static String path = "/services/rest/index/*/crawler/web/run";

	@Test
	public void testA_allStatus() throws IllegalStateException, IOException, XPathExpressionException, SAXException,
			ParserConfigurationException {
		Response response = client().path(path).request(MediaType.APPLICATION_JSON).get();
		checkCommonResult(response, CommonResult.class, 200);

	}
}
