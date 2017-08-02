/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.statistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class StatisticsList {

	private List<StatisticsAbstract> searchList;
	private List<StatisticsAbstract> updateList;
	private List<StatisticsAbstract> deleteList;
	private List<StatisticsAbstract> reloadList;
	private List<StatisticsAbstract> optimizeList;

	private StatisticsList() {
		searchList = null;
		updateList = null;
		deleteList = null;
		reloadList = null;
		optimizeList = null;
	}

	private List<StatisticsAbstract> addToList(List<StatisticsAbstract> list,
			StatisticsAbstract stat) {
		if (list == null)
			list = new ArrayList<StatisticsAbstract>();
		list.add(stat);
		return list;
	}

	private void add(StatisticsAbstract stat) {
		StatisticTypeEnum type = stat.getType();
		if (type == StatisticTypeEnum.SEARCH)
			searchList = addToList(searchList, stat);
		else if (type == StatisticTypeEnum.UPDATE)
			updateList = addToList(updateList, stat);
		else if (type == StatisticTypeEnum.DELETE)
			deleteList = addToList(deleteList, stat);
		else if (type == StatisticTypeEnum.RELOAD)
			reloadList = addToList(reloadList, stat);
		else if (type == StatisticTypeEnum.OPTIMIZE)
			optimizeList = addToList(optimizeList, stat);
	}

	public void addSearch(Timer timer) {
		if (searchList == null)
			return;
		for (StatisticsAbstract stat : searchList)
			stat.add(timer);
	}

	public void addUpdate(Timer timer) {
		if (updateList == null)
			return;
		for (StatisticsAbstract stat : updateList)
			stat.add(timer);
	}

	public void addDelete(Timer timer) {
		if (deleteList == null)
			return;
		for (StatisticsAbstract stat : deleteList)
			stat.add(timer);
	}

	public void addReload(Timer timer) {
		if (reloadList == null)
			return;
		for (StatisticsAbstract stat : reloadList)
			stat.add(timer);
	}

	public void addOptimize(Timer timer) {
		if (optimizeList == null)
			return;
		for (StatisticsAbstract stat : optimizeList)
			stat.add(timer);
	}

	public List<StatisticsAbstract> getStatList(StatisticTypeEnum type) {
		if (type == StatisticTypeEnum.SEARCH)
			return searchList;
		else if (type == StatisticTypeEnum.UPDATE)
			return updateList;
		else if (type == StatisticTypeEnum.DELETE)
			return deleteList;
		else if (type == StatisticTypeEnum.RELOAD)
			return reloadList;
		else if (type == StatisticTypeEnum.OPTIMIZE)
			return optimizeList;
		else
			return null;
	}

	public StatisticsAbstract getStat(StatisticTypeEnum type,
			StatisticPeriodEnum period) {
		List<StatisticsAbstract> stats = getStatList(type);
		if (stats == null)
			return null;
		for (StatisticsAbstract stat : stats)
			if (stat.getPeriod() == period)
				return stat;
		return null;
	}

	public static StatisticsList fromXmlConfig(XPathParser xpp,
			Node parentNode, File statDir) throws XPathExpressionException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, DOMException, IOException {
		StatisticsList stats = new StatisticsList();
		if (parentNode == null)
			return stats;
		NodeList nodes = xpp.getNodeList(parentNode, "statistic");
		if (nodes == null)
			return stats;
		for (int i = 0; i < nodes.getLength(); i++)
			stats.add(StatisticsAbstract.fromXmlConfig(xpp, nodes.item(i),
					statDir));
		return stats;
	}

	public void writeXmlConfig(XmlWriter writer) throws SAXException {
		writer.startElement("statistics");
		if (searchList != null)
			for (StatisticsAbstract stat : searchList)
				stat.writeXmlConfig(writer);
		if (updateList != null)
			for (StatisticsAbstract stat : updateList)
				stat.writeXmlConfig(writer);
		if (deleteList != null)
			for (StatisticsAbstract stat : deleteList)
				stat.writeXmlConfig(writer);
		if (optimizeList != null)
			for (StatisticsAbstract stat : optimizeList)
				stat.writeXmlConfig(writer);
		if (reloadList != null)
			for (StatisticsAbstract stat : reloadList)
				stat.writeXmlConfig(writer);
		writer.endElement();
	}

	private void writeStatList(File statDir, List<StatisticsAbstract> list)
			throws IOException {
		if (list == null)
			return;
		for (StatisticsAbstract stat : list)
			stat.save(statDir);
	}

	public void save(File statDir) throws IOException {
		if (!statDir.exists())
			statDir.mkdir();
		writeStatList(statDir, searchList);
		writeStatList(statDir, updateList);
		writeStatList(statDir, deleteList);
		writeStatList(statDir, optimizeList);
		writeStatList(statDir, reloadList);
	}
}
