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

package com.jaeksoft.searchlib.scheduler.task;

import java.io.IOException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.config.Config;
import com.jaeksoft.searchlib.crawler.file.process.CrawlFileMaster;
import com.jaeksoft.searchlib.scheduler.TaskAbstract;
import com.jaeksoft.searchlib.scheduler.TaskLog;
import com.jaeksoft.searchlib.scheduler.TaskProperties;
import com.jaeksoft.searchlib.scheduler.TaskPropertyDef;
import com.jaeksoft.searchlib.util.Variables;

public class TaskFileCrawlerStop extends TaskAbstract {

	@Override
	public String getName() {
		return "File crawler - stop";
	}

	@Override
	public TaskPropertyDef[] getPropertyList() {
		return null;
	}

	@Override
	public String[] getPropertyValues(Config config, TaskPropertyDef propertyDef, TaskProperties taskProperties) {
		return null;
	}

	@Override
	public String getDefaultValue(Config config, TaskPropertyDef propertyDef) {
		return null;
	}

	@Override
	public void execute(Client client, TaskProperties properties, Variables variables, TaskLog taskLog)
			throws SearchLibException, IOException {
		CrawlFileMaster crawlMaster = client.getFileCrawlMaster();
		if (!crawlMaster.isRunning()) {
			taskLog.setInfo("Was not running");
			return;
		}
		taskLog.setInfo("Abort request");
		crawlMaster.abort();
		if (!crawlMaster.waitForEnd(600))
			taskLog.setInfo("Not stopped after 10 minutes");
		else
			taskLog.setInfo("Stopped");
	}
}
