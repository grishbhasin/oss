/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2010-2017 Emmanuel Keller / Jaeksoft
 * <p>
 * http://www.open-search-server.com
 * <p>
 * This file is part of OpenSearchServer.
 * <p>
 * OpenSearchServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Contributor: Richard Sinelle
 **/

package com.jaeksoft.searchlib.web.controller.crawler.web;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.sitemap.SiteMapCache;
import com.jaeksoft.searchlib.crawler.web.sitemap.SiteMapItem;
import com.jaeksoft.searchlib.crawler.web.sitemap.SiteMapList;
import com.jaeksoft.searchlib.crawler.web.sitemap.SiteMapUrl;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.web.controller.AlertController;
import com.jaeksoft.searchlib.web.controller.crawler.CrawlerController;
import org.xml.sax.SAXException;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zul.Messagebox;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;

@AfterCompose(superclass = true)
public class SiteMapController extends CrawlerController {

	private class DeleteAlert extends AlertController {

		private transient SiteMapItem siteMapItem;

		protected DeleteAlert(SiteMapItem siteMapItem) throws InterruptedException {
			super("Please, confirm that you want to delete the Site Map URL: " + siteMapItem.getUri(),
					Messagebox.YES | Messagebox.NO, Messagebox.QUESTION);
			this.siteMapItem = siteMapItem;
		}

		@Override
		protected void onYes() throws SearchLibException {
			getSiteMapList().remove(siteMapItem);
			getClient().saveSiteMapList();
			onCancel();
		}
	}

	private transient SiteMapItem selectedSiteMap;

	private transient SiteMapItem currentSiteMap;

	public SiteMapController() throws SearchLibException {
		super();
	}

	@Override
	protected void reset() throws SearchLibException {
		selectedSiteMap = null;
		currentSiteMap = new SiteMapItem();
	}

	public SiteMapList getSiteMapList() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return client.getSiteMapList();
	}

	/**
	 * @param selectedSiteMap the selectedFilter to set
	 * @throws SearchLibException
	 */
	public void setSelectedSiteMap(SiteMapItem selectedSiteMap) throws SearchLibException {
		this.selectedSiteMap = selectedSiteMap;
		selectedSiteMap.copyTo(currentSiteMap);
		reload();
	}

	/**
	 * @return the selectedFilter
	 */
	public SiteMapItem getSelectedSiteMap() {
		return selectedSiteMap;
	}

	public boolean isSelected() {
		return selectedSiteMap != null;
	}

	public boolean isNotSelected() {
		return !isSelected();
	}

	/**
	 * @return the currentFilter
	 */
	public SiteMapItem getCurrentSiteMap() {
		return currentSiteMap;
	}

	@Command
	public void onSave() throws InterruptedException, SearchLibException {
		SiteMapList siteMapList = getSiteMapList();
		if (siteMapList == null)
			return;
		if (selectedSiteMap != null)
			siteMapList.remove(selectedSiteMap);
		siteMapList.add(currentSiteMap);
		getClient().saveSiteMapList();
		onCancel();
	}

	@Command
	public void onCancel() throws SearchLibException {
		currentSiteMap = new SiteMapItem();
		selectedSiteMap = null;
		reload();
	}

	@Command
	public void onDelete(@BindingParam("siteMapItem") SiteMapItem item)
			throws SearchLibException, InterruptedException {
		new DeleteAlert(item);
	}

	@Command
	public void onCheck(@BindingParam("siteMapItem") SiteMapItem item)
			throws SearchLibException, IllegalStateException, URISyntaxException, IOException, SAXException,
			ParserConfigurationException, InterruptedException {
		final Client client = getClient();
		if (client == null)
			return;
		final HttpDownloader httpDownloader = client.getWebCrawlMaster().getNewHttpDownloader(true);
		try {
			final LinkedHashSet<SiteMapUrl> set = new LinkedHashSet<>();
			item.fill(SiteMapCache.getInstance(), client.getWebCrawlMaster().getNewHttpDownloader(true), true, set);
			new AlertController(set.size() + " URL(s) found");
		} finally {
			if (httpDownloader != null)
				httpDownloader.release();
		}
	}
}
