/*
 *      Copyright 2012 Zenika
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.ivy.plugins.parser.cudf;

import org.apache.ivy.core.IvyContext;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.descriptor.DefaultDependencyArtifactDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyArtifactDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.plugins.parser.ModuleDescriptorParser;
import org.apache.ivy.plugins.parser.ParserSettings;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorWriter;
import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.repository.url.URLResource;
import org.apache.ivy.util.url.URLHandlerRegistry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * CUDFModuleDescriptorParser
 *
 * @author Antoine ROUAZE <antoine.rouaze AT zenika.com>
 */
public class CUDFModuleDescriptorParser implements ModuleDescriptorParser {

    private static final CUDFModuleDescriptorParser INSTANCE = new CUDFModuleDescriptorParser();

    public static CUDFModuleDescriptorParser getInstance() {
        return INSTANCE;
    }

    private CUDFModuleDescriptorParser() {
    }

    public ModuleDescriptor parseDescriptor(ParserSettings ivySettings, URL descriptorURL, boolean validate)
            throws ParseException, IOException {
        URLResource resource = new URLResource(descriptorURL);
        return parseDescriptor(ivySettings, descriptorURL, resource, validate);
    }

    public ModuleDescriptor parseDescriptor(ParserSettings ivySettings, URL descriptorURL, Resource res,
                                            boolean validate) throws ParseException, IOException {
        IvyContext context = IvyContext.getContext();
        ResolveData resolveData = context.getResolveData();

        CUDFParser parser = new CUDFParser("");
        List artifacts = parser.parse(URLHandlerRegistry.getDefault().openStream(descriptorURL));
        Artifact rootArtifact = (Artifact) artifacts.get(0);
        DefaultModuleDescriptor moduleDescriptor = new DefaultModuleDescriptor(this, res);
        moduleDescriptor.setResolvedPublicationDate(new Date(res.getLastModified()));
        addConfigurations(resolveData.getCurrentVisitNode().getConfsToFetch(), moduleDescriptor);
        moduleDescriptor.addArtifact("master", rootArtifact);
        ModuleRevisionId moduleRevisionId = rootArtifact.getModuleRevisionId();
        for (int i = 1; i < artifacts.size(); i++) {
            Artifact dep = (Artifact) artifacts.get(i);
            DefaultDependencyDescriptor dependencyDescriptor =
                    new DefaultDependencyDescriptor(moduleDescriptor, dep.getModuleRevisionId(), true, false, true);
            dependencyDescriptor.addDependencyConfiguration("master", "master(*)");
            DependencyArtifactDescriptor dependencyArtifactDescriptor =
                    new DefaultDependencyArtifactDescriptor(dependencyDescriptor,
                                                            dependencyDescriptor.getDependencyId().getName(),
                                                            dep.getType(), dep.getExt(), dep.getUrl(),
                                                            dep.getExtraAttributes()
                    );
            // TODO: verified scope name
            dependencyDescriptor.addDependencyArtifact("master", dependencyArtifactDescriptor);
            moduleDescriptor.addDependency(dependencyDescriptor);
            // TODO found the current configuration name!!!
        }
        moduleDescriptor.setModuleRevisionId(moduleRevisionId);
        return moduleDescriptor;
    }

    private void addConfigurations(String[] cheatConfs, DefaultModuleDescriptor moduleDescriptor) {
        moduleDescriptor.addConfiguration(new Configuration("master"));
        for (int i = 0, cheatConfsLength = cheatConfs.length; i < cheatConfsLength; i++) {
            String cheatConf = cheatConfs[i];
            moduleDescriptor.addConfiguration(
                    new Configuration(cheatConf, Configuration.Visibility.PUBLIC, "Parents conf",
                                      new String[]{"master"}, false, null
                    )
                                             );
//            moduleDescriptor.addConfiguration(new Configuration(cheatConf));
        }
    }

    public void toIvyFile(InputStream is, Resource res, File destFile, ModuleDescriptor md)
            throws ParseException, IOException {
        try {
            XmlModuleDescriptorWriter.write(md, destFile);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public boolean accept(Resource res) {
        return res.getName().endsWith("cudf");
    }

    public String getType() {
        return "cudf";
    }

    public Artifact getMetadataArtifact(ModuleRevisionId mrid, Resource res) {
        return new DefaultArtifact(mrid, new Date(res.getLastModified()), mrid.getName(), "cudf", "cudf", true);
    }

    public String toString() {
        return "CUDF parser";
    }
}
