package org.codehaus.mojo.repopurge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Remove the project dependencies from the local repository, and optionally
 * re-resolve them.
 * 
 * @author jdcasey
 * 
 * @goal local
 * @aggregator
 *
 */
public class LocalRepositoryPurgeMojo
    extends AbstractMojo
{

    /**
     * The projects in the current build. Each of these is subject to refreshing.
     * 
     * @parameter default-value="${reactorProjects}"
     * @required
     * @readonly
     */
    private List projects;

    /**
     * The list of dependencies in the form of groupId:artifactId which should
     * NOT be deleted/refreshed. This is useful for third-party artifacts.
     * 
     * @parameter
     */
    private List excludes;

    /**
     * Whether to re-resolve the artifacts once they have been deleted from the
     * local repository. If you are running this mojo from the command-line, 
     * you may want to disable this.
     * 
     * @parameter default-value="false" expression="${reResolve}"
     */
    private boolean reResolve;

    /**
     * The local repository, from which to delete artifacts.
     * 
     * @parameter default-value="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * The artifact resolver used to re-resolve dependencies, if that option is
     * enabled.
     * 
     * @component
     */
    private ArtifactResolver resolver;

    /**
     * The artifact metadata source used to resolve dependencies
     * 
     * @component
     */
    private ArtifactMetadataSource source;

    /**
     * Remove the entire artifactId directory for the artifact. 
     * ***ONLY WORKS FOR DEFAULT ARTIFACT REPOSITORY LAYOUTS.***
     * @parameter default-value="false"
     */
    private boolean scorchedEarthMode;

    /**
     * Whether this mojo should act on all transitive dependencies.
     * @parameter default-value="false"
     */
    private boolean actTransitively;

    /**
     * Used to construct artifacts for deletion/resolution...
     * @component
     */
    private ArtifactFactory factory;

    /**
     * @parameter default-value="false" expression="${purge.verbose}"
     */
    private boolean verbose;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        for ( Iterator it = projects.iterator(); it.hasNext(); )
        {
            MavenProject project = (MavenProject) it.next();

            try
            {
                refreshDependenciesForProject( project );
            }
            catch ( ArtifactResolutionException e )
            {
                MojoFailureException failure = new MojoFailureException( this,
                                                                         "Failed to refresh project dependencies for: "
                                                                             + project.getId(),
                                                                         "Artifact resolution failed for project: "
                                                                             + project.getId() );
                failure.initCause( e );

                throw failure;
            }
        }
    }

    private Map createArtifactMap( MavenProject project )
    {
        Map artifactMap = Collections.EMPTY_MAP;
        
        List dependencies = project.getDependencies();
        
        List remoteRepositories = Collections.EMPTY_LIST;
        
        Set dependencyArtifacts = new HashSet();

        for ( Iterator it = dependencies.iterator(); it.hasNext(); )
        {
            Dependency dependency = (Dependency) it.next();

            VersionRange vr = VersionRange.createFromVersion( dependency.getVersion() );
            
            Artifact artifact = factory.createDependencyArtifact( dependency.getGroupId(), dependency.getArtifactId(),
                                                                  vr, dependency.getType(), dependency.getClassifier(),
                                                                  dependency.getScope() );
            dependencyArtifacts.add( artifact );
        }
            
        if ( actTransitively )
        {
            try
            {
                ArtifactResolutionResult result = resolver.resolveTransitively( dependencyArtifacts, project.getArtifact(), remoteRepositories,
                                              localRepository, source );
                
                artifactMap = ArtifactUtils.artifactMapByVersionlessId( result.getArtifacts() );
            }
            catch ( ArtifactResolutionException e )
            {
                verbose( "Skipping: " + e.getArtifactId() + ". It cannot be resolved." );
            }
            catch ( ArtifactNotFoundException e )
            {
                verbose( "Skipping: " + e.getArtifactId() + ". It cannot be resolved." );
            }
        }
        else
        {
            artifactMap = new HashMap();
            for ( Iterator it = dependencyArtifacts.iterator(); it.hasNext(); )
            {
                Artifact artifact = (Artifact) it.next();
                
                try
                {
                    resolver.resolve( artifact, remoteRepositories, localRepository );

                    artifactMap.put( ArtifactUtils.versionlessKey( artifact ), artifact );
                }
                catch ( ArtifactResolutionException e )
                {
                    verbose( "Skipping: " + e.getArtifactId() + ". It cannot be resolved." );
                }
                catch ( ArtifactNotFoundException e )
                {
                    verbose( "Skipping: " + e.getArtifactId() + ". It cannot be resolved." );
                }
            }
        }
        
        return artifactMap;
    }

    private void verbose( String message )
    {
        if ( verbose )
        {
            getLog().info( message );
        }
    }

    private void refreshDependenciesForProject( MavenProject project )
        throws ArtifactResolutionException, MojoFailureException
    {
        Map deps = createArtifactMap( project );
        
        if ( deps.isEmpty() )
        {
            getLog().info( "Nothing to do for project: " + project.getId() );
            return;
        }

        if ( excludes != null && !excludes.isEmpty() )
        {
            for ( Iterator it = excludes.iterator(); it.hasNext(); )
            {
                String excludedKey = (String) it.next();

                verbose( "Excluding: " + excludedKey + " from refresh operation for project: " + project.getId() );
                
                deps.remove( excludedKey );
            }
        }
        List missingArtifacts = new ArrayList();
        for ( Iterator it = deps.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) it.next();

            Artifact artifact = (Artifact) entry.getValue();

            File deleteTarget = artifact.getFile();

            if ( scorchedEarthMode )
            {
                verbose( "Adjusting delete target for: " + artifact.getId() + " to incorporate entire cached project directory, due to active scorched earth mode." );
                deleteTarget = deleteTarget.getAbsoluteFile().getParentFile().getParentFile();
            }

            verbose( "Deleting: " + deleteTarget.getName() + " from local repository as part of the refresh operation for project: " + project.getId() );
            
            if ( deleteTarget.isDirectory() )
            {
                try
                {
                    FileUtils.deleteDirectory( deleteTarget );
                }
                catch ( IOException e )
                {
                    throw new MojoFailureException( this, "Cannot delete one or more cached dependencies.", "Failed to delete: " + deleteTarget );
                }
            }
            else
            {
                deleteTarget.delete();
            }
            
            if ( reResolve )
            {
                try
                {
                    resolver.resolve( artifact, project.getRemoteArtifactRepositories(), localRepository );
                }
                catch ( ArtifactResolutionException e )
                {
                    getLog().debug( e.getMessage() );
                    missingArtifacts.add( artifact );
                }
                catch ( ArtifactNotFoundException e )
                {
                    getLog().debug( e.getMessage() );
                    missingArtifacts.add( artifact );
                }
            }
        }

        if ( missingArtifacts.size() > 0 )
        {
            String message = "required artifacts missing:\n";
            for ( Iterator i = missingArtifacts.iterator(); i.hasNext(); )
            {
                Artifact missingArtifact = (Artifact) i.next();
                message += "  " + missingArtifact.getId() + "\n";
            }
            message += "\nfor the artifact:";

            throw new ArtifactResolutionException( message, project.getArtifact(), project
                .getRemoteArtifactRepositories() );
        }

    }

}
