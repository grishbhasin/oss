/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.crawler.common.database;

import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.StringUtils;
import com.jaeksoft.searchlib.util.XmlWriter;
import com.jaeksoft.searchlib.util.map.TargetField;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonFieldTarget extends TargetField {

	private boolean removeTag;

	private boolean convertHtmlEntities;

	private boolean filePath;

	private boolean crawlUrl;

	private boolean crawlFile;

	private String filePathPrefix;

	private String findRegexpTag;

	private String replaceRegexpTag;

	private Matcher findRegexpMatcher;

	public CommonFieldTarget(String targetName, boolean removeTag, boolean convertHtmlEntities, boolean filePath,
			String filePathPrefix, boolean crawlFile, boolean crawlUrl, String findRegexTag, String replaceRegexTag) {
		super(targetName);
		this.removeTag = removeTag;
		this.convertHtmlEntities = convertHtmlEntities;
		this.filePathPrefix = filePathPrefix;
		this.filePath = filePath;
		this.crawlFile = crawlFile;
		this.crawlUrl = crawlUrl;
		this.findRegexpTag = findRegexTag;
		this.replaceRegexpTag = replaceRegexTag;
		checkRegexpPattern();
	}

	public CommonFieldTarget(CommonFieldTarget from) {
		super(from.getName());
		this.copy(from);
	}

	public void copy(CommonFieldTarget from) {
		this.setName(from.getName());
		this.removeTag = from.removeTag;
		this.convertHtmlEntities = from.convertHtmlEntities;
		this.filePathPrefix = from.filePathPrefix;
		this.filePath = from.filePath;
		this.crawlFile = from.crawlFile;
		this.crawlUrl = from.crawlUrl;
		this.findRegexpTag = from.findRegexpTag;
		this.replaceRegexpTag = from.replaceRegexpTag;
		checkRegexpPattern();
	}

	public CommonFieldTarget(String targetName, Node targetNode) {
		super(targetName);
		removeTag = false;
		convertHtmlEntities = false;
		List<Node> nodeList = DomUtils.getNodes(targetNode, "filter");
		for (Node node : nodeList) {
			if ("yes".equalsIgnoreCase(DomUtils.getAttributeText(node, "removeTag")))
				removeTag = true;
			if ("yes".equalsIgnoreCase(DomUtils.getAttributeText(node, "convertHtmlEntities")))
				convertHtmlEntities = true;
			if ("yes".equalsIgnoreCase(DomUtils.getAttributeText(node, "filePath")))
				filePath = true;
			filePathPrefix = DomUtils.getAttributeText(node, "filePathPrefix");
			if ("yes".equalsIgnoreCase(DomUtils.getAttributeText(node, "crawlFile")))
				crawlFile = true;
			if ("yes".equalsIgnoreCase(DomUtils.getAttributeText(node, "crawlUrl")))
				crawlUrl = true;
			List<Node> nl = DomUtils.getNodes(node, "findRegexpTag");
			if (nl.size() > 0)
				findRegexpTag = StringEscapeUtils.unescapeXml(nl.get(0).getTextContent());
			nl = DomUtils.getNodes(node, "replaceRegexpTag");
			if (nl.size() > 0)
				replaceRegexpTag = StringEscapeUtils.unescapeXml(nl.get(0).getTextContent());
			checkRegexpPattern();
		}
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException {
		xmlWriter.startElement("filter", "removeTag", removeTag ? "yes" : "no", "convertHtmlEntities",
				convertHtmlEntities ? "yes" : "no", "filePath", filePath ? "yes" : "no", "filePathPrefix",
				filePathPrefix, "crawlFile", crawlFile ? "yes" : "no", "crawlUrl", crawlUrl ? "yes" : "no");
		if (findRegexpTag != null) {
			xmlWriter.startElement("findRegexpTag");
			xmlWriter.textNode(StringEscapeUtils.escapeXml11(findRegexpTag));
			xmlWriter.endElement();
		}
		if (replaceRegexpTag != null && replaceRegexpTag.length() > 0) {
			xmlWriter.startElement("replaceRegexpTag");
			xmlWriter.textNode(StringEscapeUtils.escapeXml11(replaceRegexpTag));
			xmlWriter.endElement();
		}
		xmlWriter.endElement();
	}

	/**
	 * @param removeTag
	 *            the removeTag to set
	 */
	public void setRemoveTag(boolean removeTag) {
		this.removeTag = removeTag;
	}

	/**
	 * @return the removeTag
	 */
	public boolean isRemoveTag() {
		return removeTag;
	}

	/**
	 * @param convertHtmlEntities
	 *            the convertHtmlEntities to set
	 */
	public void setConvertHtmlEntities(boolean convertHtmlEntities) {
		this.convertHtmlEntities = convertHtmlEntities;
	}

	/**
	 * @return the convertHtmlEntities
	 */
	public boolean isConvertHtmlEntities() {
		return convertHtmlEntities;
	}

	/**
	 * @return the filePathPrefix
	 */
	public String getFilePathPrefix() {
		return filePathPrefix;
	}

	public String getFilePath(String content) {
		StringBuilder path = new StringBuilder();
		if (filePathPrefix != null)
			path.append(filePathPrefix);
		path.append(content);
		return path.toString();
	}

	/**
	 * @param filePathPrefix
	 *            the filePathPrefix to set
	 */
	public void setFilePathPrefix(String filePathPrefix) {
		this.filePathPrefix = filePathPrefix;
	}

	/**
	 * @return the findRegexpTag
	 */
	public String getFindRegexpTag() {
		return findRegexpTag;
	}

	/**
	 * @param findRegexpTag
	 *            the findRegexTag to set
	 */
	public void setFindRegexpTag(String findRegexpTag) {
		this.findRegexpTag = findRegexpTag;
		checkRegexpPattern();
	}

	public final boolean hasRegexpPattern() {
		return (findRegexpMatcher != null);
	}

	private void checkRegexpPattern() {
		if (findRegexpTag != null)
			if (findRegexpTag.trim().length() == 0)
				findRegexpTag = null;
		findRegexpMatcher = findRegexpTag == null ? null : Pattern.compile(findRegexpTag).matcher("");
		if (replaceRegexpTag == null)
			replaceRegexpTag = StringUtils.EMPTY;
	}

	public final String applyRegexPattern(String text) {
		return findRegexpMatcher.reset(text).replaceAll(replaceRegexpTag);
	}

	/**
	 * @return the replaceRegexTag
	 */
	public String getReplaceRegexpTag() {
		return replaceRegexpTag;
	}

	/**
	 * @param replaceRegexpTag
	 *            the replaceRegexpTag to set
	 */
	public void setReplaceRegexpTag(String replaceRegexpTag) {
		this.replaceRegexpTag = replaceRegexpTag;
	}

	/**
	 * @param filePath
	 *            the FilePath to set
	 */
	public void setFilePath(boolean filePath) {
		this.filePath = filePath;
		if (!filePath)
			setFilePathPrefix(null);
	}

	/**
	 * @return the FilePath
	 */
	public boolean isFilePath() {
		return filePath;
	}

	/**
	 * @return true if the FilePath is not set
	 */
	public boolean isNotFilePath() {
		return !isFilePath();
	}

	public boolean isNotFilePathPrefixEditable() {
		return !(filePath || crawlFile);
	}

	/**
	 * @param crawlUrl
	 *            the crawlUrl to set
	 */
	public void setCrawlUrl(boolean crawlUrl) {
		this.crawlUrl = crawlUrl;
	}

	/**
	 * @return the crawlUrl
	 */
	public boolean isCrawlUrl() {
		return crawlUrl;
	}

	public boolean isNotCrawlUrl() {
		return !crawlUrl;
	}

	/**
	 * @param crawlFile
	 *            the crawlFile to set
	 */
	public void setCrawlFile(boolean crawlFile) {
		this.crawlFile = crawlFile;
	}

	/**
	 * @return the crawlFile
	 */
	public boolean isCrawlFile() {
		return crawlFile;
	}

	public boolean isNotCrawlFile() {
		return !crawlFile;
	}

}
