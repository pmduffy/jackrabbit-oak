/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.oak.plugins.index.solr.configuration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Defaults for Solr configurations.
 */
public class SolrServerConfigurationDefaults {

    // --> default values for EmbeddedSolrServerConfiguration parameters <--
    public static final String SOLR_HOME_PATH = "solr";
    public static final String CORE_NAME = "oak";
    public static final String HTTP_PORT = "8983";
    public static final String LOCAL_BASE_URL = "http://127.0.0.1";
    public static final String CONTEXT = "/solr";

    // --> default values for RemoteSolrServerConfiguration parameters <--
    public static final String COLLECTION = "oak";
    public static final String HTTP_URL = "http://127.0.0.1:8983/solr/oak";
    public static final String ZK_HOST = "127.0.0.1:9983";
    public static final int SHARDS_NO = 2;
    public static final int REPLICATION_FACTOR = 2;
    public static final String CONFIGURATION_DIRECTORY = "";

    // --> default values for OakSolrConfiguration parameters <--
    public static final String PATH_FIELD_NAME = "path_exact";
    public static final String CHILD_FIELD_NAME = "path_child";
    public static final String DESC_FIELD_NAME = "path_des";
    public static final String ANC_FIELD_NAME = "path_anc";
    public static final String CATCHALL_FIELD = "catch_all";
    public static final int ROWS = Integer.MAX_VALUE;
    public static final boolean PROPERTY_RESTRICTIONS = false;
    public static final boolean PATH_RESTRICTIONS = false;
    public static final boolean PRIMARY_TYPES = false;
    public static final Collection<String> IGNORED_PROPERTIES = Collections.unmodifiableCollection(
            Arrays.asList("rep:members", "rep:authorizableId", "jcr:uuid", "rep:principalName", "rep:password"));
    public static final String TYPE_MAPPINGS = "";
    public static final String PROPERTY_MAPPINGS = "";

}
