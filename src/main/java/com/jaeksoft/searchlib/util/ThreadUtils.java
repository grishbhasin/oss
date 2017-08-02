/**   
 *
 * Copyright (C) 2013-2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.util;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import javax.naming.NamingException;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;

public class ThreadUtils {

	public static class ThreadGroupFactory implements ThreadFactory {

		private final ThreadGroup group;

		public ThreadGroupFactory(ThreadGroup group) {
			this.group = group;
		}

		@Override
		public Thread newThread(Runnable target) {
			return new Thread(group, target);
		}

	}

	public static class ThreadInfo {

		private final String name;

		private final String location;

		private final State state;

		private final String fullStackTrace;

		public ThreadInfo(Thread thread) {
			this.name = thread.getName();
			StackTraceElement[] elements = thread.getStackTrace();
			String l = ExceptionUtils.getLocation(elements);
			if (l == null)
				l = ExceptionUtils.getFirstLocation(elements);
			this.fullStackTrace = ExceptionUtils.getFullStackTrace(elements);
			this.location = l;
			this.state = thread.getState();
		}

		public String getName() {
			return name;
		}

		public String getLocation() {
			return location;
		}

		public State getState() {
			return state;
		}

		public String getFullStackTrace() {
			return fullStackTrace;
		}
	}

	public static Thread[] getThreadArray(ThreadGroup group) {
		Thread[] threads = new Thread[group.activeCount()];
		for (;;) {
			int l = group.enumerate(threads);
			if (l == threads.length)
				break;
			threads = new Thread[l];
		}
		return threads;
	}

	public static List<ThreadInfo> getInfos(ThreadGroup... groups) throws SearchLibException, NamingException {
		if (groups == null)
			return null;
		int count = 0;
		List<Thread[]> threadsArrayList = new ArrayList<Thread[]>(groups.length);
		for (ThreadGroup group : groups) {
			Thread[] threadArray = ThreadUtils.getThreadArray(group);
			threadsArrayList.add(threadArray);
			count += threadArray.length;
		}

		List<ThreadInfo> threadList = new ArrayList<ThreadInfo>(count);
		for (Thread[] threadArray : threadsArrayList)
			for (Thread thread : threadArray)
				threadList.add(new ThreadInfo(thread));
		return threadList;
	}

	public final static void sleepMs(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Logging.warn(e);
		}

	}

	public static interface WaitInterface {

		boolean done();

		boolean abort();
	}

	public static boolean waitUntil(long secTimeOut, WaitInterface waiter) {
		long finalTime = System.currentTimeMillis() + secTimeOut * 1000;
		while (!waiter.done()) {
			if (waiter.abort())
				return false;
			if (secTimeOut != 0)
				if (System.currentTimeMillis() > finalTime)
					return false;
			sleepMs(200);
		}
		return true;
	}

	public static class RecursiveTracker {

		private int count;
		private int max;
		private final int limit;

		public RecursiveTracker(int limit) {
			this.count = 0;
			this.max = 0;
			this.limit = limit;
		}

		public int getCount() {
			return count;
		}

		public RecursiveEntry enter() {
			return count == limit ? null : new RecursiveEntry();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("limit: ");
			sb.append(limit);
			sb.append(" count: ");
			sb.append(count);
			sb.append(" max: ");
			sb.append(max);
			return sb.toString();
		}

		public class RecursiveEntry {

			private RecursiveEntry() {
				count++;
				if (max < count)
					max = count;
			}

			public void release() {
				count--;
			}
		}

	}

	public static abstract class ExceptionCatchThread implements Callable<Exception> {

		protected Exception exception;

		public ExceptionCatchThread() {
			this.exception = null;
		}

		public abstract void runner() throws Exception;

		@Override
		final public Exception call() {
			try {
				runner();
				return null;
			} catch (Exception e) {
				exception = e;
				return exception;
			}
		}
	}

	public static void invokeAndJoin(ExecutorService executor,
			Collection<? extends ExceptionCatchThread> exceptionThreads) throws SearchLibException {
		try {
			executor.invokeAll(exceptionThreads);
			checkException(exceptionThreads);
		} catch (Exception e) {
			ExceptionUtils.<SearchLibException> throwException(e, SearchLibException.class);
		}
	}

	public static void checkException(Collection<? extends ExceptionCatchThread> exceptionThreads) throws Exception {
		if (exceptionThreads == null)
			return;
		Exception exception = null;
		for (ExceptionCatchThread thread : exceptionThreads)
			if (exception == null && thread.exception != null)
				exception = thread.exception;
		if (exception != null)
			throw exception;
	}

	public static <T> void done(List<Future<T>> futures) throws ExecutionException {
		ExecutionException exception = null;
		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (ExecutionException e) {
				if (exception == null)
					exception = e;
				Logging.warn(e);
			} catch (InterruptedException e) {
				Logging.warn(e);
			}
		}
		if (exception != null)
			throw exception;
	}

}
