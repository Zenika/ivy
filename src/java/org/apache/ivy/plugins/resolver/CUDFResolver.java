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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

/**
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

    public CUDFResolver()
    {
        configure();
    }

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
    }

    public ResolvedModuleRevision getDependency( DependencyDescriptor dd, ResolveData data )
        throws ParseException
    {
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
        Message.info( ":: publishing " + artifact );
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    public String getSearchUrl()
    {
        return searchUrl;
    }

    public void setSearchUrl( String searchUrl )
    {
        this.searchUrl = searchUrl;
    }

    public String getTypeName() {
        return "cudf";
    }

    // TODO improve this
    private static final String replaceTokens( String source, String groupID, String artifactId, String version )
    {
        return source.replaceAll( "\\[groupId\\]", groupID ).replaceAll( "\\[artifactId\\]", artifactId ).replaceAll(
            "\\[version\\]", version );
    }
}
