package org.apache.ivy.plugins.cudf;

import com.zenika.cudf.apdater.IvyAdapter;
import com.zenika.cudf.model.CUDFDescriptor;
import com.zenika.cudf.model.Preamble;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.report.DownloadStatus;
import org.apache.ivy.core.report.MetadataArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.plugins.resolver.URLResolver;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Antoine Rouaze <antoine.rouaze@zenika.com>
 */
public class CUDFResolver extends URLResolver {

    private ArchivaClient client;

    private Map/*<ModuleRevisionId, ModuleDescriptor>*/ moduleDescriptorMap = new HashMap();

    public void retrieveAllDependencyMetadatas(ResolveData resolveData, ModuleDescriptor root, ResolveReport report) {
        if (client == null) {
            throw new IllegalStateException("The Archiva client must be configured.");
        }
        DependencyDescriptor[] dependencyDescriptors = root.getDependencies();
        IvyAdapter ivyAdapter = new IvyAdapter();

        CUDFDescriptor descriptor = ivyAdapter.toCUDF(new HashSet(Arrays.asList(dependencyDescriptors)));
        descriptor.setPreamble(Preamble.getDefaultPreamble());

        CUDFDescriptor result = client.resolve(descriptor);

        Set/*<ModuleDescriptor>*/ moduleDescriptors = ivyAdapter.fromCUDF(result);
        Iterator iterator = moduleDescriptors.iterator();
        while (iterator.hasNext()) {
            ModuleDescriptor moduleDescriptor = (ModuleDescriptor) iterator.next();
            moduleDescriptorMap.put(moduleDescriptor.getModuleRevisionId(), moduleDescriptor);
        }
        moduleDescriptorMap = Collections.unmodifiableMap(moduleDescriptorMap);
    }

    public ResolvedModuleRevision getDependency(DependencyDescriptor dd, ResolveData data) throws ParseException {
        Artifact metadataArtifact = new DefaultArtifact(dd.getDependencyRevisionId(), data.getDate(), dd.getDependencyId().getName(), "cudf", null, true);
        DefaultModuleDescriptor moduleDescriptor = (DefaultModuleDescriptor) moduleDescriptorMap.get(dd.getDependencyRevisionId());
        moduleDescriptor.setModuleArtifact(metadataArtifact);
        MetadataArtifactDownloadReport metadataArtifactDownloadReport = new MetadataArtifactDownloadReport(moduleDescriptor.getMetadataArtifact());
        metadataArtifactDownloadReport.setDownloadStatus(DownloadStatus.NO);
        metadataArtifactDownloadReport.setSearched(true);
        return new ResolvedModuleRevision(this, this, moduleDescriptor, metadataArtifactDownloadReport);
    }

    public void setClient(ArchivaClient client) {
        this.client = client;
    }

    //TODO: Find best way to set an object in Configurator (careful about the difference between "typeMethod" and "addMethod"). Maybe will do a refactor. Very strange...
    public void add(ArchivaClient client) {
        this.client = client;
    }

    public Map getModuleDescriptorMap() {
        return moduleDescriptorMap;
    }

}