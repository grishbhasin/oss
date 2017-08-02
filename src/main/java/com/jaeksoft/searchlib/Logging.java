/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.Properties;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.jaeksoft.searchlib.util.ExceptionUtils;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.web.StartStopListener;

public class Logging {

	private static Logger logger = null;

	public static volatile boolean isDebug = System
			.getenv("OPENSEARCHSERVER_DEBUG") != null;;

	private static volatile boolean showStackTrace = true;

	private static void configure() {

		Properties props = new Properties();
		FileReader fileReader = null;
		try {
			File configLog = new File(
					StartStopListener.OPENSEARCHSERVER_DATA_FILE,
					"log4j.properties");
			if (!configLog.exists()) {
				PropertyConfigurator.configure(getLoggerProperties());
				return;
			}
			fileReader = new FileReader(configLog);
			props.load(fileReader);
			PropertyConfigurator.configure(props);
		} catch (FileNotFoundException e) {
			BasicConfigurator.configure();
			e.printStackTrace();
		} catch (IOException e) {
			BasicConfigurator.configure();
			e.printStackTrace();
		} catch (SearchLibException e) {
			BasicConfigurator.configure();
			e.printStackTrace();
		} finally {
			if (fileReader != null)
				try {
					fileReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public final static File getLogDirectory() {
		return new File(StartStopListener.OPENSEARCHSERVER_DATA_FILE, "logs");
	}

	public final static File[] getLogFiles() {
		File dirLog = getLogDirectory();
		if (!dirLog.exists())
			return null;
		return dirLog.listFiles();
	}

	private final static Properties getLoggerProperties()
			throws SearchLibException {
		File dirLog = getLogDirectory();
		if (!dirLog.exists())
			dirLog.mkdir();
		Properties props = new Properties();
		if (isDebug)
			props.put("log4j.rootLogger", "DEBUG, R");
		else
			props.put("log4j.rootLogger", "INFO, R");
		props.put("log4j.appender.R",
				"org.apache.log4j.DailyRollingFileAppender");
		props.put("log4j.appender.R.File", new File(
				StartStopListener.OPENSEARCHSERVER_DATA_FILE, "logs"
						+ File.separator + "oss.log").getAbsolutePath());
		props.put("log4j.appender.R.DatePattern", "'.'yyyy-MM-dd");
		props.put("log4j.appender.R.layout", "org.apache.log4j.PatternLayout");
		props.put("log4j.appender.R.layout.ConversionPattern",
				"%d{HH:mm:ss,SSS} %p: %c - %m%n");
		props.put(
				"log4j.logger.org.apache.cxf.jaxrs.impl.WebApplicationExceptionMapper",
				"ERROR");
		return props;
	}

	public static void initLogger() {
		configure();
		logger = Logger.getRootLogger();
	}

	private final static boolean noLogger(PrintStream ps, Object msg,
			Throwable e) {
		if (logger != null)
			return false;
		if (msg != null)
			ps.println(msg);
		if (e != null) {
			if (showStackTrace)
				e.printStackTrace();
			else
				ps.println(getStackTrace(e.getStackTrace()));
		}
		return true;
	}

	public static boolean isShowStackTrace() {
		return showStackTrace;
	}

	public static void setShowStackTrace(boolean show) {
		showStackTrace = show;
	}

	public final static void error(Object msg, Throwable e) {
		if (noLogger(System.err, msg, e))
			return;
		if (msg == null && e != null)
			msg = e.getMessage();
		if (showStackTrace)
			logger.error(msg, e);
		else {
			error(msg);
			if (e != null) {
				Throwable t = ExceptionUtils.getRootCause(e);
				error(t != null ? t.getStackTrace() : e.getStackTrace());
			}
		}
	}

	public final static void error(StackTraceElement[] stackTrace) {
		String s = getStackTrace(stackTrace);
		if (s != null)
			logger.error(s);
	}

	public final static void error(Object msg) {
		if (noLogger(System.err, msg, null))
			return;
		logger.error(msg);
	}

	public final static void error(Throwable e) {
		if (noLogger(System.err, e.getMessage(), e))
			return;
		error(e.getMessage(), e);
	}

	public final static void warn(Object msg, Throwable e) {
		if (noLogger(System.err, msg, e))
			return;
		if (msg == null && e != null)
			msg = e.getMessage();
		if (showStackTrace)
			logger.warn(msg, e);
		else
			logger.warn(msg);
	}

	public final static void warn(Object msg) {
		if (noLogger(System.err, msg, null))
			return;
		logger.warn(msg);
	}

	public final static String getStackTrace(StackTraceElement[] stackTrace) {
		if (stackTrace == null)
			return null;
		for (StackTraceElement element : stackTrace) {
			String className = element.getClassName();
			if (className.startsWith("com.jaeksoft.")
					|| className.startsWith("com.opensearchserver."))
				return element.toString();
		}
		return null;
	}

	public final static void warn(String msg, StackTraceElement[] stackTrace) {
		logger.warn(msg);
		String s = getStackTrace(stackTrace);
		if (s != null)
			logger.warn(s);
	}

	public final static void warn(Throwable e) {
		if (noLogger(System.err, e.getMessage(), e))
			return;
		warn(e.getMessage(), e);
	}

	public final static void info(Object msg, Throwable e) {
		if (noLogger(System.out, msg, e))
			return;
		if (msg == null && e != null)
			msg = e.getMessage();
		if (showStackTrace)
			logger.info(msg, e);
		else
			logger.info(msg);
	}

	public final static void info(Object msg) {
		if (noLogger(System.out, msg, null))
			return;
		logger.info(msg);
	}

	public final static void info(Throwable e) {
		if (noLogger(System.out, e.getMessage(), e))
			return;
		info(e.getMessage(), e);
	}

	public final static void debug(Object msg, Throwable e) {
		if (noLogger(System.out, msg, e))
			return;
		if (msg == null && e != null)
			msg = e.getMessage();
		if (showStackTrace)
			logger.debug(msg, e);
		else
			logger.debug(msg);
	}

	public final static void debug(Object msg) {
		if (noLogger(System.out, msg, null))
			return;
		logger.debug(msg);
	}

	public final static void debug(Throwable e) {
		if (noLogger(System.out, e.getMessage(), e))
			return;
		debug(e.getMessage(), e);
	}

	public final static String readLogs(int lines, String fileName)
			throws IOException {
		if (fileName == null)
			return null;
		File logFile = new File(getLogDirectory(), fileName);
		if (!logFile.exists())
			return null;
		FileReader fr = null;
		BufferedReader br = null;
		StringWriter sw = null;
		PrintWriter pw = null;
		LinkedList<String> list = new LinkedList<String>();
		try {
			fr = new FileReader(logFile);
			br = new BufferedReader(fr);
			String line = null;
			int size = 0;
			while ((line = br.readLine()) != null) {
				list.add(line);
				if (size++ > lines)
					list.remove();
			}
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			for (String l : list)
				pw.println(StringEscapeUtils.escapeJava(l));
			return sw.toString();
		} finally {
			IOUtils.close(br, fr, pw, sw);
		}
	}

}
