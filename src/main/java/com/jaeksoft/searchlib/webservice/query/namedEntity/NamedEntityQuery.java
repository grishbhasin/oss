/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.query.namedEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.NamedEntityExtractionRequest;
import com.jaeksoft.searchlib.webservice.query.QueryAbstract;

@JsonInclude(Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class NamedEntityQuery extends QueryAbstract {

	final public String text;

	final public String searchRequest;

	final public String namedEntityField;

	final public Integer maxNumberOfWords;

	final public List<StopWords> stopWords;

	final public List<String> returnedFields;

	@JsonInclude(Include.NON_NULL)
	public static class StopWords {

		final public String listName;
		final public boolean ignoreCase;

		public StopWords() {
			this.listName = null;
			this.ignoreCase = true;
		}

		public StopWords(final String listName, final boolean ignoreCase) {
			this.listName = listName;
			this.ignoreCase = ignoreCase;
		}
	}

	public NamedEntityQuery() {
		text = null;
		searchRequest = null;
		namedEntityField = null;
		returnedFields = null;
		stopWords = null;
		maxNumberOfWords = null;
	}

	public NamedEntityQuery(NamedEntityExtractionRequest request) {
		text = request.getText();
		searchRequest = request.getSearchRequest();
		namedEntityField = request.getNamedEntityField();
		Collection<String> rfList = request.getReturnedFields();
		returnedFields = CollectionUtils.isEmpty(rfList) ? null : new ArrayList<String>(rfList);
		Map<String, Boolean> stopWordsMap = request.getStopWordsMap();
		stopWords = MapUtils.isEmpty(stopWordsMap) ? null : new ArrayList<StopWords>(stopWordsMap.size());
		if (stopWordsMap != null)
			for (Map.Entry<String, Boolean> entry : stopWordsMap.entrySet())
				stopWords.add(new StopWords(entry.getKey(), entry.getValue()));
		maxNumberOfWords = request.getMaxNumberOfWords();
	}

	@Override
	protected void apply(AbstractRequest req) {
		super.apply(req);
		NamedEntityExtractionRequest request = (NamedEntityExtractionRequest) req;
		if (text != null)
			request.setText(text);
		if (searchRequest != null)
			request.setSearchRequest(searchRequest);
		if (namedEntityField != null)
			request.setNamedEntityField(namedEntityField);
		if (returnedFields != null)
			request.setReturnedFields(returnedFields);
		if (stopWords != null)
			for (StopWords sw : stopWords)
				request.addStopWords(sw.listName, sw.ignoreCase);
		if (maxNumberOfWords != null)
			request.setMaxNumberOfWords(maxNumberOfWords);
	}
}
