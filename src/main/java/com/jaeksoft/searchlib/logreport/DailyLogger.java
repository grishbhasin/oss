/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.logreport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeDateFormat;
import com.jaeksoft.searchlib.util.FormatUtils.ThreadSafeSimpleDateFormat;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.SimpleLock;

public class DailyLogger {

	final private SimpleLock lock = new SimpleLock();

	final private static ThreadSafeDateFormat dailyFormat = new ThreadSafeSimpleDateFormat(
			"yyyy-MM-dd");

	final private ThreadSafeDateFormat timeStampFormat;

	final private File parentDir;

	final private String filePrefix;

	private long timeLimit;

	private String currentLogFileName;

	private PrintWriter printWriter = null;

	private FileWriter fileWriter = null;

	public DailyLogger(File parentDir, String filePrefix,
			ThreadSafeDateFormat timeStampFormat) {
		this.parentDir = parentDir;
		this.filePrefix = filePrefix;
		this.timeStampFormat = timeStampFormat;
		setTimeLimit(System.currentTimeMillis());
	}

	private void setTimeLimit(long millis) {
		lock.rl.lock();
		try {
			close();
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(millis);
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			StringBuilder sb = new StringBuilder(filePrefix);
			sb.append('.');
			sb.append(dailyFormat.format(cal.getTime()));
			currentLogFileName = sb.toString();
			cal.add(Calendar.DAY_OF_MONTH, 1);
			timeLimit = cal.getTimeInMillis();
		} finally {
			lock.rl.unlock();
		}
	}

	final private void open() throws IOException {
		fileWriter = new FileWriter(new File(parentDir, currentLogFileName),
				true);
		printWriter = new PrintWriter(fileWriter);
	}

	final public void close() {
		lock.rl.lock();
		try {
			IOUtils.close(printWriter, fileWriter);
			printWriter = null;
			fileWriter = null;
		} finally {
			lock.rl.unlock();
		}
	}

	final protected void log(String message) throws SearchLibException {
		lock.rl.lock();
		try {
			long time = System.currentTimeMillis();
			if (time >= timeLimit)
				setTimeLimit(time);
			if (printWriter == null)
				open();
			if (timeStampFormat != null)
				printWriter.print(timeStampFormat.format(time));
			printWriter.println(message);
			printWriter.flush();
		} catch (IOException e) {
			close();
			throw new SearchLibException(e);
		} finally {
			lock.rl.unlock();
		}
	}
}
