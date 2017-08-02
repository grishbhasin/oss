/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.ocr;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.util.ExecuteUtils;
import com.jaeksoft.searchlib.util.FileUtils;
import com.jaeksoft.searchlib.util.ImageUtils;
import com.jaeksoft.searchlib.util.PropertiesUtils;
import com.jaeksoft.searchlib.util.ReadWriteLock;
import com.jaeksoft.searchlib.web.StartStopListener;

public class OcrManager implements Closeable {

	private final static String OCR_PROPERTY_FILE = "ocr.xml";

	private final static String OCR_PROPERTY_ENABLED = "enabled";

	private final static String OCR_PROPERTY_DEFAULT_LANGUAGE = "defaultLanguage";

	private final static String OCR_PROPERTY_TESSERACT_PATH = "tesseractPath";

	private final static String OCR_PROPERTY_HOCR_FILE_EXTENSION = "hocrFileExt";

	private final ReadWriteLock rwl = new ReadWriteLock();

	private boolean enabled = false;

	private String tesseractPath = null;

	private String hocrFileExtension = "hocr";

	private TesseractLanguageEnum defaultLanguage;

	private File propFile;

	private final Semaphore tesseractSemaphore;

	private OcrManager(File dataDir)
			throws InvalidPropertiesFormatException, IOException, InstantiationException, IllegalAccessException {
		propFile = new File(dataDir, OCR_PROPERTY_FILE);
		Properties properties = PropertiesUtils.loadFromXml(propFile);
		enabled = "true".equalsIgnoreCase(properties.getProperty(OCR_PROPERTY_ENABLED, "false"));
		defaultLanguage = TesseractLanguageEnum
				.find(properties.getProperty(OCR_PROPERTY_DEFAULT_LANGUAGE, TesseractLanguageEnum.None.name()));
		tesseractPath = properties.getProperty(OCR_PROPERTY_TESSERACT_PATH);
		hocrFileExtension = properties.getProperty(OCR_PROPERTY_HOCR_FILE_EXTENSION, "hocr");
		setEnabled(enabled);
		tesseractSemaphore = new Semaphore(Runtime.getRuntime().availableProcessors() / 2 + 1);
	}

	private static OcrManager INSTANCE = null;
	final private static ReadWriteLock rwlInstance = new ReadWriteLock();

	public static final OcrManager getInstance() throws SearchLibException {
		rwlInstance.r.lock();
		try {
			if (INSTANCE != null)
				return INSTANCE;
		} finally {
			rwlInstance.r.unlock();
		}
		rwlInstance.w.lock();
		try {
			if (INSTANCE != null)
				return INSTANCE;
			return INSTANCE = new OcrManager(StartStopListener.OPENSEARCHSERVER_DATA_FILE);
		} catch (InvalidPropertiesFormatException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InstantiationException e) {
			throw new SearchLibException(e);
		} catch (IllegalAccessException e) {
			throw new SearchLibException(e);
		} finally {
			rwlInstance.w.unlock();
		}
	}

	private void save() throws IOException {
		Properties properties = new Properties();
		properties.setProperty(OCR_PROPERTY_ENABLED, Boolean.toString(enabled));
		if (tesseractPath != null)
			properties.setProperty(OCR_PROPERTY_TESSERACT_PATH, tesseractPath);
		if (defaultLanguage != null)
			properties.setProperty(OCR_PROPERTY_DEFAULT_LANGUAGE, defaultLanguage.name());
		if (hocrFileExtension != null)
			properties.setProperty(OCR_PROPERTY_HOCR_FILE_EXTENSION, hocrFileExtension);
		PropertiesUtils.storeToXml(properties, propFile);
	}

	@Override
	public void close() {
		rwl.w.lock();
		try {
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		rwl.r.lock();
		try {
			return enabled;
		} finally {
			rwl.r.unlock();
		}
	}

	public boolean isDisabled() {
		return !isEnabled();
	}

	/**
	 * @param enabled
	 *            the enabled to set
	 * @throws IOException
	 */
	public void setEnabled(boolean enabled) throws IOException {
		rwl.w.lock();
		try {
			this.enabled = enabled;
			save();
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the tesseractPath
	 */
	public String getTesseractPath() {
		rwl.r.lock();
		try {
			return tesseractPath;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param tesseractPath
	 *            the tesseractPath to set
	 * @throws IOException
	 */
	public void setTesseractPath(String tesseractPath) throws IOException {
		rwl.w.lock();
		try {
			this.tesseractPath = tesseractPath;
			save();
		} finally {
			rwl.w.unlock();
		}
	}

	private final static Pattern tesseractCheckPattern = Pattern.compile("Usage:.*tesseract.* imagename.* outputbase",
			Pattern.DOTALL);

	public void checkTesseract() throws SearchLibException {
		rwl.r.lock();
		try {
			if (tesseractPath == null || tesseractPath.length() == 0)
				throw new SearchLibException("Please enter a path");
			File file = new File(tesseractPath);
			if (!file.exists())
				throw new SearchLibException("The file don't exist");
			List<String> args = new ArrayList<String>();
			args.add(tesseractPath);
			StringBuilder sbResult = new StringBuilder();
			ExecuteUtils.run(args, 60, sbResult, 1);
			String result = sbResult.toString();
			if (!tesseractCheckPattern.matcher(result).find())
				throw new SearchLibException("Wrong returned message: " + result);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (InterruptedException e) {
			throw new SearchLibException(e);
		} finally {
			rwl.r.unlock();
		}
	}

	private String checkOutputPath(File outputFile, boolean hocr) throws SearchLibException {
		String outputPath = outputFile.getAbsolutePath();
		if (hocr) {
			if (!outputPath.endsWith(".html") && !outputPath.endsWith(".hocr"))
				throw new SearchLibException("Output file must ends with .txt, .html or .hocr (" + outputPath + ")");
			outputPath = outputPath.substring(0, outputPath.length() - 5);
		} else {
			if (!outputPath.endsWith(".txt"))
				throw new SearchLibException("Output file must ends with .txt, .html or .hocr (" + outputPath + ")");
			outputPath = outputPath.substring(0, outputPath.length() - 4);
		}
		return outputPath;
	}

	public void ocerize(File input, File outputFile, LanguageEnum lang, boolean hocr)
			throws SearchLibException, IOException, InterruptedException {
		tesseractSemaphore.acquire();
		try {
			rwl.r.lock();
			try {
				if (!enabled)
					return;
				if (tesseractPath == null || tesseractPath.length() == 0)
					throw new SearchLibException("No path for the OCR");
				List<String> args = new ArrayList<String>();
				args.add(tesseractPath);
				args.add(input.getAbsolutePath());
				args.add(checkOutputPath(outputFile, hocr));
				args.add("-psm 1");
				TesseractLanguageEnum tle = TesseractLanguageEnum.find(lang);
				if (tle == null)
					tle = defaultLanguage;
				if (tle != null && tle != TesseractLanguageEnum.None)
					args.add("-l " + tle.option);
				if (hocr)
					args.add("hocr");
				int ev = ExecuteUtils.run(args, 3600, null, null);
				if (ev == 3)
					Logging.warn("Image format not supported by Tesseract (" + input.getName() + ")");
			} finally {
				rwl.r.unlock();
			}
		} finally {
			tesseractSemaphore.release();
		}
	}

	private final static String OCR_IMAGE_FORMAT = "jpg";

	public void ocerizeImage(Image image, File outputFile, LanguageEnum lang, boolean hocr)
			throws InterruptedException, IOException, SearchLibException {
		File imageFile = null;
		try {
			RenderedImage renderedImage = ImageUtils.toBufferedImage(image);
			imageFile = File.createTempFile("ossocrimg", '.' + OCR_IMAGE_FORMAT);
			ImageIO.write(renderedImage, OCR_IMAGE_FORMAT, imageFile);
			image.flush();
			if (imageFile.length() == 0)
				throw new SearchLibException("Empty image " + imageFile.getAbsolutePath());
			ocerize(imageFile, outputFile, lang, hocr);
		} finally {
			Logging.debug(imageFile);
			if (imageFile != null)
				FileUtils.deleteQuietly(imageFile);
		}
	}

	/**
	 * @return the defaultLanguage
	 */
	public TesseractLanguageEnum getDefaultLanguage() {
		rwl.r.lock();
		try {
			return defaultLanguage;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param defaultLanguage
	 *            the defaultLanguage to set
	 * @throws IOException
	 */
	public void setDefaultLanguage(TesseractLanguageEnum defaultLanguage) throws IOException {
		rwl.w.lock();
		try {
			this.defaultLanguage = defaultLanguage;
			save();
		} finally {
			rwl.w.unlock();
		}
	}

	/**
	 * @return the hocrFileExtension
	 */
	public String getHocrFileExtension() {
		rwl.r.lock();
		try {
			return hocrFileExtension;
		} finally {
			rwl.r.unlock();
		}
	}

	/**
	 * @param hocrFileExtension
	 *            the hocrFileExtension to set
	 */
	public void setHocrFileExtension(String hocrFileExtension) {
		rwl.w.lock();
		try {
			this.hocrFileExtension = hocrFileExtension;
		} finally {
			rwl.w.unlock();
		}
	}

}
