package org.apache.ivy.plugins.cudf;

import junit.framework.TestCase;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import java.util.Date;

/**
 * @author Antoine Rouaze <antoine.rouaze@zenika.com>
 */
public class TestCUDFResolver extends TestCase {



    public void testCUDFResolver() {
        ModuleDescriptor moduleDescriptor = createModuleDescriptor();

        CUDFResolver resolver = new CUDFResolver();
        ArchivaClientMock mock = new ArchivaClientMock();
        resolver.setClient(mock);

        resolver.initResolver(null, moduleDescriptor, null);
        assertTrue(mock.isCalled());
        assertEquals(resolver.getModuleDescriptorMap().size(), 3);
    }

    private ModuleDescriptor createModuleDescriptor() {
        ModuleRevisionId moduleRevisionId = ModuleRevisionId.newInstance("com.zenika", "test", "1.0.0");
        DefaultModuleDescriptor descriptor = new DefaultModuleDescriptor(moduleRevisionId, "", new Date());

        ModuleRevisionId dependencyRevisionId = ModuleRevisionId.newInstance("com.zenika", "jar1", "1.0");
        DefaultDependencyDescriptor dependencyDescriptor = new DefaultDependencyDescriptor(dependencyRevisionId, false);

        descriptor.addDependency(dependencyDescriptor);
        return descriptor;
    }



}
