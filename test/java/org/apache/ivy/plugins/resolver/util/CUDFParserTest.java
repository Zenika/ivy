package org.apache.ivy.plugins.resolver.util;

import junit.framework.TestCase;
import org.apache.ivy.core.module.descriptor.Artifact;

import java.net.MalformedURLException;
import java.util.List;

/**
 * @author adrien
 * @since 2012-04-23 15:21
 */
public class CUDFParserTest
    extends TestCase
{

    public void testParser() throws MalformedURLException
    {
        CUDFParser parser = new CUDFParser();
        List artifacts = parser.parse( this.getClass().getResourceAsStream( "testdownload.cudf" ) );
        assertEquals( 3, artifacts.size() );
        for ( int i = 0; i < artifacts.size(); i++ )
        {
            assertNotNull( "Error: an null artifact is in the list.", artifacts.get( i ) );
            Artifact artifact = (Artifact) artifacts.get( i );
            assertNotNull( "Error: the url of the artifact is null.", artifact.getUrl() );
        }
    }
}
