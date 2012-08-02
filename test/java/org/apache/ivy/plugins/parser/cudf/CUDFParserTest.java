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
package org.apache.ivy.plugins.parser.cudf;

import junit.framework.TestCase;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import java.net.MalformedURLException;
import java.util.List;

/**
 * @author Adrien Lecharpentier <adrien.lecharpentier@zenika.com>
 * @since 2012-04-23 15:21
 */
public class CUDFParserTest extends TestCase {

    private CUDFParser parser;

    public void setUp() throws Exception {
        parser = new CUDFParser("");
    }

    public void testParser() throws MalformedURLException {
        List artifacts = parser.parse(
                this.getClass().getResourceAsStream("/org/apache/ivy/plugins/resolver/util/testdownload.cudf"));
        assertEquals(3, artifacts.size());
        for (int i = 0; i < artifacts.size(); i++) {
            assertNotNull("Error: an null artifact is in the list.", artifacts.get(i));
            Artifact artifact = (Artifact) artifacts.get(i);
            assertArtifactIsValid(artifact);
        }
    }

    private void assertArtifactIsValid(Artifact artifact) {
        assertNotNull("Error: the name of the artifact is null", artifact.getName());
        assertNotNull("Error: the type of the artifact is null", artifact.getType());
        assertNotNull("Error: the module revision id of the artifact is null", artifact.getModuleRevisionId());
        ModuleRevisionId moduleRevisionId = artifact.getModuleRevisionId();
        assertNotNull("Error: the organisation name of the module revision id is null",
                      moduleRevisionId.getOrganisation());
        assertNotNull("Error: the name of the module revision id is null", moduleRevisionId.getName());
        assertNotNull("Error: the version of the module revision id null", moduleRevisionId.getRevision());
    }

    public void testEmptyCUFDParsing() throws MalformedURLException {
        List artifacts =
                parser.parse(this.getClass().getResourceAsStream("/org/apache/ivy/plugins/resolver/util/empty.cudf"));
        assertTrue(artifacts.isEmpty());
    }

    public void testNullResourceParsing() throws MalformedURLException {
        try {
            parser.parse(null);
            fail("Give a null parameter should've thrown an exception");
        } catch (IllegalStateException e) {

        }
    }
}
