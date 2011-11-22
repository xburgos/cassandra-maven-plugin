package org.codehaus.mojo.cis;


import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.war.AbstractWarMojo;
import org.codehaus.plexus.archiver.war.WarArchiver;

/**
 * Generate a manifest for this WAR.
 *
 * @author Mike Perham
 * @version $Id: WarMojo.java 307363 2005-10-09 04:50:58Z brett $
 * @goal manifest
 * @phase process-resources
 * @requiresDependencyResolution runtime
 */
public class CisWarManifestMojo extends AbstractCisWarMojo
{
    /**
     * The Jar archiver.
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#war}"
     * @required
     */
    private WarArchiver warArchiver;

    protected void initWarMojo( AbstractWarMojo warMojo ) throws MojoExecutionException
    {
        super.initWarMojo( warMojo );
        setParameter( warMojo, "warArchiver", warArchiver );
    }

    protected AbstractWarMojo newWarMojo()
    {
        return new org.apache.maven.plugin.war.WarManifestMojo();
    }
}
