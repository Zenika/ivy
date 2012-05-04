package org.apache.ivy.plugins.resolver;

import junit.framework.*;
import org.apache.ivy.core.module.descriptor.*;
import org.apache.ivy.core.module.id.*;
import org.apache.ivy.core.resolve.*;
import org.apache.ivy.util.url.*;

import java.io.*;
import java.text.*;

/**
 * @author Antoine ROUAZE <antoine.rouaze AT zenika.com>
 */
public class CUDFResolverTest extends TestCase {

    private CUDFResolver resolver;

    public void setUp() throws Exception {
        resolver = new CUDFResolver();
        resolver.setUrl(new File("test/repositories/cudf").toURI().toURL().toString());
        resolver.setSearchUrl("/[groupId]/[artifactId]/[version]");
        resolver.setPattern("[artifactId]-[version].jar");
        resolver.setUrlHandler(URLHandlerRegistry.getDefault());
    }

    public void testGetDependency() throws ParseException {
        ModuleRevisionId moduleRevisionId = ModuleRevisionId.newInstance("org/apache", "test", "1.0");
        DependencyDescriptor dependencyDescriptor = new DefaultDependencyDescriptor(moduleRevisionId, false);
        ResolvedModuleRevision moduleRevision = resolver.getDependency(dependencyDescriptor, null);
        System.out.println(moduleRevision);
    }
}