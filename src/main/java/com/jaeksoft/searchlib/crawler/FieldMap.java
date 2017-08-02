/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.AnalyzerList;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.FieldValue;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.JsonUtils;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.util.map.GenericLink;
import com.jaeksoft.searchlib.util.map.SourceField;
import com.jaeksoft.searchlib.util.map.TargetField;

public class FieldMap extends FieldMapGeneric<SourceField, TargetField> {

	final private char concatSeparator;

	public FieldMap() {
		concatSeparator = '|';
	}

	public FieldMap(String multilineText, char fieldSeparator, char concatSeparator) throws IOException {
		StringReader sr = null;
		BufferedReader br = null;
		this.concatSeparator = concatSeparator;
		try {
			sr = new StringReader(multilineText);
			br = new BufferedReader(sr);
			String line;
			while ((line = br.readLine()) != null) {
				String[] cols = StringUtils.split(line, fieldSeparator);
				if (cols == null || cols.length < 2)
					continue;
				String source = cols[0];
				String target = cols[1];
				String analyzer = cols.length > 2 ? cols[2] : null;
				Float boost = cols.length > 3 ? Float.parseFloat(cols[3]) : null;
				add(new SourceField(source, concatSeparator), new TargetField(target, analyzer, boost, null));
			}
		} finally {
			IOUtils.close(br, sr);
		}
	}

	public FieldMap(File file)
			throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		super(file, "/map");
		concatSeparator = '|';
	}

	public FieldMap(Node node) throws XPathExpressionException {
		super(node);
		concatSeparator = '|';
	}

	@Override
	protected TargetField loadTarget(String targetName, Node node) {
		return new TargetField(targetName, node);
	}

	@Override
	protected SourceField loadSource(String sourceName) {
		return new SourceField(sourceName, concatSeparator);
	}

	@Override
	protected void writeTarget(XmlWriter xmlWriter, TargetField target) throws SAXException {
	}

	private void addFieldContent(FieldContent fc, TargetField targetField, IndexDocument target) throws IOException {
		if (fc == null)
			return;
		targetField.addFieldValueItems(fc.getValues(), target);
	}

	public void mapIndexDocument(IndexDocument source, IndexDocument target) throws IOException {
		for (GenericLink<SourceField, TargetField> link : getList()) {
			SourceField sourceField = link.getSource();
			if (sourceField.isUnique()) {
				FieldContent fc = sourceField.getUniqueString(source);
				if (fc == null)
					fc = sourceField.getUniqueString(target);
				addFieldContent(fc, link.getTarget(), target);
			} else {
				String value = sourceField.getConcatString(source, target);
				link.getTarget().addValue(value, target);
			}
		}
	}

	public void mapIndexDocument(ResultDocument source, IndexDocument target) throws IOException {
		for (GenericLink<SourceField, TargetField> link : getList()) {
			SourceField sourceField = link.getSource();
			if (sourceField.isUnique()) {
				List<FieldValueItem> fvi = sourceField.getUniqueString(source);
				link.getTarget().addFieldValueItems(fvi, target);
			} else {
				String value = sourceField.getConcatString(source, target);
				link.getTarget().addValue(value, target);
			}
		}
	}

	public void mapIndexDocumentJson(String target, ResultDocument sourceDocument, IndexDocument targetDocument)
			throws JsonProcessingException {
		Map<String, List<String>> map = new TreeMap<String, List<String>>();
		for (GenericLink<SourceField, TargetField> link : getList()) {
			TargetField targetField = link.getTarget();
			if (!target.equals(targetField.getName()))
				continue;
			SourceField sourceField = link.getSource();
			String source = sourceField.getUniqueName();
			FieldValue fieldValue = sourceDocument.getReturnFields().get(source);
			if (fieldValue == null)
				continue;
			if (fieldValue.getValuesCount() == 0)
				continue;
			List<String> list = map.get(source);
			if (list == null) {
				list = new ArrayList<String>(fieldValue.getValuesCount());
				map.put(source, list);
			}
			fieldValue.populate(list);
		}
		String json = JsonUtils.toJsonString(map);
		targetDocument.setString(target, json);
	}

	public void cacheAnalyzers(AnalyzerList analyzerList, LanguageEnum lang) throws SearchLibException {
		for (GenericLink<SourceField, TargetField> link : getList()) {
			TargetField target = link.getTarget();
			if (target == null)
				throw new SearchLibException("No target field for " + link.getSource());
			target.setCachedAnalyzer(analyzerList, lang);
		}
	}

	public boolean contains(String source, String target) {
		if (source == null || target == null)
			return false;
		for (GenericLink<SourceField, TargetField> link : getList())
			if (target.equals(link.getTarget().getName()))
				if (link.getSource().contains(source))
					return true;
		return false;
	}

	/**
	 * Return a map with all different boost for one target name
	 * 
	 * @param target
	 *            the target field
	 * @param boostMap
	 *            a boost map
	 */
	public void populateBoosts(String target, Map<Float, TargetField> boostMap) {
		if (target == null || boostMap == null)
			return;
		for (GenericLink<SourceField, TargetField> link : getList()) {
			TargetField targetField = link.getTarget();
			if (target.equals(targetField.getName())) {
				Float boost = targetField.getBoost();
				if (boost != null && boost != 1.0F)
					boostMap.put(boost, targetField);
			}
		}
	}

	public boolean isMapped(String target) {
		if (target == null)
			return false;
		for (GenericLink<SourceField, TargetField> link : getList())
			if (target.equals(link.getTarget().getName()))
				return true;
		return false;
	}

	public boolean isMapped(String source, String target) {
		if (source == null || target == null)
			return false;
		for (GenericLink<SourceField, TargetField> link : getList())
			if (target.equals(link.getTarget().getName()))
				if (link.getSource().contains(source))
					return true;
		return false;
	}
}
