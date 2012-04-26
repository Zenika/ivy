package org.apache.ivy.plugins.resolver;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.DownloadReport;
import org.apache.ivy.core.resolve.DownloadOptions;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.plugins.resolver.util.ResolvedResource;
import org.apache.ivy.util.Message;
import org.apache.ivy.util.url.URLHandler;
import org.apache.ivy.util.url.URLHandlerRegistry;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

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

    public ResolvedModuleRevision getDependency( DependencyDescriptor dd, ResolveData data )
        throws ParseException
    {
        configure();
        clearIvyAttempts();
        Message.info( ":: dependency " + dd );
        return super.getDependency( dd, data );
    }

    public ResolvedResource findIvyFileRef( DependencyDescriptor dd, ResolveData data )
    {
        clearIvyAttempts();
        ModuleRevisionId mrid = dd.getDependencyRevisionId();
        Message.info(
            ":: finding ivy file for " + mrid.getOrganisation() + ":" + mrid.getName() + ":" + mrid.getRevision() );
        // TODO il va me falloir le nom du fichier..
        ResolvedResource resolvedResource = new ResolvedResource( null, mrid.getRevision() );
        return resolvedResource;
    }

    public DownloadReport download( Artifact[] artifacts, DownloadOptions options )
    {
        Message.info( ":: download file" );
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

    // TODO improve this
    private static final String replaceTokens( String source, String groupID, String artifactId, String version )
    {
        return source.replaceAll( "\\[groupId\\]", groupID ).replaceAll( "\\[artifactId\\]", artifactId ).replaceAll(
            "\\[version\\]", version );
    }
}
