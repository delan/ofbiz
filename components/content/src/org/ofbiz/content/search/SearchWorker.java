/*
 * $Id: SearchWorker.java,v 1.3 2004/05/14 21:39:19 byersa Exp $
 *
 *  Copyright (c) 2001, 2002 The Open For Business Project - www.ofbiz.org
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 *  THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ofbiz.content.search;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.FileNotFoundException;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.UtilMisc;

import org.apache.lucene.document.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.*;

import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.base.util.Debug;



/**
 * SearchWorker Class
 * 
 * @author <a href="mailto:byersa@automationgroups.com">Al Byers</a> Hacked from Lucene demo file
 * @version $Revision: 1.3 $
 * @since 3.1
 * 
 *  
 */
public class SearchWorker {
	public static final String module = SearchWorker.class.getName();
	
	public static void indexContentList(List idList, GenericDelegator delegator, Map context) throws Exception {
		String path = null;
		indexContentList(delegator, context, idList, path);
	}
	
	public static void indexContentList(GenericDelegator delegator, Map context, List idList, String path) throws Exception {
		String indexAllPath = getIndexPath(path);
		GenericValue content = null;
		
		// Delete existing documents
		IndexReader reader = IndexReader.open(indexAllPath);
		if (Debug.infoOn()) Debug.logInfo("in indexContent, reader:" + reader, module);
		List contentList = new ArrayList();
		Iterator iter = idList.iterator();
		while (iter.hasNext()) {
			String id = (String)iter.next();
		  	if (Debug.infoOn()) Debug.logInfo("in indexContent, id:" + id, module);
			try {
		  		content = delegator.findByPrimaryKeyCache("Content", UtilMisc.toMap("contentId",id));
		  		contentList.add(content);
		  	} catch(GenericEntityException e) {
		  		Debug.logError(e, module);
		  		return;
		  	}
			deleteContentDocument(content, reader);
		}
		reader.close();
		
		// Now create
	  	IndexWriter writer =  null;
	  	try {
	  	    writer =  new IndexWriter(indexAllPath, new StandardAnalyzer(), false);
	    } catch(FileNotFoundException e) {
		    writer = new IndexWriter(indexAllPath, new StandardAnalyzer(), true);
		}
	  	if (Debug.infoOn()) Debug.logInfo("in indexContent, writer:" + writer, module);
		
		iter = contentList.iterator();
		while (iter.hasNext()) {
			content = (GenericValue)iter.next();
			indexContent(delegator, context, content, writer);
		}
		writer.optimize();
		writer.close();
    }
	
	
	public static void deleteContentDocument(GenericValue content, String path) throws Exception {
	    String indexAllPath = null;
	    indexAllPath = getIndexPath(path);
	    IndexReader reader = IndexReader.open(indexAllPath);
        deleteContentDocument(content, reader);
        reader.close();
	}
	
	public static void deleteContentDocument(GenericValue content, IndexReader reader) throws Exception {
		String contentId = content.getString("contentId");
		Term term = new Term("contentId", contentId);
	    if (Debug.infoOn()) Debug.logInfo("in indexContent, term:" + term, module);
		int qtyDeleted = reader.delete(term);
		if (Debug.infoOn()) Debug.logInfo("in indexContent, qtyDeleted:" + term, module);
		String dataResourceId = content.getString("dataResourceId");
		if (dataResourceId != null) {
			deleteDataResourceDocument(dataResourceId, reader);
		}

        return;
	}
	

	public static void deleteDataResourceDocument(String dataResourceId, IndexReader reader) throws Exception {
		Term term = new Term("dataResourceId", dataResourceId);
	    if (Debug.infoOn()) Debug.logInfo("in indexContent, term:" + term, module);
		int qtyDeleted = reader.delete(term);
		if (Debug.infoOn()) Debug.logInfo("in indexContent, qtyDeleted:" + term, module);

        return;
	}

	public static void indexContent(GenericDelegator delegator, Map context, GenericValue content, String path) throws Exception {
		String indexAllPath = getIndexPath(path);
		IndexWriter writer = null;
		try {
		   	writer = new IndexWriter(indexAllPath, new StandardAnalyzer(), false);
		} catch(FileNotFoundException e) {
		   	writer = new IndexWriter(indexAllPath, new StandardAnalyzer(), true);
		}
		
		indexContent(delegator, context, content, writer);
       	writer.optimize();
    	writer.close();
		return;
	}
	
	public static void indexContent(GenericDelegator delegator, Map context, GenericValue content, IndexWriter writer) throws Exception {
	    Document doc = ContentDocument.Document(content);
	    if (Debug.infoOn()) Debug.logInfo("in indexContent, content:" + content, module);
        writer.addDocument(doc);
        String dataResourceId = content.getString("dataResourceId");
        if (UtilValidate.isNotEmpty(dataResourceId)) {
            indexDataResource(delegator, context, dataResourceId, writer);
        }
        
        return;
	}
	
	public static void indexDataResource(GenericDelegator delegator, Map context, String id) throws Exception {
		String path = null;
		indexDataResource(delegator, context, id, path );
	}
	
	public static void indexDataResource(GenericDelegator delegator, Map context, String id, String path) throws Exception {
		String indexAllPath = getIndexPath(path);
		IndexWriter writer = null;
		try {
		    writer = new IndexWriter(indexAllPath, new StandardAnalyzer(), false);
		} catch(FileNotFoundException e) {
		    writer = new IndexWriter(indexAllPath, new StandardAnalyzer(), true);
		}	
		indexDataResource(delegator, context, id, writer);
	    writer.optimize();
        writer.close();

	}
	public static void indexDataResource(GenericDelegator delegator, Map context, String id, IndexWriter writer) throws Exception {
	    Document doc = DataResourceDocument.Document(id, delegator, context);
	    writer.addDocument(doc);
	}
	
	public static String getIndexPath(String path) {
		String indexAllPath = path;
		if (UtilValidate.isEmpty(indexAllPath))
			indexAllPath = UtilProperties.getPropertyValue("search", "defaultIndex");
		if (UtilValidate.isEmpty(indexAllPath))
			indexAllPath = "index";
		return indexAllPath;

	}
}
