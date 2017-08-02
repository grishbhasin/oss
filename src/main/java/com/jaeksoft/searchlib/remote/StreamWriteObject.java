/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.remote;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.jaeksoft.searchlib.Logging;

public class StreamWriteObject {

	// private GZIPOutputStream gos;
	private ObjectOutputStream oos;

	public StreamWriteObject(OutputStream os) throws IOException {
		// gos = new GZIPOutputStream(os);
		oos = new ObjectOutputStream(os);
	}

	public void write(Object object) throws IOException {
		oos.writeObject(object);
	}

	public void close(boolean bFlush) {
		if (oos != null) {
			try {
				if (bFlush)
					oos.flush();
				oos.close();
			} catch (IOException e) {
				Logging.warn(e.getMessage(), e);
			}
			oos = null;
		}
	}

}
