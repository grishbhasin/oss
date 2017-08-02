/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.web.controller.crawler.file;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.GlobalCommand;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.web.controller.PushEvent;
import com.jaeksoft.searchlib.web.controller.ScopeAttribute;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;

@AfterCompose(superclass = true)
public class FileCrawlerController extends CrawlerController {

	public FileCrawlerController() throws SearchLibException {
		super();
	}

	protected FilePathItem getFilePathItemEdit() {
		return (FilePathItem) getAttribute(ScopeAttribute.FILEPATHITEM_EDIT);
	}

	protected FilePathItem getFilePathItemSelected() {
		return (FilePathItem) getAttribute(ScopeAttribute.FILEPATHITEM_SELECTED);
	}

	protected void setFilePathItemEdit(FilePathItem filePathItem) {
		setAttribute(ScopeAttribute.FILEPATHITEM_EDIT, filePathItem);
		PushEvent.eventEditFileRepository.publish(filePathItem);
	}

	protected void setFilePathItemSelected(FilePathItem filePathItem) {
		setAttribute(ScopeAttribute.FILEPATHITEM_SELECTED, filePathItem);
	}

	public boolean isFilePathEdit() {
		return getFilePathItemEdit() != null;
	}

	public boolean isNoFilePathEdit() {
		return !isFilePathEdit();
	}

	public boolean isFilePathSelected() {
		return getFilePathItemSelected() != null;
	}

	public boolean isNoFilePathSelected() {
		return !isFilePathSelected();
	}

	@Override
	protected void reset() throws SearchLibException {
		setFilePathItemEdit(null);
		setFilePathItemSelected(null);
	}

	@GlobalCommand
	@Override
	public void eventEditFileRepository(
			@BindingParam("filePathItem") FilePathItem filePathItem)
			throws SearchLibException {
		reload();
	}
}
