package org.codehaus.mojo.shade.mojo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.mojo.shade.Shader;
import org.codehaus.mojo.shade.relocation.SimpleRelocator;
import org.codehaus.mojo.shade.resource.ComponentsXmlResourceTransformer;

/**
 * @author Jason van Zyl
 * @author Mauro Talevi
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
    private MavenProjectHelper projectHelper;
    
    /** @component */
    private Shader shader;

    /**
     * Artifacts to include/exclude from the final artifact.
     *
     * @parameter
     */
    private ArtifactSet artifactSet;

    /**
     * Packages to be relocated.
     *
     * @parameter
     */
    private PackageRelocation[] relocations;

    /** @parameter expression="${project.build.directory}" */
    private File outputDirectory;

    /**
     * The name of the shaded artifactId 
     * 
     * @parameter expression="${shadedArtifactId}" default-value="${project.artifactId}"
     */
    private String shadedArtifactId;

    /**
     * Defines whether the shaded artifact should be attached as classifier to 
     * the original artifact.  If false, the shaded jar will be the main artifact 
     * of the project
     * 
     * @parameter expression="${shadedArtifactAttached}" default-value="false"
     */
    private boolean shadedArtifactAttached;

    /**
     * The name of the classifier used in case the shaded artifact is attached.
     * 
     * @parameter expression="${shadedClassifierName}" default-value="shaded"
     */
    private String shadedClassifierName;

    /** @throws MojoExecutionException  */
    public void execute()
        throws MojoExecutionException
    {
        Set artifacts = new HashSet();

        for ( Iterator it = project.getArtifacts().iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();

            if ( excludeArtifact(artifact) )
            {
                getLog().info( "Excluding " + artifact.getId() + " from the shaded jar." );

                continue;
            }

            artifacts.add( artifact.getFile() );
        }

        artifacts.add( project.getArtifact().getFile() );


        File outputJar = shadedArtifactFileWithClassifier();

        // Now add our extra resources
        try
        {
            List relocators = getRelocators();

            List resourceTransformers = getResourceTrasformers();

            shader.shade( artifacts, outputJar, relocators, resourceTransformers );
            
            if ( shadedArtifactAttached )
            {
                getLog().info( "Attaching shaded artifact." );
                projectHelper.attachArtifact( getProject(), "jar", shadedClassifierName, outputJar );
            }
            else
            {
                getLog().info( "Replacing original artifact with shaded artifact." );
                outputJar.renameTo( shadedArtifactFile() );
            }            
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error creating shaded jar.", e);
        }
    }

    private boolean excludeArtifact( Artifact artifact )
    {
        String id = artifact.getGroupId() + ":" + artifact.getArtifactId();
        if ( excludedArtifacts().contains( id ) )
        {
            return true;
        }
        return false;
    }

    private Set excludedArtifacts()
    {
        if ( artifactSet != null && artifactSet.getExcludes() != null )
        {
            return artifactSet.getExcludes();
            
        }
        return Collections.EMPTY_SET;
    }

    private List getRelocators()
    {
        List relocators = new ArrayList();

        if ( relocations == null ) 
        {
            return relocators;
        }
        
        for ( int i = 0; i < relocations.length; i++ )
        {
            PackageRelocation r = relocations[i];

            if ( r.getExcludes() != null )
            {
                relocators.add( new SimpleRelocator( r.getPattern(), r.getExcludes()) );
            }
            else
            {
                relocators.add( new SimpleRelocator( r.getPattern(), null ) );
            }
        }
        return relocators;
    }

    private List getResourceTrasformers()
    {
        List resourceTransformers = new ArrayList();

        resourceTransformers.add( new ComponentsXmlResourceTransformer() );
        
        return resourceTransformers;
    }


    private File shadedArtifactFileWithClassifier()
    {
        Artifact artifact = project.getArtifact();
        final String shadedName = shadedArtifactId + "-" + artifact.getVersion() + "-" + shadedClassifierName + "."
            + artifact.getType();
        return new File( outputDirectory, shadedName );
    }
    
    private File shadedArtifactFile()
    {
        Artifact artifact = project.getArtifact();
        final String shadedName = shadedArtifactId + "-" + artifact.getVersion() + "." + artifact.getType();
        return new File( outputDirectory, shadedName );
    }    

    protected MavenProject getProject()
    {
        if ( project.getExecutionProject() != null )
        {
            return project.getExecutionProject();
        }
        else
        {
            return project;
        }
    }
}
