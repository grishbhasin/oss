/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.result.collector.docsethit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;
import org.roaringbitmap.RoaringBitmap;

import com.jaeksoft.searchlib.result.collector.AbstractBaseCollector;
import com.jaeksoft.searchlib.result.collector.CollectorInterface;

final public class DocSetHitBaseCollector extends
		AbstractBaseCollector<DocSetHitCollectorInterface> implements
		DocSetHitCollectorInterface {

	private final int maxDoc;
	public final LuceneCollector collector;
	private Float score;
	private int size = 0;

	public DocSetHitBaseCollector(final int maxDoc, final boolean isFilterHits) {
		this.maxDoc = maxDoc;
		collector = isFilterHits ? new FilterHitsCollector()
				: new LuceneCollector();
	}

	private DocSetHitBaseCollector(final DocSetHitBaseCollector src) {
		this.maxDoc = src.maxDoc;
		this.size = src.size;
		collector = null;
	}

	@Override
	public CollectorInterface duplicate(AbstractBaseCollector<?> base) {
		return new DocSetHitBaseCollector((DocSetHitBaseCollector) base);
	}

	@Override
	final public void collectDoc(final int doc) throws IOException {
		size++;
	}

	public class LuceneCollector extends Collector {

		protected int currentDocBase = 0;
		private Scorer scorer = null;

		@Override
		final public boolean acceptsDocsOutOfOrder() {
			return true;
		}

		@Override
		public void setNextReader(final IndexReader reader, final int docBase)
				throws IOException {
			currentDocBase = docBase;
		}

		@Override
		public void collect(final int doc) throws IOException {
			score = null;
			lastCollector.collectDoc(doc + currentDocBase);
		}

		@Override
		final public void setScorer(final Scorer scorer) throws IOException {
			this.scorer = scorer;
		}

	}

	final public class FilterHitsCollector extends LuceneCollector {

		final public class Segment {

			public final Integer docBase;
			public final IndexReader indexReader;
			public final RoaringBitmap docBitSet;

			private Segment(int docBase, IndexReader reader) {
				this.docBase = docBase;
				this.indexReader = reader;
				this.docBitSet = new RoaringBitmap();
			}
		}

		final public List<Segment> segments;

		private Segment currentSegment;

		private FilterHitsCollector() {
			segments = new ArrayList<Segment>();
		}

		@Override
		final public void setNextReader(final IndexReader reader,
				final int docBase) throws IOException {
			currentDocBase = docBase;
			currentSegment = new Segment(docBase, reader);
			segments.add(currentSegment);
		}

		@Override
		public void collect(final int doc) throws IOException {
			score = null;
			lastCollector.collectDoc(doc + currentDocBase);
			currentSegment.docBitSet.add(doc);
		}

	}

	final public float score() throws IOException {
		if (score == null)
			score = collector.scorer.score();
		return score;
	}

	@Override
	final public void endCollection() {
	}

	@Override
	final public int getSize() {
		return size;
	}

	final public int getMaxDoc() {
		return maxDoc;
	}

	@Override
	final public void doSwap(final int a, final int b) {
	}

}
