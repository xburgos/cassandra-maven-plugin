package org.codehaus.mojo.minijar;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.commons.io.IOUtils;
import org.codehaus.mojo.minijar.resource.ApacheLegalResourceTransformer;
import org.codehaus.mojo.minijar.resource.ComponentsXmlResourceTransformer;
import org.codehaus.mojo.minijar.resource.ResourceTransformer;
import org.codehaus.mojo.minijar.resource.MinijarResourceMatcher;
import org.vafer.dependency.utils.ResourceMatcher;
import org.vafer.dependency.utils.ResourceRenamer;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Creates an ueberjar including all dependencies into one jar.
 *
 * @goal ueberjar
 * @requiresDependencyResolution compile
 */
public final class UeberJarMojo
    extends AbstractPluginMojo
{
    /**
     * @parameter expression="${stripUnusedClasses}" default-value="true"
     */
    private boolean stripUnusedClasses;

    /**
     * @parameter expression="${includeArtifact}" default-value="true"
     * @readonly
     */
    private boolean includeArtifact;

    /**
     * @parameter expression="${renameClasses}" default-value="true"
     * @readonly
     */
    private boolean renameClasses;

    /**
     * @parameter
     */
    private ArrayList excludes;

    /**
     * @parameter
     */
    private ArrayList dependenciesToHide = new ArrayList();

    /**
     * @parameter expression="${hiddenPackageName}" default-value="hidden"
     * @readonly
     */
    private String hiddenPackageName;

    /**
     * @component
     */
    private MavenProjectHelper projectHelper;

    /**
     * Converts a String into name that can be used as java package name
     * @param name input String to create a java package name from
     * @return java package name derived from input String
     */
    private String convertToValidPackageName( final String name )
    {
        final char[] chars = name.toCharArray();
        final StringBuffer sb = new StringBuffer();

        if ( chars.length > 0 )
        {
            final char c = chars[0];
            if ( Character.isJavaIdentifierStart( c ) )
            {
                sb.append( c );
            }
            else
            {
                sb.append( "C" );
            }
        }

        for ( int i = 1; i < chars.length; i++ )
        {
            final char c = chars[ i ];
            if ( Character.isJavaIdentifierPart( c ) )
            {
                sb.append( c );
            }
        }

        return sb.toString();
    }

    /**
     * Creates a combine jar of the dependencies and (as configured) also the build artifact
     * @param remove Set of classes that can be removed
     * @throws MojoExecutionException on error
     */
    private void createUeberJar( final Set remove ) throws MojoExecutionException
    {
        Set artifacts = new HashSet();

        for ( Iterator it = getDependencies().iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();

            String id = artifact.getGroupId() + ":" + artifact.getArtifactId();

            if ( excludes.contains( id ) )
            {
                getLog().info( "Excluding " + artifact.getId() + " from the ueberjar." );
                
                continue;
            }

            artifacts.add( artifact );
        }

        final Artifact projectArtifact = project.getArtifact();

        if ( includeArtifact )
        {
            getLog().info( "Including project artifact." );
            artifacts.add( projectArtifact );
        }

        Collection transformers = new ArrayList();

        transformers.add( new ComponentsXmlResourceTransformer() );

        transformers.add( new ApacheLegalResourceTransformer() );

        final MinijarResourceMatcher matcher = createMatcher( remove, transformers );

        final File[] jars = new File[ artifacts.size() ];
        final MinijarResourceMatcher[] matchers = new MinijarResourceMatcher[ artifacts.size() ];
        final ResourceRenamer[] renamers = new ResourceRenamer[ artifacts.size() ];


        final Iterator it = artifacts.iterator();
        for ( int i = 0; i < jars.length; i++ )
        {
            final Artifact artifact = (Artifact) it.next();
            final File file = artifact.getFile();
            jars[i] = file;
            matchers[i] = matcher;
            renamers[i] = new ResourceRenamer()
            {
                public String getNewNameFor( final String pResourceName )
                {
                    if ( !dependenciesToHide.contains( artifact.getGroupId() + ":" + artifact.getArtifactId() ) || artifact == projectArtifact )
                    {
                        return pResourceName;
                    }

                    // TODO: make renaming pattern configurable
                    return convertToValidPackageName( hiddenPackageName ) + "/" + pResourceName;
                }
            };
        }

        // TODO: wrap calls to getResource() and getResourceAsStream() for runtime conversion (maybe this can done by JarUtils.combineJars() )

        // TODO: make output file name construction configurable
        final String oldName = projectArtifact.getFile().getName();
        final String newName = oldName.substring( 0, oldName.lastIndexOf( '.' ) ) + "-ueber.jar";

        final File outputJar = new File( buildDirectory, newName );

        // Now add our extra resources

        try
        {

            JarUtils.combineJars(
                    jars,
                    matchers,
                    renamers,
                    outputJar,
                    transformers
                    );

        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not create combined output jar " + outputJar, e );
        }

        getLog().info( "Attaching artifact." );

        projectHelper.attachArtifact( project, "jar", "ueber", outputJar );
    }


    /**
     * Main entry point
     * @throws MojoExecutionException on error
     */
    public void execute()
        throws MojoExecutionException
    {
        final Set remove;

        if ( stripUnusedClasses )
        {
            getLog().info( "Calculating transitive hull of dependencies." );

            try
            {
                remove = getNotRequiredClazzes();
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Could not analyse classpath dependencies", e );
            }
        }
        else
        {
            remove = new HashSet();
        }

        // create one big jar. either just deps or artifact+deps. can be either stripped or not
        createUeberJar( remove );
    }

}
