/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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
 **/

package com.jaeksoft.searchlib.crawler.web.database;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.crawler.TargetStatus;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.index.FieldContent;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.schema.FieldValueItem;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeDecimalFormat;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeSimpleDateFormat;
import com.jaeksoft.searchlib.util.LinkUtils;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UrlItem {

	private String urlString;
	private URL url;
	private String contentDispositionFilename;
	private String contentBaseType;
	private String contentTypeCharset;
	private Long contentLength;
	private String contentEncoding;
	private String lang;
	private String langMethod;
	private String host;
	private List<String> subhost;
	private Date when;
	private RobotsTxtStatus robotsTxtStatus;
	private FetchStatus fetchStatus;
	private Integer responseCode;
	private ParserStatus parserStatus;
	private IndexStatus indexStatus;
	private int count;
	private String md5size;
	private Date lastModifiedDate;
	private Date contentUpdateDate;
	private List<String> outLinks;
	private List<String> inLinks;
	private String parentUrl;
	private String redirectionUrl;
	private LinkItem.Origin origin;
	private List<String> headers;
	private int backlinkCount;
	private int depth;
	private String urlWhen;

	protected UrlItem() {
		urlString = null;
		url = null;
		contentDispositionFilename = null;
		contentBaseType = null;
		contentTypeCharset = null;
		contentLength = null;
		contentEncoding = null;
		lang = null;
		langMethod = null;
		host = null;
		subhost = null;
		outLinks = null;
		inLinks = null;
		when = new Date();
		robotsTxtStatus = RobotsTxtStatus.UNKNOWN;
		fetchStatus = FetchStatus.UN_FETCHED;
		responseCode = null;
		parserStatus = ParserStatus.NOT_PARSED;
		indexStatus = IndexStatus.NOT_INDEXED;
		count = 0;
		md5size = null;
		lastModifiedDate = null;
		contentUpdateDate = null;
		parentUrl = null;
		redirectionUrl = null;
		origin = null;
		headers = null;
		backlinkCount = 0;
		depth = 0;
		urlWhen = null;
	}

	protected void init(ResultDocument doc) {
		setUrl(doc.getValueContent(UrlItemFieldEnum.INSTANCE.url.getName(), 0));
		setHost(doc.getValueContent(UrlItemFieldEnum.INSTANCE.host.getName(), 0));
		setSubHost(doc.getValues(UrlItemFieldEnum.INSTANCE.subhost.getName()));
		addOutLinks(doc.getValues(UrlItemFieldEnum.INSTANCE.outlink.getName()));
		addInLinks(doc.getValues(UrlItemFieldEnum.INSTANCE.inlink.getName()));
		setContentDispositionFilename(
				doc.getValueContent(UrlItemFieldEnum.INSTANCE.contentDispositionFilename.getName(), 0));
		setContentBaseType(doc.getValueContent(UrlItemFieldEnum.INSTANCE.contentBaseType.getName(), 0));
		setContentTypeCharset(doc.getValueContent(UrlItemFieldEnum.INSTANCE.contentTypeCharset.getName(), 0));
		setContentLength(doc.getValueContent(UrlItemFieldEnum.INSTANCE.contentLength.getName(), 0));
		setContentEncoding(doc.getValueContent(UrlItemFieldEnum.INSTANCE.contentEncoding.getName(), 0));
		setLang(doc.getValueContent(UrlItemFieldEnum.INSTANCE.lang.getName(), 0));
		setLangMethod(doc.getValueContent(UrlItemFieldEnum.INSTANCE.langMethod.getName(), 0));
		setWhen(doc.getValueContent(UrlItemFieldEnum.INSTANCE.when.getName(), 0));
		setRobotsTxtStatusInt(doc.getValueContent(UrlItemFieldEnum.INSTANCE.robotsTxtStatus.getName(), 0));
		setFetchStatusInt(doc.getValueContent(UrlItemFieldEnum.INSTANCE.fetchStatus.getName(), 0));
		setResponseCode(doc.getValueContent(UrlItemFieldEnum.INSTANCE.responseCode.getName(), 0));
		setParserStatusInt(doc.getValueContent(UrlItemFieldEnum.INSTANCE.parserStatus.getName(), 0));
		setIndexStatusInt(doc.getValueContent(UrlItemFieldEnum.INSTANCE.indexStatus.getName(), 0));
		setMd5size(doc.getValueContent(UrlItemFieldEnum.INSTANCE.md5size.getName(), 0));
		setLastModifiedDate(doc.getValueContent(UrlItemFieldEnum.INSTANCE.lastModifiedDate.getName(), 0));
		setContentUpdateDate(doc.getValueContent(UrlItemFieldEnum.INSTANCE.contentUpdateDate.getName(), 0));
		setParentUrl(doc.getValueContent(UrlItemFieldEnum.INSTANCE.parentUrl.getName(), 0));
		setRedirectionUrl(doc.getValueContent(UrlItemFieldEnum.INSTANCE.redirectionUrl.getName(), 0));
		setOrigin(LinkItem.findOrigin(doc.getValueContent(UrlItemFieldEnum.INSTANCE.origin.getName(), 0)));
		addHeaders(doc.getValues(UrlItemFieldEnum.INSTANCE.headers.getName()));
		setBacklinkCount(doc.getValueContent(UrlItemFieldEnum.INSTANCE.backlinkCount.getName(), 0));
		setDepth(doc.getValueContent(UrlItemFieldEnum.INSTANCE.depth.getName(), 0));
		urlWhen = doc.getValueContent(UrlItemFieldEnum.INSTANCE.urlWhen.getName(), 0);
	}

	private void addHeaders(List<FieldValueItem> headersList) {
		if (headersList == null)
			return;
		if (headers == null)
			headers = new ArrayList<String>();
		for (FieldValueItem item : headersList)
			headers.add(item.getValue());
	}

	public List<String> getSubHost() {
		return subhost;
	}

	public List<String> getOutLinks() {
		return outLinks;
	}

	public List<String> getInLinks() {
		return inLinks;
	}

	public void setSubHost(List<FieldValueItem> subhostlist) {
		this.subhost = null;
		if (subhostlist == null)
			return;
		this.subhost = new ArrayList<String>();
		for (FieldValueItem item : subhostlist)
			this.subhost.add(item.getValue());
	}

	public void clearOutLinks() {
		if (outLinks == null)
			return;
		outLinks.clear();
	}

	public void addOutLinks(List<FieldValueItem> linkList) {
		if (linkList == null)
			return;
		if (outLinks == null)
			outLinks = new ArrayList<String>();
		for (FieldValueItem item : linkList)
			outLinks.add(item.getValue());
	}

	public void addOutLinks(FieldContent fieldContent) {
		if (fieldContent == null)
			return;
		addOutLinks(fieldContent.getValues());
	}

	public void clearInLinks() {
		if (inLinks == null)
			return;
		inLinks.clear();
	}

	public void addInLinks(List<FieldValueItem> linkList) {
		if (linkList == null)
			return;
		if (inLinks == null)
			inLinks = new ArrayList<String>();
		for (FieldValueItem item : linkList)
			inLinks.add(item.getValue());
	}

	public void addInLinks(FieldContent fieldContent) {
		if (fieldContent == null)
			return;
		addInLinks(fieldContent.getValues());
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public FetchStatus getFetchStatus() {
		if (fetchStatus == null)
			return FetchStatus.UN_FETCHED;
		return fetchStatus;
	}

	public void setParserStatus(ParserStatus status) {
		this.parserStatus = status;

	}

	public void setParserStatusInt(int v) {
		this.parserStatus = ParserStatus.find(v);

	}

	private void setParserStatusInt(String v) {
		if (v != null)
			setParserStatusInt(Integer.parseInt(v));
	}

	public String getContentTypeCharset() {
		return contentTypeCharset;
	}

	public void setContentTypeCharset(String v) {
		contentTypeCharset = v;

	}

	public String getContentDispositionFilename() {
		return contentDispositionFilename;
	}

	public void setContentDispositionFilename(String v) {
		contentDispositionFilename = v;
	}

	public String getContentBaseType() {
		return contentBaseType;
	}

	public void setContentBaseType(String v) {
		contentBaseType = v;

	}

	public void setContentEncoding(String v) {
		contentEncoding = v;

	}

	public String getContentEncoding() {
		return contentEncoding;
	}

	private void setContentLength(String v) {
		if (v == null)
			return;
		if (v.length() == 0)
			return;
		try {
			contentLength = longFormat.parse(v).longValue();
		} catch (ParseException e) {
			Logging.error(e.getMessage(), e);
		}
	}

	public void setContentLength(Long v) {
		contentLength = v;

	}

	public Long getContentLength() {
		return contentLength;
	}

	public ParserStatus getParserStatus() {
		if (parserStatus == null)
			return ParserStatus.NOT_PARSED;
		return parserStatus;
	}

	public void setIndexStatus(IndexStatus status) {
		this.indexStatus = status;

	}

	public void setIndexStatusInt(int v) {
		this.indexStatus = IndexStatus.find(v);

	}

	private void setIndexStatusInt(String v) {
		if (v != null)
			setIndexStatusInt(Integer.parseInt(v));

	}

	public IndexStatus getIndexStatus() {
		if (indexStatus == null)
			return IndexStatus.NOT_INDEXED;
		return indexStatus;
	}

	public RobotsTxtStatus getRobotsTxtStatus() {
		if (robotsTxtStatus == null)
			return RobotsTxtStatus.UNKNOWN;
		return robotsTxtStatus;
	}

	public void setRobotsTxtStatus(RobotsTxtStatus status) {
		this.robotsTxtStatus = status;

	}

	public void setRobotsTxtStatusInt(int v) {
		this.robotsTxtStatus = RobotsTxtStatus.find(v);

	}

	private void setRobotsTxtStatusInt(String v) {
		if (v != null)
			setRobotsTxtStatusInt(Integer.parseInt(v));

	}

	public void setFetchStatus(FetchStatus status) {
		this.fetchStatus = status;

	}

	public void setFetchStatusInt(int v) {
		this.fetchStatus = FetchStatus.find(v);

	}

	private void setFetchStatusInt(String v) {
		if (v != null)
			setFetchStatusInt(Integer.parseInt(v));

	}

	private void setResponseCode(String v) {
		if (v != null)
			responseCode = new Integer(v);

	}

	public void setResponseCode(Integer v) {
		responseCode = v;

	}

	public Integer getResponseCode() {
		return responseCode;
	}

	public String getUrl() {
		return urlString;
	}

	public URL getURL() {
		return url;
	}

	public void setUrl(String url) {
		synchronized (this) {
			this.urlString = url;
			this.url = LinkUtils.getURL(urlString, false);
			checkUrlWhen();
		}
	}

	public String getParentUrl() {
		return parentUrl;
	}

	public void setParentUrl(String parentUrl) {
		this.parentUrl = parentUrl;
	}

	public boolean isRedirection() {
		return redirectionUrl != null;
	}

	public String getRedirectionUrl() {
		return redirectionUrl;
	}

	public void setRedirectionUrl(String redirectionUrl) {
		this.redirectionUrl = redirectionUrl;
	}

	public LinkItem.Origin getOrigin() {
		return origin;
	}

	public void setOrigin(LinkItem.Origin origin) {
		this.origin = origin;
	}

	public Date getWhen() {
		return when;
	}

	public void setWhen(Date d) {
		if (d == null) {
			setWhenNow();
			return;
		}
		when = d;

	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	protected void setLastModifiedDate(String d) {
		try {
			this.lastModifiedDate = d == null ? null : whenDateFormat.parse(d);
		} catch (ParseException e) {
			Logging.error(e.getMessage(), e);
		}
	}

	public void setLastModifiedDate(Date d) {
		this.lastModifiedDate = d;
	}

	public void setLastModifiedDate(Long time) {
		this.lastModifiedDate = time == null ? null : new Date(time);
	}

	public Date getContentUpdateDate() {
		return contentUpdateDate;
	}

	protected void setContentUpdateDate(String d) {
		try {
			this.contentUpdateDate = d == null ? null : whenDateFormat.parse(d);
		} catch (ParseException e) {
			Logging.error(e.getMessage(), e);
		}
	}

	public void setContentUpdateDate(Date d) {
		this.contentUpdateDate = d;
	}

	final static ThreadSafeSimpleDateFormat whenDateFormat = new ThreadSafeSimpleDateFormat("yyyyMMddHHmmss");

	final static ThreadSafeDecimalFormat longFormat = new ThreadSafeDecimalFormat("00000000000000");

	protected void setWhen(String d) {
		if (d == null) {
			setWhenNow();
			return;
		}
		try {
			when = whenDateFormat.parse(d);
			checkUrlWhen();
		} catch (ParseException e) {
			Logging.error(e.getMessage(), e);
			setWhenNow();
		}

	}

	public void setWhenNow() {
		setWhen(new Date(System.currentTimeMillis()));
		checkUrlWhen();
	}

	public String getCount() {
		return Integer.toString(count);
	}

	public static List<String> buildSubHost(String host) {
		if (host == null)
			return null;
		List<String> subhost = new ArrayList<String>();
		int lastPos = host.length();
		while (lastPos > 0) {
			lastPos = host.lastIndexOf('.', lastPos - 1);
			if (lastPos == -1)
				break;
			subhost.add(host.substring(lastPos + 1));
		}
		subhost.add(host);
		return subhost;
	}

	public void populate(IndexDocument indexDocument) throws IOException {
		indexDocument.setString(UrlItemFieldEnum.INSTANCE.url.getName(), getUrl());
		indexDocument.setString(UrlItemFieldEnum.INSTANCE.when.getName(), whenDateFormat.format(when));
		if (url != null) {
			indexDocument.setString(UrlItemFieldEnum.INSTANCE.host.getName(), url.getHost());
			indexDocument.setStringList(UrlItemFieldEnum.INSTANCE.subhost.getName(), buildSubHost(url.getHost()));
		}
		if (inLinks != null)
			indexDocument.setStringList(UrlItemFieldEnum.INSTANCE.inlink.getName(), inLinks);
		if (outLinks != null)
			indexDocument.setStringList(UrlItemFieldEnum.INSTANCE.outlink.getName(), outLinks);
		if (responseCode != null)
			indexDocument.setObject(UrlItemFieldEnum.INSTANCE.responseCode.getName(), responseCode);
		if (contentDispositionFilename != null)
			indexDocument.setString(UrlItemFieldEnum.INSTANCE.contentDispositionFilename.getName(),
					contentDispositionFilename);
		if (contentBaseType != null)
			indexDocument.setString(UrlItemFieldEnum.INSTANCE.contentBaseType.getName(), contentBaseType);
		if (contentTypeCharset != null)
			indexDocument.setString(UrlItemFieldEnum.INSTANCE.contentTypeCharset.getName(), contentTypeCharset);
		if (contentLength != null)
			indexDocument
					.setString(UrlItemFieldEnum.INSTANCE.contentLength.getName(), longFormat.format(contentLength));
		if (contentEncoding != null)
			indexDocument.setString(UrlItemFieldEnum.INSTANCE.contentEncoding.getName(), contentEncoding);
		if (lang != null)
			indexDocument.setString(UrlItemFieldEnum.INSTANCE.lang.getName(), lang);
		if (langMethod != null)
			indexDocument.setString(UrlItemFieldEnum.INSTANCE.langMethod.getName(), langMethod);
		indexDocument.setObject(UrlItemFieldEnum.INSTANCE.robotsTxtStatus.getName(), robotsTxtStatus.value);
		indexDocument.setObject(UrlItemFieldEnum.INSTANCE.fetchStatus.getName(), fetchStatus.value);
		indexDocument.setObject(UrlItemFieldEnum.INSTANCE.parserStatus.getName(), parserStatus.value);
		indexDocument.setObject(UrlItemFieldEnum.INSTANCE.indexStatus.getName(), indexStatus.value);
		if (md5size != null)
			indexDocument.setString(UrlItemFieldEnum.INSTANCE.md5size.getName(), md5size);
		if (lastModifiedDate != null)
			indexDocument.setString(UrlItemFieldEnum.INSTANCE.lastModifiedDate.getName(),
					whenDateFormat.format(lastModifiedDate));
		if (contentUpdateDate != null)
			indexDocument.setString(UrlItemFieldEnum.INSTANCE.contentUpdateDate.getName(),
					whenDateFormat.format(contentUpdateDate));
		if (parentUrl != null)
			indexDocument.setString(UrlItemFieldEnum.INSTANCE.parentUrl.getName(), parentUrl);
		if (redirectionUrl != null)
			indexDocument.setString(UrlItemFieldEnum.INSTANCE.redirectionUrl.getName(), redirectionUrl);
		if (origin != null)
			indexDocument.setString(UrlItemFieldEnum.INSTANCE.origin.getName(), origin.name());
		if (headers != null)
			indexDocument.setStringList(UrlItemFieldEnum.INSTANCE.headers.getName(), headers);
		indexDocument.setString(UrlItemFieldEnum.INSTANCE.backlinkCount.getName(), longFormat.format(backlinkCount));
		indexDocument.setString(UrlItemFieldEnum.INSTANCE.depth.getName(), longFormat.format(depth));
		indexDocument.setString(UrlItemFieldEnum.INSTANCE.urlWhen.getName(), urlWhen);
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getLangMethod() {
		return langMethod;
	}

	public String getFullLang() {
		StringBuilder sb = new StringBuilder();
		if (lang != null)
			sb.append(lang);
		if (langMethod != null) {
			sb.append('(');
			sb.append(langMethod);
			sb.append(')');
		}
		return sb.toString();
	}

	public void setLangMethod(String langMethod) {
		this.langMethod = langMethod;
	}

	public TargetStatus getTargetResult() {
		if (robotsTxtStatus.targetStatus != TargetStatus.TARGET_UPDATE)
			return robotsTxtStatus.targetStatus;
		if (fetchStatus.targetStatus != TargetStatus.TARGET_UPDATE)
			return fetchStatus.targetStatus;
		if (parserStatus.targetStatus != TargetStatus.TARGET_UPDATE)
			return parserStatus.targetStatus;
		return indexStatus.targetStatus;
	}

	public String getMd5size() {
		return md5size;
	}

	public void setMd5size(String md5size) {
		this.md5size = md5size;
	}

	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}

	/**
	 * @return the backLinkCount
	 */
	public int getBacklinkCount() {
		return backlinkCount;
	}

	/**
	 * @param v the backLinkCount to set
	 */
	private void setBacklinkCount(String v) {
		if (v == null)
			return;
		if (v.length() == 0)
			return;
		try {
			backlinkCount = longFormat.parse(v).intValue();
		} catch (ParseException e) {
			Logging.error(e.getMessage(), e);
		}
	}

	/**
	 * @param backLinkCount the backLinkCount to set
	 */
	public void setBacklinkCount(int backLinkCount) {
		this.backlinkCount = backLinkCount;
	}

	/**
	 * @return the depth
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * @param depth the depth to set
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}

	/**
	 * @param v the depth to set
	 */
	private void setDepth(String v) {
		if (v == null)
			return;
		if (v.length() == 0)
			return;
		try {
			depth = longFormat.parse(v).intValue();
		} catch (ParseException e) {
			Logging.error(e.getMessage(), e);
		}
	}

	public void checkUrlWhen() {
		StringBuilder sb = new StringBuilder();
		if (when != null)
			sb.append(whenDateFormat.format(when));
		if (urlString != null)
			sb.append(urlString);
		urlWhen = sb.length() == 0 ? null : sb.toString();
	}

}
