/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2015 Emmanuel Keller / Jaeksoft
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

public enum TemplateList {

	EMPTY_INDEX(new EmptyIndex()),

	WEB_CRAWLER(new WebCrawler()),

	FILE_CRAWLER(new FileCrawler()),

	USERS_CREDENTIALS(new UsersCredentialsIndex()),

	MULTI_INDEX(new MultiIndex());

	private TemplateAbstract template;

	private TemplateList(TemplateAbstract template) {
		this.template = template;
	}

	public TemplateAbstract getTemplate() {
		return template;
	}

	public static TemplateAbstract findTemplate(String name) {
		if (name == null)
			return EMPTY_INDEX.template;
		TemplateList template = TemplateList.valueOf(name.toUpperCase());
		if (template == null)
			return EMPTY_INDEX.template;
		return template.template;
	}

}
