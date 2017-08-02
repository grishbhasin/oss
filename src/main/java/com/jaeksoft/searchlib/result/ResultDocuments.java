/**
 * License Agreement for OpenSearchServer
 * <p>
 * Copyright (C) 2012-2015 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.result;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.function.expression.SyntaxError;
import com.jaeksoft.searchlib.index.ReaderInterface;
import com.jaeksoft.searchlib.index.ReaderLocal;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.render.Render;
import com.jaeksoft.searchlib.render.RenderDocumentsJson;
import com.jaeksoft.searchlib.render.RenderDocumentsXml;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.request.DocumentsRequest;
import com.jaeksoft.searchlib.request.RequestInterfaces;
import com.jaeksoft.searchlib.result.collector.DocIdInterface;
import com.jaeksoft.searchlib.schema.*;
import com.jaeksoft.searchlib.util.IOUtils;
import com.jaeksoft.searchlib.util.Timer;
import com.jaeksoft.searchlib.util.array.IntBufferedArrayFactory;
import com.jaeksoft.searchlib.util.array.IntBufferedArrayInterface;
import com.jaeksoft.searchlib.webservice.query.document.IndexDocumentResult;
import com.jaeksoft.searchlib.webservice.query.document.IndexDocumentResult.IndexField;
import com.jaeksoft.searchlib.webservice.query.document.IndexDocumentResult.IndexTerm;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermFreqVector;
import org.roaringbitmap.IntIterator;
import org.roaringbitmap.RoaringBitmap;

import java.io.IOException;
import java.util.*;

public class ResultDocuments extends AbstractResult<AbstractRequest>
		implements ResultDocumentsInterface<AbstractRequest> {

	transient private ReaderInterface reader = null;
	final private LinkedHashSet<String> fieldNameSet;
	final private int[] docArray;

	private ResultDocuments(ReaderInterface reader, AbstractRequest request, LinkedHashSet<String> fieldNameSet,
			int[] docArray) {
		super(request);
		this.reader = reader;
		this.fieldNameSet = fieldNameSet == null ? new LinkedHashSet<String>() : fieldNameSet;
		if (this.fieldNameSet.size() == 0 && request instanceof RequestInterfaces.ReturnedFieldInterface)
			((RequestInterfaces.ReturnedFieldInterface) request).getReturnFieldList().populate(this.fieldNameSet);
		this.docArray = docArray;
	}

	public ResultDocuments(ReaderInterface reader, AbstractRequest request, LinkedHashSet<String> fieldNameSet,
			List<Integer> docList) {
		this(reader, request, fieldNameSet, toDocArray(docList));
	}

	private final static int[] toDocArray(List<Integer> docList) {
		if (CollectionUtils.isEmpty(docList))
			return null;
		int[] docArray = new int[docList.size()];
		int i = 0;
		for (Integer docId : docList)
			docArray[i++] = docId;
		return docArray;
	}

	public ResultDocuments(ReaderLocal reader, DocumentsRequest request) throws IOException, SearchLibException {
		this(reader, request, null, toDocArray(reader, request));
	}

	private final static int[] toDocArray(ReaderLocal reader, DocumentsRequest request) throws IOException {
		SchemaField schemaField = null;
		Schema schema = request.getConfig().getSchema();
		String field = request.getField();
		if (!StringUtils.isEmpty(field)) {
			schemaField = schema.getField(field);
			if (schemaField == null)
				throw new IOException("Field not found: " + field);
		} else {
			schemaField = schema.getFieldList().getUniqueField();
			if (schemaField == null)
				throw new IOException("No unique field");
		}
		Collection<String> uniqueKeys = request.getUniqueKeyList();
		String fieldName = schemaField.getName();
		return request.isReverse() ?
				reverseDoc(reader, fieldName, uniqueKeys) :
				sortedDoc(reader, fieldName, uniqueKeys);
	}

	private final static int[] sortedDoc(ReaderLocal reader, String fieldName, Collection<String> uniqueKeys)
			throws IOException {
		int[] docIDs = new int[uniqueKeys.size()];
		int i = 0;
		for (String uniqueKey : uniqueKeys) {
			TermDocs termDocs = reader.getTermDocs(new Term(fieldName, uniqueKey));
			if (termDocs != null) {
				try {
					while (termDocs.next()) {
						int doc = termDocs.doc();
						if (!reader.isDeletedNoLock(doc))
							docIDs[i++] = doc;
					}
				} finally {
					IOUtils.close(termDocs);
				}
			}
		}
		if (i == docIDs.length)
			return docIDs;
		return Arrays.copyOf(docIDs, i);
	}

	private final static int[] reverseDoc(ReaderLocal reader, String fieldName, Collection<String> uniqueKeys)
			throws IOException {
		int higher = -1;
		RoaringBitmap bitSet = new RoaringBitmap();
		for (String uniqueKey : uniqueKeys) {
			TermDocs termDocs = reader.getTermDocs(new Term(fieldName, uniqueKey));
			if (termDocs != null) {
				try {
					while (termDocs.next()) {
						int doc = termDocs.doc();
						if (doc > higher)
							higher = doc;
						bitSet.add(doc);
					}
				} finally {
					IOUtils.close(termDocs);
				}
			}
		}
		bitSet.flip(0L, higher + 1);
		IntBufferedArrayInterface intBufferArray =
				IntBufferedArrayFactory.INSTANCE.newInstance(bitSet.getCardinality());
		IntIterator iterator = bitSet.getIntIterator();
		while (iterator.hasNext()) {
			int docId = iterator.next();
			if (!reader.isDeletedNoLock(docId))
				intBufferArray.add(docId);
		}
		return intBufferArray.getFinalArray();
	}

	@Override
	public ResultDocument getDocument(int pos, Timer timer) throws SearchLibException {
		if (docArray == null || pos < 0 || pos > docArray.length)
			return null;
		try {
			return new ResultDocument(fieldNameSet, docArray[pos], reader, getScore(pos), null, timer);
		} catch (IOException e) {
			throw new SearchLibException(e);
		} catch (ParseException e) {
			throw new SearchLibException(e);
		} catch (SyntaxError e) {
			throw new SearchLibException(e);
		}
	}

	@Override
	public void populate(List<IndexDocumentResult> indexDocuments) throws IOException, SearchLibException {
		SchemaFieldList schemaFieldList = request.getConfig().getSchema().getFieldList();
		for (int docId : docArray) {
			IndexDocumentResult indexDocument = new IndexDocumentResult(schemaFieldList.size());
			Map<String, FieldValue> storedFieldMap = reader.getDocumentStoredField(docId);
			for (SchemaField schemaField : schemaFieldList) {
				String fieldName = schemaField.getName();
				List<IndexTerm> indexTermList = null;
				if (schemaField.checkIndexed(Indexed.YES)) {
					if (schemaField.getTermVector() == TermVector.NO) {
						indexTermList = IndexTerm.toList(reader, fieldName, docId);
					} else {
						TermFreqVector termFreqVector = reader.getTermFreqVector(docId, fieldName);
						indexTermList = IndexTerm.toList(termFreqVector);
					}
				}
				IndexField indexField = new IndexField(fieldName, storedFieldMap.get(fieldName), indexTermList);
				indexDocument.add(indexField);
			}
			indexDocuments.add(indexDocument);
		}
	}

	@Override
	public float getScore(int pos) {
		return 0;
	}

	@Override
	public Float getDistance(int pos) {
		return null;
	}

	@Override
	public int getCollapseCount(int pos) {
		return 0;
	}

	@Override
	public int getNumFound() {
		if (docArray == null)
			return 0;
		return docArray.length;
	}

	@Override
	protected Render getRenderXml() {
		return new RenderDocumentsXml(this);
	}

	@Override
	protected Render getRenderCsv() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Render getRenderJson(boolean indent) {
		return new RenderDocumentsJson(this, indent);
	}

	@Override
	public Iterator<ResultDocument> iterator() {
		return new ResultDocumentIterator(this, null);
	}

	@Override
	public int getDocumentCount() {
		return docArray == null ? 0 : docArray.length;
	}

	@Override
	public int getRequestStart() {
		return 0;
	}

	@Override
	public int getRequestRows() {
		return docArray == null ? 0 : docArray.length;
	}

	@Override
	public DocIdInterface getDocs() {
		return null;
	}

	public int[] getDocIdArray() {
		return docArray;
	}

	@Override
	public float getMaxScore() {
		return 0;
	}

	@Override
	public int getCollapsedDocCount() {
		return 0;
	}

}
