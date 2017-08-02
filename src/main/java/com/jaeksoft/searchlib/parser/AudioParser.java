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

package com.jaeksoft.searchlib.parser;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.ClassPropertyEnum;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.streamlimiter.StreamLimiter;

public class AudioParser extends Parser {

	public static final String[] DEFAULT_MIMETYPES = { "audio/ogg",
			"audio/mpeg", "audio/mpeg3", "audio/flac", "audio/mp4",
			"audio/vnd.rn-realaudio", "audio/x-pn-realaudio",
			"audio/x-realaudio", "audio/wav", "audio/x-wav" };

	public static final String[] DEFAULT_EXTENSIONS = { "ogg", "mp3", "flac",
			"mp4", "m4a", "m4p", "wma", "wav", "ra", "rm", "m4b" };

	private static ParserFieldEnum[] fl = { ParserFieldEnum.parser_name,
			ParserFieldEnum.artist, ParserFieldEnum.album,
			ParserFieldEnum.title, ParserFieldEnum.track, ParserFieldEnum.year,
			ParserFieldEnum.genre, ParserFieldEnum.comment,
			ParserFieldEnum.album_artist, ParserFieldEnum.composer,
			ParserFieldEnum.grouping };

	public AudioParser() {
		super(fl);
		AudioFileIO.logger.setLevel(Level.OFF);
	}

	@Override
	public void initProperties() throws SearchLibException {
		super.initProperties();
		addProperty(ClassPropertyEnum.SIZE_LIMIT, "0", null, 20, 1);
	}

	private void addFields(ParserResultItem result, Tag tag, FieldKey fieldKey,
			ParserFieldEnum parserField) {
		List<TagField> list = tag.getFields(fieldKey);
		if (list != null && list.size() > 0) {
			for (TagField field : list)
				result.addField(parserField, field);
			return;
		}
		String f = tag.getFirst(fieldKey);
		if (f == null)
			return;
		f = f.trim();
		if (f.length() == 0)
			return;
		result.addField(parserField, f);
	}

	@Override
	protected void parseContent(StreamLimiter streamLimiter, LanguageEnum lang)
			throws IOException {
		AudioFile f;
		try {
			f = AudioFileIO.read(streamLimiter.getFile());
		} catch (CannotReadException e) {
			throw new IOException(e);
		} catch (TagException e) {
			throw new IOException(e);
		} catch (ReadOnlyFileException e) {
			throw new IOException(e);
		} catch (InvalidAudioFrameException e) {
			throw new IOException(e);
		} catch (SearchLibException e) {
			throw new IOException(e);
		}
		Tag tag = f.getTag();
		if (tag == null)
			return;
		ParserResultItem result = getNewParserResultItem();
		addFields(result, tag, FieldKey.TITLE, ParserFieldEnum.title);
		addFields(result, tag, FieldKey.ARTIST, ParserFieldEnum.artist);
		addFields(result, tag, FieldKey.ALBUM, ParserFieldEnum.album);
		addFields(result, tag, FieldKey.YEAR, ParserFieldEnum.year);
		addFields(result, tag, FieldKey.TRACK, ParserFieldEnum.track);
		addFields(result, tag, FieldKey.ALBUM_ARTIST,
				ParserFieldEnum.album_artist);
		addFields(result, tag, FieldKey.COMMENT, ParserFieldEnum.comment);
		addFields(result, tag, FieldKey.COMPOSER, ParserFieldEnum.composer);
		addFields(result, tag, FieldKey.GROUPING, ParserFieldEnum.grouping);
	}
}
