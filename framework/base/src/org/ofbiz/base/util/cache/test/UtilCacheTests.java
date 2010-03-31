/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.base.util.cache.test;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilObject;
import org.ofbiz.base.util.cache.CacheListener;
import org.ofbiz.base.util.cache.UtilCache;
import org.ofbiz.base.test.GenericTestCaseBase;

public class UtilCacheTests extends GenericTestCaseBase implements Serializable {
    public static final String module = UtilCacheTests.class.getName();

    protected static abstract class Change<V> {
        protected int count = 1;
    }

    protected static final class Removal<V> extends Change<V> {
        protected final V oldValue;

        protected Removal(V oldValue) {
            this.oldValue = oldValue;
        }

        public int hashCode() {
            return UtilObject.doHashCode(oldValue);
        }

        public boolean equals(Object o) {
            if (o instanceof Removal) {
                Removal other = (Removal) o;
                return UtilObject.equalsHelper(oldValue, other.oldValue);
            }
            return false;
        }
    }

    protected static final class Addition<V> extends Change<V> {
        protected final V newValue;

        protected Addition(V newValue) {
            this.newValue = newValue;
        }

        public int hashCode() {
            return UtilObject.doHashCode(newValue);
        }

        public boolean equals(Object o) {
            if (o instanceof Addition) {
                Addition other = (Addition) o;
                return UtilObject.equalsHelper(newValue, other.newValue);
            }
            return false;
        }
    }

    protected static final class Update<V> extends Change<V> {
        protected final V newValue;
        protected final V oldValue;

        protected Update(V newValue, V oldValue) {
            this.newValue = newValue;
            this.oldValue = oldValue;
        }

        public int hashCode() {
            return UtilObject.doHashCode(newValue) ^ UtilObject.doHashCode(oldValue);
        }

        public boolean equals(Object o) {
            if (o instanceof Update) {
                Update other = (Update) o;
                if (!UtilObject.equalsHelper(newValue, other.newValue)) {
                   return false;
                }
                return UtilObject.equalsHelper(oldValue, other.oldValue);
            }
            return false;
        }
    }

    protected static class Listener<K, V> implements CacheListener<K, V> {
        protected Map<K, Set<Change<V>>> changeMap = new HashMap<K, Set<Change<V>>>();

        private void add(K key, Change<V> change) {
            Set<Change<V>> changeSet = changeMap.get(key);
            if (changeSet == null) {
                changeSet = new HashSet<Change<V>>();
                changeMap.put(key, changeSet);
            }
            for (Change<V> checkChange: changeSet) {
                if (checkChange.equals(change)) {
                    checkChange.count++;
                    return;
                }
            }
            changeSet.add(change);
        }

        public synchronized void noteKeyRemoval(UtilCache<K, V> cache, K key, V oldValue) {
            add(key, new Removal<V>(oldValue));
        }

        public synchronized void noteKeyAddition(UtilCache<K, V> cache, K key, V newValue) {
            add(key, new Addition<V>(newValue));
        }

        public synchronized void noteKeyUpdate(UtilCache<K, V> cache, K key, V newValue, V oldValue) {
            add(key, new Update<V>(newValue, oldValue));
        }

        public boolean equals(Object o) {
            Listener other = (Listener) o;
            return changeMap.equals(other.changeMap);
        }
    }

    private static <K, V> Listener<K, V> createListener(UtilCache<K, V> cache) {
        Listener<K, V> listener = new Listener<K, V>();
        cache.addListener(listener);
        return listener;
    }

    public UtilCacheTests(String name) {
        super(name);
    }

    private <K, V> UtilCache<K, V> createUtilCache(int maxSize, int maxInMemory, long ttl, boolean useSoftReference, boolean useFileSystemStore) {
        return UtilCache.createUtilCache(getClass().getName() + "." + getName(), maxSize, maxInMemory, ttl, useSoftReference, useFileSystemStore);
    }

    private static <K, V> void assertUtilCacheSettings(UtilCache<K, V> cache, Integer maxSize, Integer maxInMemory, Long expireTime, Boolean useSoftReference, Boolean useFileSystemStore) {
        if (maxSize != null) {
            assertEquals(cache.getName() + ":maxSize", maxSize.intValue(), cache.getMaxSize());
        }
        if (maxInMemory != null) {
            assertEquals(cache.getName() + ":maxInMemory", maxInMemory.intValue(), cache.getMaxInMemory());
        }
        if (expireTime != null) {
            assertEquals(cache.getName() + ":expireTime", expireTime.longValue(), cache.getExpireTime());
        }
        if (useSoftReference != null) {
            assertEquals(cache.getName() + ":useSoftReference", useSoftReference.booleanValue(), cache.getUseSoftReference());
        }
        if (useFileSystemStore != null) {
            assertEquals(cache.getName() + ":useFileSystemStore", useFileSystemStore.booleanValue(), cache.getUseFileSystemStore());
        }
        assertEquals("initial empty", true, cache.isEmpty());
        assertEquals("empty keys", Collections.emptySet(), cache.getCacheLineKeys());
        assertEquals("empty values", Collections.emptyList(), cache.values());
        assertSame("find cache", cache, UtilCache.findCache(cache.getName()));
        assertNotSame("new cache", cache, UtilCache.createUtilCache());
    }

    public void testCreateUtilCache() {
        String name = getClass().getName() + "." + getName();
        assertUtilCacheSettings(UtilCache.createUtilCache(), null, null, null, null, null);
        assertUtilCacheSettings(UtilCache.createUtilCache(name), null, null, null, null, null);
        assertUtilCacheSettings(UtilCache.createUtilCache(name, false), null, null, null, Boolean.FALSE, null);
        //assertUtilCacheSettings(UtilCache.createUtilCache(name, true), null, null, null, Boolean.TRUE, null);
        assertUtilCacheSettings(UtilCache.createUtilCache(5, 15000), 5, null, 15000L, null, null);
        assertUtilCacheSettings(UtilCache.createUtilCache(name, 6, 16000), 6, null, 16000L, null, null);
        assertUtilCacheSettings(UtilCache.createUtilCache(name, 7, 17000, false), 7, null, 17000L, Boolean.FALSE, null);
        assertUtilCacheSettings(UtilCache.createUtilCache(name, 8, 18000, true), 8, null, 18000L, Boolean.TRUE, null);
        assertUtilCacheSettings(UtilCache.createUtilCache(name, 9, 5, 19000, false, false), 9, 5, 19000L, Boolean.FALSE, null);
        assertUtilCacheSettings(UtilCache.createUtilCache(name, 10, 6, 20000, false, true), 10, 6, 20000L, Boolean.FALSE, null);
        assertUtilCacheSettings(UtilCache.createUtilCache(name, 11, 7, 21000, false, false, "a", "b"), 11, 7, 21000L, Boolean.FALSE, Boolean.FALSE);
        assertUtilCacheSettings(UtilCache.createUtilCache(name, 12, 8, 22000, false, true, "c", "d"), 12, 8, 22000L, Boolean.FALSE, Boolean.TRUE);
    }

    public <K, V> void assertKey(String label, UtilCache<K, V> cache, K key, V value, V other, int size, Map<K, V> map) {
        assertNull(label + ":get-empty", cache.get(key));
        assertFalse(label + ":containsKey-empty", cache.containsKey(key));
        V oldValue = cache.put(key, other);
        assertTrue(label + ":containsKey-class", cache.containsKey(key));
        assertEquals(label + ":get-class", other, cache.get(key));
        assertNull(label + ":oldValue-class", oldValue);
        assertEquals(label + ":size-class", size, cache.size());
        oldValue = cache.put(key, value);
        assertTrue(label + ":containsKey-value", cache.containsKey(key));
        assertEquals(label + ":get-value", value, cache.get(key));
        assertEquals(label + ":oldValue-value", other, oldValue);
        assertEquals(label + ":size-value", size, cache.size());
        map.put(key, value);
        assertEquals(label + ":map-keys", map.keySet(), cache.getCacheLineKeys());
        assertEquals(label + ":map-values", map.values(), cache.values());
    }

    private <K, V> void assertHasSingleKey(UtilCache<K, V> cache, K key, V value) {
        assertFalse("is-empty", cache.isEmpty());
        assertEquals("size", 1, cache.size());
        assertTrue("found", cache.containsKey(key));
        assertTrue("validKey", UtilCache.validKey(cache.getName(), key));
        assertFalse("validKey", UtilCache.validKey(":::" + cache.getName(), key));
        assertEquals("get", value, cache.get(key));
        assertEquals("keys", new HashSet<K>(UtilMisc.toList(key)), cache.getCacheLineKeys());
        assertEquals("values", UtilMisc.toList(value), cache.values());
    }

    private <K, V> void assertNoSingleKey(UtilCache<K, V> cache, K key) {
        assertFalse("not-found", cache.containsKey(key));
        assertFalse("validKey", UtilCache.validKey(cache.getName(), key));
        assertNull("no-get", cache.get(key));
        assertNull("remove", cache.remove(key));
        assertTrue("is-empty", cache.isEmpty());
        assertEquals("size", 0, cache.size());
        assertEquals("keys", Collections.emptySet(), cache.getCacheLineKeys());
        assertEquals("values", Collections.emptyList(), cache.values());
    }

    public void testSimple() throws Exception {
        UtilCache<String, String> cache = createUtilCache(5, 0, 0, false, false);
        Listener<String, String> gotListener = createListener(cache);
        Listener<String, String> wantedListener = new Listener<String, String>();
        for (int i = 0; i < 2; i++) {
            assertTrue("UtilCacheTable.keySet", UtilCache.getUtilCacheTableKeySet().contains(cache.getName()));
            assertSame("UtilCache.findCache", cache, UtilCache.findCache(cache.getName()));
            assertSame("UtilCache.getOrCreateUtilCache", cache, UtilCache.getOrCreateUtilCache(cache.getName(), cache.getMaxSize(), cache.getMaxInMemory(), cache.getExpireTime(), cache.getUseSoftReference(), cache.getUseFileSystemStore()));

            assertNoSingleKey(cache, "one");
            long origByteSize = cache.getSizeInBytes();

            wantedListener.noteKeyAddition(cache, null, "null");
            assertNull("put", cache.put(null, "null"));
            assertHasSingleKey(cache, null, "null");
            long nullByteSize = cache.getSizeInBytes();
            assertThat(nullByteSize, greaterThan(origByteSize));

            wantedListener.noteKeyRemoval(cache, null, "null");
            assertEquals("remove", "null", cache.remove(null));
            assertNoSingleKey(cache, null);

            wantedListener.noteKeyAddition(cache, "one", "uno");
            assertNull("put", cache.put("one", "uno"));
            assertHasSingleKey(cache, "one", "uno");
            long unoByteSize = cache.getSizeInBytes();
            assertThat(unoByteSize, greaterThan(origByteSize));

            wantedListener.noteKeyUpdate(cache, "one", "single", "uno");
            assertEquals("replace", "uno", cache.put("one", "single"));
            assertHasSingleKey(cache, "one", "single");
            long singleByteSize = cache.getSizeInBytes();
            assertThat(singleByteSize, greaterThan(origByteSize));
            assertThat(singleByteSize, greaterThan(unoByteSize));

            wantedListener.noteKeyRemoval(cache, "one", "single");
            assertEquals("remove", "single", cache.remove("one"));
            assertNoSingleKey(cache, "one");
            assertEquals("byteSize", origByteSize, cache.getSizeInBytes());

            wantedListener.noteKeyAddition(cache, "one", "uno");
            assertNull("put", cache.put("one", "uno"));
            assertHasSingleKey(cache, "one", "uno");

            wantedListener.noteKeyUpdate(cache, "one", "only", "uno");
            assertEquals("replace", "uno", cache.put("one", "only"));
            assertHasSingleKey(cache, "one", "only");

            wantedListener.noteKeyRemoval(cache, "one", "only");
            assertEquals("remove", "only", cache.remove("one"));
            assertNoSingleKey(cache, "one");
            assertEquals("byteSize", origByteSize, cache.getSizeInBytes());

            cache.setExpireTime(100);
            wantedListener.noteKeyAddition(cache, "one", "uno");
            assertNull("put", cache.put("one", "uno"));
            assertHasSingleKey(cache, "one", "uno");

            wantedListener.noteKeyRemoval(cache, "one", "uno");
            Thread.sleep(200);
            assertNoSingleKey(cache, "one");
        }

        assertEquals("get-miss", 10, cache.getMissCountNotFound());
        assertEquals("get-miss-total", 10, cache.getMissCountTotal());
        assertEquals("get-hit", 24, cache.getHitCount());
        assertEquals("remove-hit", 6, cache.getRemoveHitCount());
        assertEquals("remove-miss", 10, cache.getRemoveMissCount());
        cache.removeListener(gotListener);
        assertEquals("listener", wantedListener, gotListener);
        cache.clear();
    }

    public void testChangeSize() throws Exception {
        int size = 5;
        long ttl = 2000;
        UtilCache<String, Serializable> cache = createUtilCache(size, size, ttl, false, false);
        Map<String, Serializable> map = new HashMap<String, Serializable>();
        for (int i = 0; i < size; i++) {
            String s = Integer.toString(i);
            assertKey(s, cache, s, new String(s), new String(":" + s), i + 1, map);
        }
        cache.setMaxSize(2);
        for (int i = 0; i < size - 2; i++) {
            String s = Integer.toString(i);
            map.remove(s);
        }
        assertEquals("size", 2, cache.size());
    }

    private void expireTest(UtilCache<String, Serializable> cache, int size, long ttl) throws Exception {
        Map<String, Serializable> map = new HashMap<String, Serializable>();
        for (int i = 0; i < size; i++) {
            String s = Integer.toString(i);
            assertKey(s, cache, s, new String(s), new String(":" + s), i + 1, map);
        }
        Thread.sleep(ttl + 500);
        map.clear();
        for (int i = 0; i < size; i++) {
            String s = Integer.toString(i);
            assertNull("no-key(" + s + ")", cache.get(s));
        }
        assertEquals("map-keys", map.keySet(), cache.getCacheLineKeys());
        assertEquals("map-values", map.values(), cache.values());
        for (int i = 0; i < size; i++) {
            String s = Integer.toString(i);
            assertKey(s, cache, s, new String(s), new String(":" + s), i + 1, map);
        }
        assertEquals("map-keys", map.keySet(), cache.getCacheLineKeys());
        assertEquals("map-values", map.values(), cache.values());
    }

    public void testExpire() throws Exception {
        UtilCache<String, Serializable> cache = createUtilCache(5, 5, 2000, false, false);
        expireTest(cache, 5, 2000);
        long start = System.currentTimeMillis();
        useAllMemory();
        long end = System.currentTimeMillis();
        long ttl = end - start + 1000;
        cache = createUtilCache(1, 1, ttl, true, false);
        expireTest(cache, 1, ttl);
        assertFalse("not empty", cache.isEmpty());
        useAllMemory();
        assertNull("not-key(0)", cache.get("0"));
        assertTrue("empty", cache.isEmpty());
    }
}
