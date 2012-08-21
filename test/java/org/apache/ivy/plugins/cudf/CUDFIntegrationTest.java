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
package org.apache.ivy.plugins.cudf;

import junit.framework.TestCase;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.util.CacheCleaner;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;

import java.io.File;

/**
 * @author Antoine ROUAZE <antoine.rouaze AT zenika.com>
 */
public class CUDFIntegrationTest extends TestCase {

    private Ivy ivy;

    private File cache;

    protected void setUp() throws Exception {
        cache = new File("build/cache");
        System.setProperty("ivy.cache.dir", cache.getAbsolutePath());
        createCache();

        ivy = Ivy.newInstance();
    }

    private void createCache() {
        cache.mkdirs();
    }

    protected void tearDown() throws Exception {
        CacheCleaner.deleteDir(cache);
    }

    public void testCUDFIntegration() throws Exception{
        ivy.configure(new File("test/test-cudf/ivysettings.xml"));
        ivy.getLoggerEngine().pushLogger(new DefaultMessageLogger(Message.MSG_DEBUG));
        ResolveReport report = ivy.resolve(new File("test/test-cudf/ivy.xml"));
        assertEquals(3, report.getArtifacts().size());
    }

}
