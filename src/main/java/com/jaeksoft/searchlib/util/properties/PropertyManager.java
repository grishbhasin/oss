/**
 * License Agreement for OpenSearchServer
 * <p/>
 * Copyright (C) 2012-2016 Emmanuel Keller / Jaeksoft
 * <p/>
 * http://www.open-search-server.com
 * <p/>
 * This file is part of OpenSearchServer.
 * <p/>
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with OpenSearchServer.
 * If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.util.properties;

import com.jaeksoft.searchlib.SearchLibException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertyManager {

	private final HashMap<String, PropertyItem<?>> propertyMap;

	private final File propFile;

	private final Properties properties;

	public PropertyManager(File file) throws IOException {
		propFile = file;
		propertyMap = new HashMap<>();
		properties = new Properties();
		if (propFile.exists()) {
			FileInputStream inputStream = null;
			try {
				inputStream = new FileInputStream(propFile);
				properties.loadFromXML(inputStream);
			} catch (IOException e) {
				throw e;
			} finally {
				if (inputStream != null)
					inputStream.close();
			}
		}
	}

	void add(PropertyItem<?> propertyItem) {
		propertyMap.put(propertyItem.getName(), propertyItem);
	}

	public void save() throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(propFile);
			properties.storeToXML(fos, "");
		} catch (IOException e) {
			throw e;
		} finally {
			if (fos != null)
				fos.close();
		}
	}

	public PropertyItem<Integer> newIntegerProperty(String name, Integer defaultValue, Integer min, Integer max)
			throws NumberFormatException, IOException {
		PropertyItem<Integer> propertyItem = new PropertyItem<Integer>(this, name, defaultValue, min, max);
		String value = properties.getProperty(name);
		if (value != null)
			propertyItem.initValue(Integer.parseInt(value));
		return propertyItem;
	}

	public PropertyItem<Boolean> newBooleanProperty(String name, Boolean defaultValue) {
		PropertyItem<Boolean> propertyItem = new PropertyItem<Boolean>(this, name, defaultValue, null, null);
		String value = properties.getProperty(name);
		if (value != null)
			propertyItem
					.initValue("1".equals(value) || "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value));
		return propertyItem;
	}

	public PropertyItem<String> newStringProperty(String name, String defaultValue) {
		PropertyItem<String> propertyItem = new PropertyItem<String>(this, name, defaultValue, null, null);
		String value = properties.getProperty(name);
		if (value != null)
			propertyItem.initValue(value);
		return propertyItem;
	}

	public void put(PropertyItem<?> propertyItem) throws IOException {
		propertyItem.put(properties);
		save();
	}

	public void fillProperties(Map<String, Comparable> map) {
		for (PropertyItem<?> item : propertyMap.values())
			map.put(item.getName(), item.getValue());
	}

	public void setProperty(String name,  Comparable value) throws IOException, SearchLibException {
		PropertyItem<?> item = propertyMap.get(name);
		if (item != null)
			item.setValueObject(value);
	}

}
