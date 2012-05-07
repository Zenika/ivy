package org.apache.ivy.plugins.resolver;

import org.apache.ivy.core.cache.ArtifactOrigin;
import org.apache.ivy.core.event.EventManager;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.DownloadStatus;
import org.apache.ivy.core.report.MetadataArtifactDownloadReport;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.plugins.parser.cudf.CUDFParser;
import org.apache.ivy.plugins.resolver.util.ResolvedResource;
import org.apache.ivy.util.url.URLHandler;
import org.apache.ivy.util.url.URLHandlerRegistry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * The CUDF resolve use a server side dependency resolver to find out all the transitive dependencies of the project.
 *
 * @author Adrien Lecharpentier
 */
public class CUDFResolver
    extends RepositoryResolver
{

    private static final String DEFAULT_URL = "http://localhost:9091/archiva";

    private static final String DEFAULT_URL_SEARCH =
        "/restServices/archivaServices/cudfService/cone/[groupId]/[artifactId]/[version]";

    private String url;

    private String searchUrl;

    private boolean useCache = false;

    private URLHandler urlHandler = URLHandlerRegistry.getHttp();

    private String pattern;


    private void configure()
    {
        if ( this.url == null )
        {
            this.url = DEFAULT_URL;
        }
        if ( this.searchUrl == null )
        {
            this.searchUrl = DEFAULT_URL_SEARCH;
        }
        if ( pattern == null )
        {
            throw new IllegalStateException( "The pattern must be configured" );
        }
    }

        public final ResolvedModuleRevision getDependency( DependencyDescriptor dd, ResolveData data )
        throws ParseException
    {
        configure();
        ResolvedModuleRevision resolvedModuleRevision;
        try
        {
            List artifacts = retrieveCUDFArtifacts( dd.getDependencyRevisionId() );
            Artifact rootArtifact = (Artifact) artifacts.get( 0 );
            DefaultModuleDescriptor moduleDescriptor =
                DefaultModuleDescriptor.newDefaultInstance( rootArtifact.getModuleRevisionId() );
            MetadataArtifactDownloadReport madr = new MetadataArtifactDownloadReport( rootArtifact );
            madr.setDownloadStatus( DownloadStatus.SUCCESSFUL );
            madr.setArtifactOrigin( new ArtifactOrigin( rootArtifact, false, "" ) );
            madr.setSearched( true );
            for ( int i = 1; i < artifacts.size(); i++ )
            {
                Artifact dep = (Artifact) artifacts.get( i );
                moduleDescriptor.addDependency( new DefaultDependencyDescriptor( dep.getModuleRevisionId(), true ) );
                // TODO found the current configuration name!!!
                moduleDescriptor.addArtifact( "default", dep );

            }
            resolvedModuleRevision = new ResolvedModuleRevision( this, this, moduleDescriptor, madr, true );
        }
        catch ( IOException e )
        {
            throw new IllegalStateException( e );
        }
        return resolvedModuleRevision;
    }

    protected void put( Artifact artifact, File src, String dest, boolean overwrite )
        throws IOException
    {
        throw new IllegalStateException( "No put possible for cudf resolver" );
    }

    protected void putChecksum( Artifact artifact, File src, String dest, boolean overwrite, String algorithm )
        throws IOException
    {
        throw new IllegalStateException( "No put possible for cudf resolver" );
    }

    protected void putSignature( Artifact artifact, File src, String dest, boolean overwrite )
        throws IOException
    {
        throw new IllegalStateException( "No put possible for cudf resolver" );
    }

    public EventManager getEventManager()
    {
        return null;
    }

    protected ResolvedModuleRevision findModuleInCache( DependencyDescriptor dd, ResolveData data )
    {
        return this.findModuleInCache( dd, data, false );
    }

    protected ResolvedModuleRevision findModuleInCache( DependencyDescriptor dd, ResolveData data, boolean anyResolver )
    {
        if ( useCache )
        {
            super.addArtifactPattern( pattern );
            return super.findModuleInCache( dd, data, anyResolver );
        }
        return null;
    }

    public ResolvedResource findIvyFileRef( DependencyDescriptor dd, ResolveData data )
    {
        return null;
    }

    public void publish( Artifact artifact, File src, boolean overwrite )
        throws IOException
    {
        throw new IllegalStateException(
            "You cannot use CUDF Resolver to publish artifact. It is only a 'resolve'/'retrieve' resolver." );
    }

    public boolean isM2compatible()
    {
        return false;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    public void setSearchUrl( String searchUrl )
    {
        this.searchUrl = searchUrl;
    }

    public void setUseCache( boolean useCache )
    {
        this.useCache = useCache;
    }

    public void setPattern( String pattern )
    {
        this.pattern = pattern;
    }

    public void setUrlHandler(URLHandler urlHandler) {
        this.urlHandler = urlHandler;
    }

    /**
     * Retrieve the list of artifacts present in a cudf format, the root artifact included.
     * <p/>
     * The list has at least one element: the root artifact.
     *
     * @param moduleRevisionId describe the root artifact
     * @return the List of all artifacts needed to the root artifact, included itself
     * @throws IOException
     */
    private List/*<Artifact>*/ retrieveCUDFArtifacts( ModuleRevisionId moduleRevisionId )
        throws IOException
    {
        InputStream inputStream = null;
        try
        {
            inputStream = urlHandler.openStream( new URL(
                replaceTokens( url + searchUrl, moduleRevisionId.getOrganisation(), moduleRevisionId.getName(),
                               moduleRevisionId.getRevision() ) ) );
            return new CUDFParser(this.url).parse( inputStream );
        }
        finally
        {
            if ( inputStream != null )
            {
                try
                {
                    inputStream.close();
                }
                catch ( IOException e )
                {
                    //nothing
                }
            }
        }
    }

    /**
     * Returns the source string with [groupId], [artfactIf] and [version] replaced.
     * <p/>
     * GEFEN TODO improve
     *
     * @param source     the string to change
     * @param groupID    the groupId
     * @param artifactId the artifactId
     * @param version    the version
     * @return the source string with [groupId], [artfactIf] and [version] replaced.
     */
    private static final String replaceTokens( String source, String groupID, String artifactId, String version )
    {
        return source.replaceAll( "\\[groupId\\]", groupID ).replaceAll( "\\[artifactId\\]", artifactId ).replaceAll(
            "\\[version\\]", version );
    }
}
