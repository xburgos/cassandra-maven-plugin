package org.codehaus.plexus.shade.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.shade.Shader;
import org.codehaus.plexus.shade.resource.ComponentsXmlResourceTransformer;
import org.codehaus.plexus.shade.relocation.SimpleRelocator;

import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;

/**
 * @author Jason van Zyl
 * @goal shade
 * @phase package
 */
public class ShadeMojo
    extends AbstractMojo
{
    /**
     * @parameter expression="${project}"
     * @readonly
     */
    private MavenProject project;

    /** @component */
    private Shader shader;

    /**
     * Artifacts to include/exclude from the final artifact.
     *
     * @parameter
     */
    private Resource artifactSet;

    /**
     * Packages to be relocated.
     *
     * @parameter
     */
    private PatternRelocator[] relocations;

    /** @parameter expression="${project.build.directory}" */
    private File outputDirectory;

    /** @throws MojoExecutionException  */
    public void execute()
        throws MojoExecutionException
    {
        Set artifacts = new HashSet();

        for ( Iterator it = project.getArtifacts().iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();

            String id = artifact.getGroupId() + ":" + artifact.getArtifactId();

            if ( artifactSet.getExcludes().contains( id ) )
            {
                getLog().info( "Excluding " + artifact.getId() + " from the uberjar." );

                continue;
            }

            artifacts.add( artifact.getFile() );
        }

        artifacts.add( project.getArtifact().getFile() );

        String oldName = project.getArtifact().getFile().getName();

        String newName = oldName.substring( 0, oldName.lastIndexOf( '.' ) ) + "-uber.jar";

        File outputJar = new File( outputDirectory, newName );

        // Now add our extra resources

        try
        {
            List relocators = new ArrayList();

            for ( int i = 0; i < relocations.length; i++ )
            {
                PatternRelocator r = relocations[i];

                if ( r.getExcludes() != null )
                {
                    relocators.add( new SimpleRelocator( r.getPattern(), r.getExcludes()) );
                }
                else
                {
                    relocators.add( new SimpleRelocator( r.getPattern(), null ) );
                }
            }

            List resourceTransformers = new ArrayList();

            resourceTransformers.add( new ComponentsXmlResourceTransformer() );

            shader.shade( artifacts, outputJar, relocators, resourceTransformers );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error creating uber Jar." );
        }
    }
}
