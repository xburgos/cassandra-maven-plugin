package org.codehaus.mojo.was6;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.dom4j.Document;

/**
 * Installs an EAR into WebSphere Application Server.
 * 
 * @goal installApp
 * @author AB62939
 */
public class WsInstallAppMojo
    extends AbstractAppMojo
{
    /**
     * Flag for updating existing application or installing a brand new.
     * 
     * @parameter expression="${was6.updateExisting}" default-value="true"
     */
    private boolean updateExisting;

    /**
     * Name of target cluster to deploy to.
     * 
     * @parameter expression="${was6.targetCluster}"
     */
    private String targetCluster;

    /**
     * EAR archive to deploy.
     * 
     * @parameter expression="${was6.earFile}" default-value="${project.artifact.file}"
     */
    private File earFile;

    /**
     * {@inheritDoc}
     */
    protected String getTaskName()
    {
        return "wsInstallApp";
    }

    /**
     * {@inheritDoc}
     */
    protected void configureBuildScript( Document document )
        throws MojoExecutionException
    {
        super.configureBuildScript( document );

        if ( !earFile.canRead() )
        {
            throw new MojoExecutionException( "Bad archive: " + earFile.getAbsolutePath() );
        }
        configureTaskAttribute( document, "ear", earFile.getAbsolutePath() );

        StringBuffer options = new StringBuffer();

        options.append( "-appname " ).append( applicationName );

        if ( updateExisting )
        {
            options.append( " -update" );
        }

        if ( targetCluster != null )
        {
            options.append( " -cluster " ).append( targetCluster );
        }

        configureTaskAttribute( document, "options", options );
    }

}
