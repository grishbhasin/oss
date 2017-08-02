/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.template;

public class WebCrawler extends TemplateAbstract {

	public final static String publicName = "web crawler";

	public final static String description = "This is an index with predefined fields, "
			+ "analysers and parsers. This template is suited to web crawling and "
			+ "indexation.";

	public final static String root = "web_crawler";

	public final static String[] resources = {

	"config.xml", "parsers.xml", "jobs.xml",

	"requests.xml", "webcrawler-mapping.xml",

	"webcrawler-urlfilter.xml", "webcrawler-properties.xml",

	"autocompletion" + "/" + "autocomplete.xml",

	"renderers" + '/' + "default.xml",

	"stopwords" + '/' + "English stop words",

	"stopwords" + '/' + "French stop words",

	"stopwords" + '/' + "German stop words"

	};

	protected WebCrawler() {
		super(root, resources, publicName, description);
	}

}
