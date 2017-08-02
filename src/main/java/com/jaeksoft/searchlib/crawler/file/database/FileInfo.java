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

package com.jaeksoft.searchlib.crawler.file.database;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;

import org.apache.commons.io.FilenameUtils;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.crawler.common.database.FetchStatus;
import com.jaeksoft.searchlib.crawler.common.database.IndexStatus;
import com.jaeksoft.searchlib.crawler.common.database.ParserStatus;
import com.jaeksoft.searchlib.crawler.file.process.FileInstanceAbstract;
import com.jaeksoft.searchlib.index.IndexDocument;
import com.jaeksoft.searchlib.result.ResultDocument;
import com.jaeksoft.searchlib.util.LinkUtils;
import com.jaeksoft.searchlib.util.StringUtils;

public class FileInfo {

	private Long fileSystemDate;
	private FileTypeEnum fileType;
	private String uriString;
	private String fileName;
	private String fileExtension;
	private Long fileSize;
	private FetchStatus fetchStatus;
	private ParserStatus parserStatus;
	private IndexStatus indexStatus;

	public FileInfo() {
		init();
	}

	public FileInfo(ResultDocument doc) throws UnsupportedEncodingException, URISyntaxException {
		init();
		setFileSystemDate(doc.getValueContent(FileItemFieldEnum.INSTANCE.fileSystemDate.getName(), 0));
		String s = doc.getValueContent(FileItemFieldEnum.INSTANCE.fileType.getName(), 0);
		if (s != null)
			setFileType(FileTypeEnum.valueOf(s));
		setUri(doc.getValueContent(FileItemFieldEnum.INSTANCE.uri.getName(), 0));
		setFileName(doc.getValueContent(FileItemFieldEnum.INSTANCE.fileName.getName(), 0));
		setFileExtension(doc.getValueContent(FileItemFieldEnum.INSTANCE.fileExtension.getName(), 0));
		setFileSize(doc.getValueContent(FileItemFieldEnum.INSTANCE.fileSize.getName(), 0));
		setFetchStatusInt(doc.getValueContent(FileItemFieldEnum.INSTANCE.fetchStatus.getName(), 0));
		setParserStatusInt(doc.getValueContent(FileItemFieldEnum.INSTANCE.parserStatus.getName(), 0));
		setIndexStatusInt(doc.getValueContent(FileItemFieldEnum.INSTANCE.indexStatus.getName(), 0));
	}

	public FileInfo(FileInstanceAbstract fileInstance) throws IOException {
		init();
		setUriFileNameExtension(fileInstance.getURI());

		URI uri = fileInstance.getURI();
		setUri(uri.toASCIIString());

		setFileName(fileInstance.getFileName());
		setFileExtension(FilenameUtils.getExtension(fileName));

		setFileSystemDate(fileInstance.getLastModified());
		setFileSize(fileInstance.getFileSize());
		setFileType(fileInstance.getFileType());
	}

	protected void init() {
		fileSystemDate = null;
		fileType = null;
		fileSize = null;
		fileName = null;
		fileExtension = null;
		uriString = null;
		fetchStatus = FetchStatus.UN_FETCHED;
		parserStatus = ParserStatus.NOT_PARSED;
		indexStatus = IndexStatus.NOT_INDEXED;
	}

	public Long getFileSystemDate() {
		return fileSystemDate;
	}

	public void setFileSystemDate(Long d) {
		fileSystemDate = d;
	}

	private void setFileSystemDate(String d) {
		if (d == null) {
			fileSystemDate = null;
			return;
		}
		try {
			fileSystemDate = FileItem.dateFormat.parse(d).getTime();
		} catch (ParseException e) {
			Logging.warn(e.getMessage());
			fileSystemDate = null;
		}
	}

	public FileTypeEnum getFileType() {
		return fileType;
	}

	private void setFileType(FileTypeEnum type) {
		this.fileType = type;
	}

	public String getUri() {
		return uriString;
	}

	private void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	private void setFileSize(String size) {
		if (size != null)
			this.fileSize = Long.parseLong(size);
	}

	private void setFileSize(Long size) {
		this.fileSize = size;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public String getHumanSize() {
		if (fileSize == null)
			return null;
		return StringUtils.humanBytes(fileSize);
	}

	private void setUriFileNameExtension(URI uri) {
		String path = uri.getPath();
		setUri(uri.toASCIIString());
		setFileName(LinkUtils.lastPart(path));
		setFileExtension(FilenameUtils.getExtension(fileName));
	}

	private void setUri(String uriString) {
		this.uriString = uriString;
	}

	public FetchStatus getFetchStatus() {
		if (fetchStatus == null)
			return FetchStatus.UN_FETCHED;
		return fetchStatus;
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

	public IndexStatus getIndexStatus() {
		if (indexStatus == null)
			return IndexStatus.NOT_INDEXED;
		return indexStatus;
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

	public ParserStatus getParserStatus() {
		if (parserStatus == null)
			return ParserStatus.NOT_PARSED;
		return parserStatus;
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

	final public boolean isStatusFull() {
		return fetchStatus == FetchStatus.FETCHED
				&& (parserStatus == ParserStatus.PARSED || parserStatus == ParserStatus.PARSER_ERROR)
				&& (indexStatus == IndexStatus.INDEXED || indexStatus == IndexStatus.NOTHING_TO_INDEX
						|| indexStatus == IndexStatus.INDEX_ERROR);
	}

	/**
	 * Test if a new crawl is needed
	 * 
	 * @param newFileInfo
	 *            the current file
	 * @return true if the crawl is required
	 */
	final public boolean isNewCrawlNeeded(final FileInfo newFileInfo) {
		if (!isStatusFull())
			return true;
		if (fileSystemDate == null)
			return true;
		if (fileType == null)
			return true;
		if (newFileInfo.fileSystemDate == null)
			return true;
		if (fileSystemDate.longValue() != newFileInfo.fileSystemDate.longValue())
			return true;
		if (fileType != newFileInfo.fileType)
			return true;
		if (fileSize != null && newFileInfo.fileSize != null)
			if (fileSize.longValue() != newFileInfo.fileSize.longValue())
				return true;
		return false;
	}

	public void populate(IndexDocument indexDocument) {
		if (fileSystemDate != null)
			indexDocument.setString(FileItemFieldEnum.INSTANCE.fileSystemDate.getName(),
					FileItem.dateFormat.format(fileSystemDate));
		if (fileSize != null)
			indexDocument.setString(FileItemFieldEnum.INSTANCE.fileSize.getName(), fileSize.toString());
		if (fileName != null)
			indexDocument.setString(FileItemFieldEnum.INSTANCE.fileName.getName(), fileName.toString());
		indexDocument.setObject(FileItemFieldEnum.INSTANCE.fetchStatus.getName(), fetchStatus.value);
		indexDocument.setObject(FileItemFieldEnum.INSTANCE.parserStatus.getName(), parserStatus.value);
		indexDocument.setObject(FileItemFieldEnum.INSTANCE.indexStatus.getName(), indexStatus.value);
		if (fileType != null)
			indexDocument.setString(FileItemFieldEnum.INSTANCE.fileType.getName(), fileType.name());
		if (fileExtension != null)
			indexDocument.setString(FileItemFieldEnum.INSTANCE.fileExtension.getName(), fileExtension);
	}

}
