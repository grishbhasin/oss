/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2008-2016 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib;

import com.jaeksoft.searchlib.cluster.ClusterManager;
import com.jaeksoft.searchlib.config.ConfigFileRotation;
import com.jaeksoft.searchlib.config.ConfigFiles;
import com.jaeksoft.searchlib.index.IndexConfig;
import com.jaeksoft.searchlib.ocr.OcrManager;
import com.jaeksoft.searchlib.renderer.RendererResults;
import com.jaeksoft.searchlib.replication.ReplicationMerge;
import com.jaeksoft.searchlib.template.TemplateAbstract;
import com.jaeksoft.searchlib.template.TemplateList;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.user.UserList;
import com.jaeksoft.searchlib.util.*;
import com.jaeksoft.searchlib.web.StartStopListener;
import com.jaeksoft.searchlib.web.controller.PushEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.lucene.search.BooleanQuery;
import org.xml.sax.SAXException;
import org.zkoss.zk.ui.WebApp;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.net.URI;
import java.util.*;

/**
 * This class handles a list of indexes stored in a given directory.
 */
public class ClientCatalog {

	private static transient volatile TreeMap<File, Client> CLIENTS = new TreeMap<File, Client>();

	private static transient volatile TreeSet<File> OLD_CLIENTS = new TreeSet<File>();

	private static final ReadWriteLock clientsLock = new ReadWriteLock();

	private static final ReadWriteLock usersLock = new ReadWriteLock();

	private static UserList userList = null;

	private static final ConfigFiles configFiles = new ConfigFiles();

	private static final RendererResults rendererResults = new RendererResults();

	private static final ThreadGroup threadGroup = new ThreadGroup("Catalog");

	/**
	 * This method should be called first before using any other function of
	 * OpenSearchServer. It will initialize all internal resources. The
	 * data_directory is the folder which will contain all the indexes (data and
	 * configuration).
	 *
	 * @param data_directory The directory which contain the indexes
	 */
	public static final void init(File data_directory) {
		StartStopListener.start(data_directory);
	}

	/**
	 * Close OpenSearchServer. This method closes all indexes and stops any
	 * running task..
	 */
	public static final void close() {
		StartStopListener.shutdown();
	}

	private static final boolean isOldClient(File indexDirectory) {
		clientsLock.r.lock();
		try {
			return OLD_CLIENTS.contains(indexDirectory);
		} finally {
			clientsLock.r.unlock();
		}
	}

	private static final Client getClient(File indexDirectory) throws SearchLibException {
		clientsLock.r.lock();
		try {
			Client client = CLIENTS.get(indexDirectory);
			if (client != null)
				return client;
		} finally {
			clientsLock.r.unlock();
		}
		int i = 60;
		while (isOldClient(indexDirectory) && i > 0) {
			ThreadUtils.sleepMs(500);
			i--;
		}
		if (i == 0)
			throw new SearchLibException("Time out while getting " + indexDirectory);
		clientsLock.w.lock();
		try {
			Client client = CLIENTS.get(indexDirectory);
			if (client != null)
				return client;
			client = ClientFactory.INSTANCE.newClient(indexDirectory, true, false,
					ClientFactory.INSTANCE.properties.getSilentBackupUrl());
			CLIENTS.put(indexDirectory, client);
			return client;
		} finally {
			clientsLock.w.unlock();
		}
	}

	private static List<Client> findDepends(String indexName) {
		clientsLock.r.lock();
		try {
			ArrayList<Client> list = new ArrayList<Client>();
			for (Client client : CLIENTS.values()) {
				IndexConfig indexConfig = client.getIndex().getIndexConfig();
				if (indexConfig.isMulti())
					if (indexConfig.isIndexMulti(indexName)) {
						list.add(client);
					}
			}
			return list;
		} finally {
			clientsLock.r.unlock();
		}
	}

	public static final void openAll() {
		try {
			synchronized (ClientCatalog.class) {
				for (ClientCatalogItem catalogItem : getClientCatalog(null)) {
					Logging.info("OSS loads index " + catalogItem.getIndexName());
					getClient(catalogItem.getIndexName());
				}
			}
		} catch (SearchLibException e) {
			Logging.error(e);
		}
	}

	public static final void closeAll() {
		synchronized (ClientCatalog.class) {
			clientsLock.r.lock();
			try {
				for (Client client : CLIENTS.values()) {
					if (client == null)
						continue;
					Logging.info("OSS unloads index " + client.getIndexName());
					client.close();
				}
			} finally {
				clientsLock.r.unlock();
			}
			rendererResults.release();
		}
	}

	public static final long countAllDocuments() throws IOException, SearchLibException {
		long count = 0;
		clientsLock.r.lock();
		try {
			for (Client client : CLIENTS.values()) {
				if (client.isTrueReplicate())
					continue;
				count += client.getStatistics().getNumDocs();
			}
		} finally {
			clientsLock.r.unlock();
		}
		return count;
	}

	private static volatile long lastInstanceSize = 0;

	public static final long calculateInstanceSize() throws SearchLibException {
		if (StartStopListener.OPENSEARCHSERVER_DATA_FILE == null)
			return 0;
		lastInstanceSize = new LastModifiedAndSize(StartStopListener.OPENSEARCHSERVER_DATA_FILE, false).getSize();
		return lastInstanceSize;
	}

	public static long getInstanceSize() throws SearchLibException {
		if (lastInstanceSize != 0)
			return lastInstanceSize;
		return calculateInstanceSize();
	}

	public static final LastModifiedAndSize getLastModifiedAndSize(String indexName) throws SearchLibException {
		File file = getIndexDirectory(indexName);
		if (!file.exists())
			return null;
		return new LastModifiedAndSize(file, false);
	}

	public static final Client getClient(String indexName) throws SearchLibException {
		return getClient(getIndexDirectory(indexName));
	}

	public static final void closeIndex(String indexName) throws SearchLibException {
		Client client = null;
		clientsLock.w.lock();
		try {
			File indexDirectory = getIndexDirectory(indexName);
			client = CLIENTS.get(indexDirectory);
			if (client == null)
				return;
			Logging.info("Closing client " + indexName);
			client.close();
			CLIENTS.remove(indexDirectory);
		} finally {
			clientsLock.w.unlock();
		}
		if (client != null)
			PushEvent.eventClientSwitch.publish(client);
	}

	private static final File getIndexDirectory(String indexName) throws SearchLibException {
		if (!isValidIndexName(indexName))
			throw new SearchLibException("The name '" + indexName + "' is not allowed");
		return new File(StartStopListener.OPENSEARCHSERVER_DATA_FILE, indexName);
	}

	public static final Set<ClientCatalogItem> getClientCatalog(User user) throws SearchLibException {
		File[] files =
				StartStopListener.OPENSEARCHSERVER_DATA_FILE.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
		Set<ClientCatalogItem> set = new TreeSet<ClientCatalogItem>();
		if (files == null)
			return null;
		for (File file : files) {
			if (!file.isDirectory())
				continue;
			String indexName = file.getName();
			if (!isValidIndexName(indexName))
				continue;
			if (user == null || user.hasAnyRole(indexName, Role.GROUP_INDEX))
				set.add(new ClientCatalogItem(indexName));
		}
		return set;
	}

	/**
	 * Tests if an index exists
	 *
	 * @param indexName The name of an index
	 * @return true if the index exist
	 * @throws SearchLibException inherited error
	 */
	public static final boolean exists(String indexName) throws SearchLibException {
		return exists(null, indexName);
	}

	public static final boolean exists(User user, String indexName) throws SearchLibException {
		if (user != null && !user.isAdmin())
			throw new SearchLibException("Operation not permitted");
		if (!isValidIndexName(indexName))
			throw new SearchLibException("The name '" + indexName + "' is not allowed");
		return getClientCatalog(null).contains(new ClientCatalogItem(indexName));
	}

	public static synchronized final OcrManager getOcrManager() throws SearchLibException {
		return OcrManager.getInstance();
	}

	public static synchronized final ClusterManager getClusterManager() throws SearchLibException {
		return ClusterManager.getInstance();
	}

	final private static boolean isValidIndexName(String name) {
		if (name.startsWith("."))
			return false;
		if ("logs".equals(name))
			return false;
		return true;
	}

	/**
	 * Create a new index.
	 *
	 * @param indexName    The name of the index.
	 * @param templateName The name of the template (EMPTY_INDEX, WEB_CRAWLER,
	 *                     FILE_CRAWLER)
	 * @param remoteURI    the remote URI
	 * @throws IOException        inherited error
	 * @throws SearchLibException inherited error
	 */
	public static void createIndex(String indexName, String templateName, URI remoteURI)
			throws SearchLibException, IOException {
		TemplateAbstract template = TemplateList.findTemplate(templateName);
		if (template == null)
			throw new SearchLibException("Template not found: " + templateName);
		createIndex(null, indexName, template, remoteURI);
	}

	public static void createIndex(User user, String indexName, TemplateAbstract template, URI remoteURI)
			throws SearchLibException, IOException {
		if (user != null && !user.isAdmin())
			throw new SearchLibException("Operation not permitted");
		ClientFactory.INSTANCE.properties.checkMaxIndexNumber();
		if (!isValidIndexName(indexName))
			throw new SearchLibException("The name '" + indexName + "' is not allowed");
		synchronized (ClientCatalog.class) {
			File indexDir = new File(StartStopListener.OPENSEARCHSERVER_DATA_FILE, indexName);
			if (indexDir.exists())
				throw new SearchLibException("directory " + indexName + " already exists");
			template.createIndex(indexDir, remoteURI);
		}
	}

	/**
	 * Delete an index.
	 *
	 * @param indexName The name of the index
	 * @throws SearchLibException inherited error
	 * @throws NamingException    inherited error
	 * @throws IOException        inherited error
	 */
	public static void eraseIndex(String indexName) throws SearchLibException, NamingException, IOException {
		eraseIndex(null, indexName);
	}

	public static void eraseIndex(User user, String indexName) throws SearchLibException, NamingException, IOException {
		if (user != null && !user.isAdmin())
			throw new SearchLibException("Operation not permitted");
		File indexDir = getIndexDirectory(indexName);
		Client client = null;
		synchronized (ClientCatalog.class) {
			clientsLock.r.lock();
			try {
				client = CLIENTS.get(indexDir);
			} finally {
				clientsLock.r.unlock();
			}
			if (client != null) {
				client.close();
				client.delete();
			} else
				FileUtils.deleteDirectory(indexDir);
			if (client != null) {
				clientsLock.w.lock();
				try {
					CLIENTS.remove(client.getDirectory());
				} finally {
					clientsLock.w.unlock();
				}
				PushEvent.eventClientSwitch.publish(client);
			}
		}
	}

	public static UserList getUserList() throws SearchLibException {
		usersLock.r.lock();
		try {
			if (userList == null) {
				File userFile = new File(StartStopListener.OPENSEARCHSERVER_DATA_FILE, "users.xml");
				if (userFile.exists()) {
					XPathParser xpp = new XPathParser(userFile);
					userList = UserList.fromXml(xpp, xpp.getNode("/" + UserList.usersElement));
				} else
					userList = new UserList();
			}
			return userList;
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} finally {
			usersLock.r.unlock();
		}
	}

	public static void flushPrivileges() {
		usersLock.w.lock();
		try {
			userList = null;
		} finally {
			usersLock.w.unlock();
		}
	}

	private static void saveUserListWithoutLock()
			throws TransformerConfigurationException, SAXException, IOException, SearchLibException {
		ConfigFileRotation cfr = configFiles.get(StartStopListener.OPENSEARCHSERVER_DATA_FILE, "users.xml");
		try {
			XmlWriter xmlWriter = new XmlWriter(cfr.getTempPrintWriter("UTF-8"), "UTF-8");
			getUserList().writeXml(xmlWriter);
			xmlWriter.endDocument();
			cfr.rotate();
		} finally {
			cfr.abort();
		}
	}

	public static void saveUserList() throws SearchLibException {
		usersLock.w.lock();
		try {
			saveUserListWithoutLock();
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (TransformerConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} finally {
			usersLock.w.unlock();
		}
	}

	public static User authenticate(String login, String password) throws SearchLibException {
		usersLock.r.lock();
		try {
			User user = getUserList().get(login);
			if (user == null)
				return null;
			if (!user.authenticate(password))
				return null;
			return user;
		} finally {
			usersLock.r.unlock();
		}
	}

	public static User authenticateKey(String login, String key) throws SearchLibException {
		usersLock.r.lock();
		try {
			User user = getUserList().get(login);
			if (user == null)
				return null;
			if (!user.authenticateKey(key))
				return null;
			return user;
		} finally {
			usersLock.r.unlock();
		}
	}

	private static final File getTempReceiveDir(Client client) {
		File clientDir = client.getDirectory();
		return new File(clientDir.getParentFile(), '.' + clientDir.getName());
	}

	private static final File getTrashReceiveDir(Client client) {
		File clientDir = client.getDirectory();
		return new File(clientDir.getParentFile(), "._" + clientDir.getName());
	}

	public static final void receive_init(Client client) throws IOException, SearchLibException {
		ClientFactory.INSTANCE.properties.checkMaxStorageLimit();
		File rootDir = getTempReceiveDir(client);
		FileUtils.deleteDirectory(rootDir);
		rootDir.mkdir();
	}

	private static void lockClientDir(File clientDir) {
		clientsLock.w.lock();
		try {
			CLIENTS.remove(clientDir);
			OLD_CLIENTS.add(clientDir);
		} finally {
			clientsLock.w.unlock();
		}
	}

	private static void lockClients(List<Client> clients) {
		if (clients == null)
			return;
		for (Client client : clients)
			lockClientDir(client.getDirectory());
	}

	private static void closeClients(List<Client> clients) {
		if (clients == null)
			return;
		for (Client client : clients)
			client.close();
	}

	private static void unlockClientDir(File clientDir, Client newClient) {
		clientsLock.w.lock();
		try {
			if (newClient != null)
				CLIENTS.put(clientDir, newClient);
			OLD_CLIENTS.remove(clientDir);
		} finally {
			clientsLock.w.unlock();
		}
	}

	private static void unlockClients(List<Client> clients) {
		if (clients == null)
			return;
		for (Client client : clients)
			unlockClientDir(client.getDirectory(), null);
	}

	public static void receive_switch(WebApp webapp, Client client)
			throws SearchLibException, NamingException, IOException {
		File trashDir = getTrashReceiveDir(client);
		File clientDir = client.getDirectory();
		if (trashDir.exists())
			FileUtils.deleteDirectory(trashDir);
		Client newClient = null;
		List<Client> clientDepends = findDepends(client.getIndexName());
		lockClientDir(clientDir);
		try {
			lockClients(clientDepends);
			closeClients(clientDepends);
			try {
				client.trash(trashDir);
				getTempReceiveDir(client).renameTo(clientDir);
				File pathToMoveFile = new File(clientDir, PATH_TO_MOVE);
				if (pathToMoveFile.exists()) {
					for (String pathToMove : FileUtils.readLines(pathToMoveFile, "UTF-8")) {
						File from = new File(trashDir, pathToMove);
						File to = new File(clientDir, pathToMove);
						FileUtils.moveFile(from, to);
					}
					if (!pathToMoveFile.delete())
						throw new IOException("Unable to delete the file: " + pathToMoveFile.getAbsolutePath());
				}
				newClient = ClientFactory.INSTANCE.newClient(clientDir, true, true,
						ClientFactory.INSTANCE.properties.getSilentBackupUrl());
				newClient.writeReplCheck();
			} finally {
				unlockClients(clientDepends);
			}
		} finally {
			unlockClientDir(clientDir, newClient);
		}
		PushEvent.eventClientSwitch.publish(client);
		FileUtils.deleteDirectory(trashDir);
	}

	public static void receive_merge(WebApp webapp, Client client) throws SearchLibException, IOException {
		File tempDir = getTempReceiveDir(client);
		File clientDir = client.getDirectory();
		Client newClient = null;
		lockClientDir(clientDir);
		try {
			client.close();
			new ReplicationMerge(tempDir, clientDir);
			newClient = ClientFactory.INSTANCE.newClient(clientDir, true, true,
					ClientFactory.INSTANCE.properties.getSilentBackupUrl());
			newClient.writeReplCheck();
		} finally {
			unlockClientDir(clientDir, newClient);
		}
		PushEvent.eventClientSwitch.publish(client);
		FileUtils.deleteDirectory(tempDir);
	}

	public static void receive_abort(Client client) throws IOException {
		File tempDir = getTempReceiveDir(client);
		File trashDir = getTrashReceiveDir(client);
		synchronized (ClientCatalog.class) {
			if (tempDir.exists())
				FileUtils.deleteDirectory(tempDir);
			if (trashDir.exists())
				FileUtils.deleteDirectory(trashDir);
		}
	}

	public static final void receive_dir(Client client, String filePath) throws IOException {
		File rootDir = getTempReceiveDir(client);
		File targetFile = new File(rootDir, filePath);
		targetFile.mkdir();
	}

	private final static String PATH_TO_MOVE = ".path-to-move";

	public static final boolean receive_file_exists(Client client, String filePath, long lastModified, long length)
			throws IOException {
		File existsFile = new File(client.getDirectory(), filePath);
		if (!existsFile.exists())
			return false;
		if (existsFile.lastModified() != lastModified)
			return false;
		if (existsFile.length() != length)
			return false;
		File rootDir = getTempReceiveDir(client);
		IOUtils.appendLines(new File(rootDir, PATH_TO_MOVE), filePath);
		return true;
	}

	public static final void receive_file(Client client, String filePath, long lastModified, InputStream is)
			throws IOException {
		File rootDir = getTempReceiveDir(client);
		File targetFile = new File(rootDir, filePath);
		targetFile.createNewFile();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(targetFile);
			int len;
			byte[] buffer = new byte[131072];
			while ((len = is.read(buffer)) != -1)
				fos.write(buffer, 0, len);
		} catch (IOException e) {
			throw e;
		} finally {
			IOUtils.close(fos);
		}
		targetFile.setLastModified(lastModified);
	}

	public static int getMaxClauseCount() {
		return BooleanQuery.getMaxClauseCount();
	}

	public static void setMaxClauseCount(int value) {
		BooleanQuery.setMaxClauseCount(value);
	}

	public final static RendererResults getRendererResults() {
		return rendererResults;
	}

	public static ThreadGroup getThreadGroup() {
		return threadGroup;
	}

}
