/*
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2012-2017 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.runtime;

import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.browser.BrowserDriverEnum;
import com.jaeksoft.searchlib.ocr.OcrManager;
import com.jaeksoft.searchlib.ocr.TesseractLanguageEnum;
import com.jaeksoft.searchlib.scheduler.TaskManager;
import com.jaeksoft.searchlib.util.properties.PropertyItem;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.CommonController;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import java.io.IOException;

@AfterCompose(superclass = true)
public class AdvancedComposer extends CommonController {

	public AdvancedComposer() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
	}

	public OcrManager getOcrManager() throws SearchLibException, IOException {
		return ClientCatalog.getOcrManager();
	}

	@Command
	public void onCheckTesseract() throws SearchLibException, InterruptedException {
		ClientCatalog.getOcrManager().checkTesseract();
		new AlertController("OK");
	}

	public BrowserDriverEnum[] getBrowserList() {
		return BrowserDriverEnum.values();
	}

	public BrowserDriverEnum getDefaultWebBrowserDriver() {
		return BrowserDriverEnum.find(ClientFactory.INSTANCE.getDefaultWebBrowserDriver().getValue(),
				BrowserDriverEnum.FIREFOX);
	}

	public void setDefaultWebBrowserDriver(BrowserDriverEnum driver) throws IOException, SearchLibException {
		ClientFactory.INSTANCE.getDefaultWebBrowserDriver().setValue(driver.name());
	}

	public PropertyItem<Integer> getMaxClauseCount() {
		return ClientFactory.INSTANCE.getBooleanQueryMaxClauseCount();
	}

	public PropertyItem<Boolean> getExternalParser() {
		return ClientFactory.INSTANCE.getExternalParser();
	}

	public PropertyItem<Boolean> getLogFullTrace() {
		return ClientFactory.INSTANCE.getLogFullTrace();
	}

	public ClientFactory getClientFactory() {
		return ClientFactory.INSTANCE;
	}

	public TesseractLanguageEnum[] getTesseractLanguageEnum() {
		return TesseractLanguageEnum.values();
	}

	public TaskManager getTaskManager() {
		return TaskManager.getInstance();
	}

	@Command
	@NotifyChange("*")
	public void onSchedulerRestart() throws SearchLibException {
		TaskManager.getInstance().stop();
		TaskManager.getInstance().start();
	}

}
