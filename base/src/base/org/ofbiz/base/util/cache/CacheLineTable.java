/*
 * $Id$
 *
 * Copyright (c) 2004-2005 The Open For Business Project - www.ofbiz.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package org.ofbiz.base.util.cache;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;

import org.apache.commons.collections.map.LRUMap;
import org.ofbiz.base.util.Debug;

/**
 * 
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Rev$
 * @since      3.2
 */
public class CacheLineTable implements Serializable {

    public static final String module = CacheLineTable.class.getName();
    protected static transient jdbm.RecordManager jdbmMgr = null;

    protected transient jdbm.htree.HTree fileTable = null;
    protected Map memoryTable = null;
    protected String fileStore = null;
    protected String cacheName = null;
    protected int maxInMemory = 0;

    public CacheLineTable(String fileStore, String cacheName, boolean fileTable, int maxInMemory) {
        this.fileStore = fileStore;
        this.cacheName = cacheName;
        this.maxInMemory = maxInMemory;
        if (fileTable) {
            // create the manager the first time it is needed
            if (CacheLineTable.jdbmMgr == null) {
                synchronized (this) {
                    if (CacheLineTable.jdbmMgr == null) {
                        try {
                            CacheLineTable.jdbmMgr = new JdbmRecordManager(fileStore);
                        } catch (IOException e) {
                            Debug.logError(e, module);
                        }
                    }
                }
            }
            if (CacheLineTable.jdbmMgr != null) {
                try {
                    long recno = CacheLineTable.jdbmMgr.getNamedObject(cacheName);
                    if (recno != 0) {
                        this.fileTable = jdbm.htree.HTree.load(CacheLineTable.jdbmMgr, recno);
                    } else {
                        this.fileTable = jdbm.htree.HTree.createInstance(CacheLineTable.jdbmMgr);
                        CacheLineTable.jdbmMgr.setNamedObject(cacheName, this.fileTable.getRecid());
                        CacheLineTable.jdbmMgr.commit();
                    }
                } catch (IOException e) {
                    Debug.logError(e, module);
                }
            }
        }
        this.setLru(maxInMemory);
    }

    public synchronized Object put(Object key, Object value) {
        memoryTable.put(key, value);
        if (fileTable != null) {
            try {
                fileTable.put(key, value);                
                CacheLineTable.jdbmMgr.commit();
            } catch (IOException e) {
                Debug.logError(e, module);
            }
        }
        return value;
    }

    public Object get(Object key) {
        Object value = memoryTable.get(key);
        if (value == null) {
            if (fileTable != null) {
                try {
                    value = fileTable.get(key);
                } catch (IOException e) {
                    Debug.logError(e, module);
                }
            }
        }
        return value;
    }

    public synchronized Object remove(Object key) {
        Object value = this.get(key);
        if (fileTable != null) {
            try {
                fileTable.remove(key);
            } catch (IOException e) {
                Debug.logError(e, module);
            }
        }
        memoryTable.remove(key);
        return value;
    }

    public synchronized Collection values() {
        List values = FastList.newInstance();

        if (fileTable != null) {
            try {
                jdbm.helper.FastIterator iter = fileTable.values();
                Object value = iter.next();
                while (value != null) {
                    values.add(value);
                    value = iter.next();
                }
            } catch (IOException e) {
                Debug.logError(e, module);
            }
        } else {
            values.addAll(memoryTable.values());
        }

        return values;
    }

    /**
     * 
     * @return An unmodifiable Set for the keys for this cache; to remove while iterating call the remove method on this class.
     */
    public synchronized Set keySet() {
        Set keys = FastSet.newInstance();

        if (fileTable != null) {
            try {
                jdbm.helper.FastIterator iter = fileTable.keys();
                Object key = iter.next();
                while (key != null) {
                    keys.add(key);
                    key = iter.next();
                }
            } catch (IOException e) {
                Debug.logError(e, module);
            }
        } else {
            keys.addAll(memoryTable.keySet());
        }

        return Collections.unmodifiableSet(keys);
    }

    public synchronized void clear() {
        if (fileTable != null && this.size() > 0) {
            try {
                // remove this table
                long recid = fileTable.getRecid();
                CacheLineTable.jdbmMgr.delete(recid);
                CacheLineTable.jdbmMgr.commit();
                this.fileTable = null;                

                // create a new table
                this.fileTable = jdbm.htree.HTree.createInstance(CacheLineTable.jdbmMgr);
                CacheLineTable.jdbmMgr.setNamedObject(cacheName, this.fileTable.getRecid());
                CacheLineTable.jdbmMgr.commit();
            } catch (IOException e) {
                Debug.logError(e, module);
            }
        }
        memoryTable.clear();
    }

    public int size() {
        if (fileTable != null) {
            return this.keySet().size();
        } else {
            return memoryTable.size();
        }
    }

    public synchronized void setLru(int newSize) {
        this.maxInMemory = newSize;

        Map oldmap = null;
        if (this.memoryTable != null) {
            // using linked map to preserve the order when using LRU (FastMap is a linked map)
            oldmap = FastMap.newInstance();
            oldmap.putAll(this.memoryTable);
        }

        if (newSize > 0) {
            this.memoryTable = new LRUMap(newSize);
        } else {
            this.memoryTable = FastMap.newInstance();
        }

        if (oldmap != null) {
            this.memoryTable.putAll(oldmap);
        }
    }

    public synchronized Object getKeyFromMemory(int index) {
        Iterator i = null;
        if (memoryTable instanceof LRUMap) {
            i = ((LRUMap) memoryTable).orderedMapIterator();
        } else {
            i = memoryTable.keySet().iterator();
        }

        int currentIdx = 0;
        while (i.hasNext()) {
            Object key = i.next();
            if (currentIdx == index) {
                return key;
            }
        }
        return null;
    }
}

