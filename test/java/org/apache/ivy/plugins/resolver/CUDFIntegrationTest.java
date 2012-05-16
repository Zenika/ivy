package org.apache.ivy.plugins.resolver;

import junit.framework.TestCase;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.util.CacheCleaner;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.FileUtil;
import org.apache.ivy.util.Message;

import java.io.File;

/**
 * @author Antoine ROUAZE <antoine.rouaze AT zenika.com>
 */
public class CUDFIntegrationTest extends TestCase{

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
        assertEquals(report.getArtifacts().size(), 3);
    }

    public void testCUDFIntegrationWithoutUrl() throws Exception {
        ivy.configure(new File("test/test-cudf/ivysettings-without-url.xml"));
        ivy.getLoggerEngine().pushLogger(new DefaultMessageLogger(Message.MSG_DEBUG));
        ResolveReport report = ivy.resolve(new File("test/test-cudf/ivy.xml"));
        assertEquals(report.getArtifacts().size(), 3);
    }

    public void testCUDFCacheIntegration() throws Exception{
        ivy.configure(new File("test/test-cudf/ivysettings.xml"));
        ivy.getLoggerEngine().pushLogger(new DefaultMessageLogger(Message.MSG_DEBUG));
        ResolveReport report = ivy.resolve(new File("test/test-cudf/ivy.xml"));
        assertTrue(report.getDownloadSize() > 0);
        ResolveReport report1 = ivy.resolve(new File("test/test-cudf/ivy.xml"));
        assertEquals(report1.getDownloadSize(), 0);
    }
}
