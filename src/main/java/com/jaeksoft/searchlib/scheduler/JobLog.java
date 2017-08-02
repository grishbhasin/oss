/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.scheduler;

import java.util.LinkedList;

import com.jaeksoft.searchlib.util.ReadWriteLock;

public class JobLog {

	private final int maxSize;

	private final LinkedList<TaskLog> logList;

	private TaskLog[] cacheLogArray;

	private final static TaskLog[] EMPTY_LOG_ARRAY = new TaskLog[0];

	private final ReadWriteLock rwl = new ReadWriteLock();

	protected JobLog(int size) {
		rwl.w.lock();
		try {
			cacheLogArray = EMPTY_LOG_ARRAY;
			maxSize = size;
			logList = new LinkedList<TaskLog>();
		} finally {
			rwl.w.unlock();
		}
	}

	protected void addLog(TaskLog taskLog) {
		rwl.w.lock();
		try {
			logList.addFirst(taskLog);
			if (logList.size() > maxSize)
				logList.removeLast();
			cacheLogArray = new TaskLog[logList.size()];
			logList.toArray(cacheLogArray);
		} finally {
			rwl.w.unlock();
		}
	}

	public TaskLog[] getLogs() {
		rwl.r.lock();
		try {
			return cacheLogArray;
		} finally {
			rwl.r.unlock();
		}
	}
}
