package org.apache.ivy.plugins.parser.cudf;

import org.apache.ivy.core.cache.ArtifactOrigin;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.descriptor.DefaultDependencyArtifactDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyArtifactDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.DownloadStatus;
import org.apache.ivy.core.report.MetadataArtifactDownloadReport;
import org.apache.ivy.plugins.parser.ModuleDescriptorParser;
import org.apache.ivy.plugins.parser.ParserSettings;
import org.apache.ivy.plugins.parser.m2.PomModuleDescriptorParser;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorWriter;
import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.repository.url.URLResource;
import org.apache.ivy.util.url.URLHandler;
import org.apache.ivy.util.url.URLHandlerRegistry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author Antoine ROUAZE <antoine.rouaze AT zenika.com>
 */
public class CUDFModuleDescriptorParser implements ModuleDescriptorParser {

    private static final CUDFModuleDescriptorParser INSTANCE = new CUDFModuleDescriptorParser();

    public static CUDFModuleDescriptorParser getInstance() {
        return INSTANCE;
    }

    public CUDFModuleDescriptorParser() {

    }

    public ModuleDescriptor parseDescriptor(ParserSettings ivySettings, URL descriptorURL, boolean validate) throws ParseException, IOException {
        URLResource resource = new URLResource(descriptorURL);
        return parseDescriptor(ivySettings, descriptorURL, resource, validate);
    }

    public ModuleDescriptor parseDescriptor(ParserSettings ivySettings, URL descriptorURL, Resource res, boolean validate) throws ParseException, IOException {
        CUDFParser parser = new CUDFParser("");
        List artifacts = parser.parse(URLHandlerRegistry.getDefault().openStream(descriptorURL));
        Artifact rootArtifact = (Artifact) artifacts.get(0);
        DefaultModuleDescriptor moduleDescriptor = new DefaultModuleDescriptor(this, res);
        moduleDescriptor.setResolvedPublicationDate(new Date(res.getLastModified()));
        moduleDescriptor.addConfiguration(new Configuration("default"));
        moduleDescriptor.addArtifact("default", rootArtifact);
//        moduleDescriptor.addConfiguration(new Configuration("default", Configuration.Visibility.PUBLIC,
//                "runtime dependencies and master artifact can be used with this conf",
//                new String[] {"runtime", "master"}, true, null));
        ModuleRevisionId moduleRevisionId = rootArtifact.getModuleRevisionId();
        for (int i = 1; i < artifacts.size(); i++) {
            Artifact dep = (Artifact) artifacts.get(i);
            DefaultDependencyDescriptor dependencyDescriptor = new DefaultDependencyDescriptor(moduleDescriptor, dep.getModuleRevisionId(), true, false, true);
            dependencyDescriptor.addDependencyConfiguration("default", "default(*)");
            DependencyArtifactDescriptor dependencyArtifactDescriptor = new DefaultDependencyArtifactDescriptor(dependencyDescriptor,
                    dependencyDescriptor.getDependencyId().getName(), dep.getType(), dep.getExt(), dep.getUrl(), dep.getExtraAttributes());
            // TODO: verified scope name
            dependencyDescriptor.addDependencyArtifact("default", dependencyArtifactDescriptor);
            moduleDescriptor.addDependency(dependencyDescriptor);
            // TODO found the current configuration name!!!
        }
        moduleDescriptor.setModuleRevisionId(moduleRevisionId);
        return moduleDescriptor;
    }

    public void toIvyFile(InputStream is, Resource res, File destFile, ModuleDescriptor md) throws ParseException, IOException {
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

    public static String[] getInfo(String packageName) {
        return packageName.split(CUDFReader.SEPARATOR);
    }
}
