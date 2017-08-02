/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2013-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.mailbox;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.FieldMapContext;
import com.jaeksoft.searchlib.crawler.FieldMapGeneric;
import com.jaeksoft.searchlib.crawler.common.database.CommonFieldTarget;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.util.map.SourceField;

public class MailboxFieldMap extends
		FieldMapGeneric<SourceField, CommonFieldTarget> {

	@Override
	protected CommonFieldTarget loadTarget(String targetName, Node node) {
		return new CommonFieldTarget(targetName, node);
	}

	@Override
	protected SourceField loadSource(String source) {
		return new SourceField(source);
	}

	@Override
	protected void writeTarget(XmlWriter xmlWriter, CommonFieldTarget target)
			throws SAXException {
		target.writeXml(xmlWriter);
	}

	public boolean isUrl() {
		for (GenericLink<SourceField, CommonFieldTarget> link : getList()) {
			CommonFieldTarget dfTarget = link.getTarget();
			if (dfTarget.isCrawlUrl())
				return true;
		}
		return false;
	}

	public CommonFieldTarget getUniqueFieldTarget(Client client) {
		if (client == null)
			return null;
		String uniqueField = client.getSchema().getUniqueField();
		if (StringUtils.isEmpty(uniqueField))
			return null;
		List<CommonFieldTarget> fieldTargets = getLinks(new SourceField(
				MailboxFieldEnum.message_id.name()));
		if (fieldTargets == null)
			return null;
		for (CommonFieldTarget fieldTarget : fieldTargets)
			if (uniqueField.equals(fieldTarget.getName()))
				return fieldTarget;
		return null;
	}

	public void mapIndexDocument(FieldMapContext context, IndexDocument source,
			IndexDocument target) throws IOException, SearchLibException,
			ParseException, SyntaxError, URISyntaxException,
			ClassNotFoundException, InterruptedException,
			InstantiationException, IllegalAccessException {
		for (GenericLink<SourceField, CommonFieldTarget> link : getList()) {
			SourceField sourceField = link.getSource();
			CommonFieldTarget targetField = link.getTarget();
			if (sourceField.isUnique()) {
				FieldContent fc = sourceField.getUniqueString(source);
				if (fc == null)
					fc = sourceField.getUniqueString(target);
				mapFieldTarget(context, fc, targetField, target, null);
			} else {
				String value = sourceField.getConcatString(source, target);
				mapFieldTarget(context, targetField, value, target, null);
			}
		}
	}

}
