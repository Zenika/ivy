package org.apache.ivy.plugins.resolver;

import junit.framework.TestCase;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.plugins.repository.Repository;
import org.apache.ivy.plugins.repository.url.URLRepository;
import org.apache.ivy.util.url.URLHandlerRegistry;

import java.io.File;
import java.text.ParseException;

/**
 * @author Antoine ROUAZE <antoine.rouaze AT zenika.com>
 */
public class CUDFResolverTest extends TestCase {

    private static final String BASE_DIR = "test/repositories/cudf";

    private CUDFResolver resolver;

    public void setUp() throws Exception {
        Repository repository = new URLRepository();
        resolver = new CUDFResolver();
        resolver.setUrl(new File(BASE_DIR).toURI().toURL().toString());
        resolver.setSearchUrl("/[groupId]/[artifactId]/[version]");
        resolver.setPattern("[artifactId]-[version].jar");
        resolver.setUrlHandler(URLHandlerRegistry.getDefault());
        resolver.setRepository(repository);
    }

    public void testGetDependency() throws ParseException {
        ModuleRevisionId moduleRevisionId = ModuleRevisionId.newInstance("org/apache", "test", "1.0");
        DependencyDescriptor dependencyDescriptor = new DefaultDependencyDescriptor(moduleRevisionId, false);
        ResolvedModuleRevision moduleRevision = resolver.getDependency(dependencyDescriptor, null);
        ModuleRevisionId resolvedModuleRevisionId = moduleRevision.getDescriptor().getResolvedModuleRevisionId();
        assertEquals("Error: Unexpected value of resolved module id organisation", resolvedModuleRevisionId.getOrganisation(), moduleRevision.getId().getOrganisation());
        assertEquals("Error: Unexpected value of resolved module id name", resolvedModuleRevisionId.getName(), moduleRevision.getId().getName());
        assertEquals("Error: Unexpected value of resolved module id revision", resolvedModuleRevisionId.getRevision(), moduleRevision.getId().getRevision());
        assertEquals("Error: The module revision should have to have two dependencies attached to it", moduleRevision.getDescriptor().getDependencies().length, 2);
        System.out.println(moduleRevision);
    }
}