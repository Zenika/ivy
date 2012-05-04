package org.apache.ivy.plugins.parser.cudf;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author adrien
 * @since 2012-04-23 14:59
 */
public class CUDFParser
{
    private static final String PACKAGE_START_LINE = "package: ";

    private static final String NUMBER_START_LINE = "number: ";

    private static final String URL_START_LINE = "url: ";

    private static final String SEPARATOR = "%3a";

    private static final String TYPE_START_LINE = "type: ";

    private static final String VERSION_START_LINE = "version: ";

    /**
     * Returns a list of Artifacts fetch from a CUDF formatted InputStream.
     * <p/>
     * The List returned will never be null but at worst it will be empty (contract).
     *
     * @param inputStream the stream to parse
     * @return a list of Artifact that are all needed. The list will never be null but at worst an empty list.
     * @throws MalformedURLException if the url in the cudf output is not correct.
     */
    public List/*<Artifact>*/ parse( InputStream inputStream )
        throws MalformedURLException
    {
        if ( inputStream == null )
        {
            throw new IllegalStateException();
        }
        List artifacts = new ArrayList();
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader( new InputStreamReader( inputStream, "UTF-8" ) );
            String next = reader.readLine();
            String packageLine = null;
            String versionLine = null;
            String urlLine = null;
            String typeLine = null;
            String cudfVersion = null;
            while ( true )
            {
                String line = next;
                for ( next = reader.readLine(); next != null && next.length() > 1 && next.charAt( 0 ) == ' ';
                      next = reader.readLine() )
                {
                    line = line + next.substring( 1 );
                }

                if ( line == null || ( line != null && line.length() == 0 ) )
                {
                    if ( cudfVersion != null && !"0".equals( versionLine.substring( VERSION_START_LINE.length() ).trim() ) )
                    {
                        validateArtifact( packageLine, versionLine, urlLine, typeLine, artifacts );
                    }
                    packageLine = versionLine = urlLine = typeLine = cudfVersion = null;
                    if ( line == null )
                    {
                        break;
                    }
                }

                if ( line.startsWith( "#" ) || line.startsWith( "preamble:" ) || line.startsWith( "property: " )
                    || line.startsWith( "univ-checksum: " ) || ( line.length() > 0 && line.charAt( 0 ) == ' ' ) )
                {
                    continue;
                }

                line = line.trim();
                if ( line.startsWith( PACKAGE_START_LINE ) )
                {
                    packageLine = line;
                }
                else if ( line.startsWith( NUMBER_START_LINE ) )
                {
                    versionLine = line;
                }
                else if ( line.startsWith( URL_START_LINE ) )
                {
                    urlLine = line;
                }
                else if ( line.startsWith( TYPE_START_LINE ) )
                {
                    typeLine = line;
                }
                else if ( line.startsWith( VERSION_START_LINE ) )
                {
                    cudfVersion = line;
                }
            }
        }
        catch ( MalformedURLException e )
        {
            throw e;
        }
        catch ( IOException e )
        {
            return Collections.emptyList();
        }
        finally
        {
            if ( reader != null )
            {
                try
                {
                    reader.close();
                }
                catch ( IOException e )
                {
                    return Collections.emptyList();
                }
            }
        }
        return artifacts;
    }

    private void validateArtifact( String packageLine, String versionLine, String urlLine, String typeLine,
                                   List artifacts )
        throws MalformedURLException
    {
        if ( packageLine == null || versionLine == null )
        {
            return;
        }
        String[] info = packageLine.substring( PACKAGE_START_LINE.length() ).trim().split( SEPARATOR );
        String version = versionLine.substring( NUMBER_START_LINE.length() ).trim();
        String type = typeLine == null ? "" : typeLine.substring( TYPE_START_LINE.length() ).trim();
        Map/*<String, String>*/ extraAttributes = new HashMap();
        String url = null;
        Artifact artifact = null;
        if ( urlLine != null )
        {
            url = urlLine.substring( URL_START_LINE.length() ).trim().replaceAll( SEPARATOR, ":" );
            extraAttributes.put( "url",  url );
            artifact = new DefaultArtifact( ModuleRevisionId.newInstance( info[0], info[1], version ), new Date(), info[1], type,
                    type, new URL(url), extraAttributes );
        } else {
            artifact = new DefaultArtifact( ModuleRevisionId.newInstance( info[0], info[1], version ), new Date(), info[1], type,
                    type, extraAttributes );
        }
        artifacts.add( artifact );
    }
}
