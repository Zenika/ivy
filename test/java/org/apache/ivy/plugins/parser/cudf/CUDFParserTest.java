package org.apache.ivy.plugins.parser.cudf;

import junit.framework.TestCase;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import java.net.MalformedURLException;
import java.util.List;

/**
 * @author adrien
 * @since 2012-04-23 15:21
 */
public class CUDFParserTest
    extends TestCase
{

    private CUDFParser parser;

    public void setUp() throws Exception {
        parser = new CUDFParser();
    }

    public void testParser() throws MalformedURLException
    {
        List artifacts = parser.parse( this.getClass().getResourceAsStream( "/org/apache/ivy/plugins/resolver/util/testdownload.cudf" ) );
        assertEquals( 3, artifacts.size() );
        for ( int i = 0; i < artifacts.size(); i++ )
        {
            assertNotNull( "Error: an null artifact is in the list.", artifacts.get( i ) );
            Artifact artifact = (Artifact) artifacts.get( i );
            assertArtifactIsValid(artifact);
        }
    }

    private void assertArtifactIsValid(Artifact artifact) {
        assertNotNull( "Error: the url of the artifact is null.", artifact.getUrl() );
        assertNotNull("Error: the name of the artifact is null", artifact.getName());
        assertNotNull("Error: the type of the artifact is null", artifact.getType());
        assertNotNull("Error: the attribute named 'url' of the artifact is null", artifact.getAttribute("url"));
        assertNotNull("Error: the module revision id of the artifact is null", artifact.getModuleRevisionId());
        ModuleRevisionId moduleRevisionId = artifact.getModuleRevisionId();
        assertNotNull("Error: the organisation name of the module revision id is null", moduleRevisionId.getOrganisation());
        assertNotNull("Error: the name of the module revision id is null", moduleRevisionId.getName());
        assertNotNull("Error: the version of the module revision id null", moduleRevisionId.getRevision());
    }

    public void testEmptyCUFDParsing() throws MalformedURLException {
        List artifacts = parser.parse(this.getClass().getResourceAsStream( "/org/apache/ivy/plugins/resolver/util/empty.cudf"));
        assertTrue(artifacts.isEmpty());
    }

//    public void testBadCUDFParsing() throws MalformedURLException {
//        TODO: should not throw a NullPointerException
//        parser.parse(this.getClass().getResourceAsStream( "/org/apache/ivy/plugins/resolver/util/bad.cudf"));
//    }

//    public void testMissingLineInCUDFParsing() throws MalformedURLException {
//        TODO: should not throw a NullPointerException
//        parser.parse(this.getClass().getResourceAsStream( "/org/apache/ivy/plugins/resolver/util/bad2.cudf"));
//    }

    public void testBadUrlInCUDFParsing() {
        try {
            parser.parse(this.getClass().getResourceAsStream( "/org/apache/ivy/plugins/resolver/util/bad-url.cudf"));
            fail("Should thrown an exception");
        } catch (MalformedURLException e) {

        }
    }

    public void testNullResourceParsing() throws MalformedURLException {
        try {
            parser.parse(null);
            fail("Give a null parameter should've thrown an exception");
        } catch (IllegalStateException e) {

        }
    }
}
