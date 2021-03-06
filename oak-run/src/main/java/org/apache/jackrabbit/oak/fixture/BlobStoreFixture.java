/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.jackrabbit.oak.fixture;

import java.io.Closeable;
import java.io.File;
import java.util.Map;

import javax.annotation.CheckForNull;

import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.core.data.DataStore;
import org.apache.jackrabbit.core.data.DataStoreException;
import org.apache.jackrabbit.core.data.FileDataStore;
import org.apache.jackrabbit.oak.commons.PropertiesUtil;
import org.apache.jackrabbit.oak.plugins.blob.datastore.DataStoreBlobStore;
import org.apache.jackrabbit.oak.spi.blob.BlobStore;
import org.apache.jackrabbit.oak.spi.blob.FileBlobStore;
import org.apache.jackrabbit.oak.spi.blob.MemoryBlobStore;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class BlobStoreFixture implements Closeable{
    private final String name;
    protected final String unique;

    public BlobStoreFixture(String name) {
        this.name = name;
        this.unique = getUniqueName(name);
    }

    public abstract BlobStore setUp();

    public abstract void tearDown();

    public abstract long size();

    public void close(){
        tearDown();
    }

    /**
     * Creates an instance of the BlobStoreFixture based on configuration
     * determined from system properties
     *
     * @param basedir directory to be used in case of file based BlobStore
     * @param fallbackToFDS if true then FileDataStore would be used in absence of
     *                      any explicitly defined BlobStore
     */
    @CheckForNull
    public static BlobStoreFixture create(File basedir, boolean fallbackToFDS) {
        String className = System.getProperty("dataStore");
        if (className != null) {
            return getDataStore();
        }

        if(basedir == null){
            basedir = FileUtils.getTempDirectory();
        }

        String blobStore = System.getProperty("blobStoreType");
        if ("FDS".equals(blobStore) || (blobStore == null && fallbackToFDS)) {
            return getFileDataStore(basedir, DataStoreBlobStore.DEFAULT_CACHE_SIZE);
        } else if ("FBS".equals(blobStore)) {
            return getFileBlobStore(basedir);
        } else if ("MEM".equals(blobStore)) {
            return getMemoryBlobStore();
        }

        return null;
    }

    public static BlobStoreFixture getFileDataStore(final File basedir, final int fdsCacheInMB) {
        return new BlobStoreFixture("FDS") {
            private File storeDir;
            private FileDataStore fds;

            @Override
            public BlobStore setUp() {
                fds = new FileDataStore();
                fds.setMinRecordLength(4092);
                storeDir = new File(basedir, unique);
                fds.init(storeDir.getAbsolutePath());
                configure(fds);
                BlobStore bs = new DataStoreBlobStore(fds, true, fdsCacheInMB);
                configure(bs);
                return bs;
            }

            @Override
            public void tearDown() {
                fds.close();
                FileUtils.deleteQuietly(storeDir);
            }

            @Override
            public long size() {
                return FileUtils.sizeOfDirectory(storeDir);
            }
        };
    }

    public static BlobStoreFixture getFileBlobStore(final File basedir) {
        return new BlobStoreFixture("FBS") {
            private File storeDir;
            private FileBlobStore fbs;

            @Override
            public BlobStore setUp() {
                storeDir = new File(basedir, unique);
                fbs = new FileBlobStore(storeDir.getAbsolutePath());
                configure(fbs);
                return fbs;
            }

            @Override
            public void tearDown() {
                FileUtils.deleteQuietly(storeDir);
            }

            @Override
            public long size() {
                return FileUtils.sizeOfDirectory(storeDir);
            }
        };
    }

    public static BlobStoreFixture getMemoryBlobStore() {
        return new BlobStoreFixture("MEM") {
            private MemoryBlobStore mbs = new MemoryBlobStore();

            @Override
            public BlobStore setUp() {
                return mbs;
            }

            @Override
            public void tearDown() {

            }

            @Override
            public long size() {
                throw new UnsupportedOperationException("Implementation pending");
            }
        };
    }

    public static BlobStoreFixture getDataStore() {
        return new BlobStoreFixture("DS") {
            private DataStore dataStore;
            private BlobStore blobStore;

            @Override
            public BlobStore setUp() {
                String className = System.getProperty("dataStore");
                checkNotNull(className, "No system property named 'dataStore' defined");
                try {
                    dataStore = Class.forName(className).asSubclass(DataStore.class).newInstance();
                    configure(dataStore);
                    dataStore.init(null);
                    blobStore = new DataStoreBlobStore(dataStore);
                    configure(blobStore);
                    return blobStore;
                } catch (Exception e) {
                    throw new IllegalStateException("Cannot instantiate DataStore " + className, e);
                }
            }

            @Override
            public void tearDown() {
                if (blobStore instanceof DataStoreBlobStore) {
                    ((DataStoreBlobStore) blobStore).clearInUse();
                    try {
                        ((DataStoreBlobStore) blobStore).deleteAllOlderThan(
                                System.currentTimeMillis() + 10000000);
                    } catch (DataStoreException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public long size() {
                throw new UnsupportedOperationException("Implementation pending");
            }
        };
    }


    //~------------------------------------------------< utility >

    private static String getUniqueName(String name) {
        return String.format("%s-%d", name, System.currentTimeMillis());
    }

    private static void configure(Object o) {
        PropertiesUtil.populate(o, getConfig(), false);
    }

    private static Map<String, ?> getConfig() {
        Map<String, Object> result = Maps.newHashMap();
        for (Map.Entry<String, ?> e : Maps.fromProperties(System.getProperties()).entrySet()) {
            String key = e.getKey();
            if (key.startsWith("ds.") || key.startsWith("bs.")) {
                key = key.substring(3); //length of bs.
                result.put(key, e.getValue());
            }
        }
        return result;
    }
}
