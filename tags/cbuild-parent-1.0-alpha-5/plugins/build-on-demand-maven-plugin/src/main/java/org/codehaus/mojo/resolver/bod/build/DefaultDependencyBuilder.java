package org.codehaus.mojo.resolver.bod.build;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.io.logging.DefaultMessageHolder;
import org.apache.maven.shared.io.logging.MessageHolder;
import org.codehaus.mojo.resolver.bod.BuildOnDemandResolutionException;
import org.codehaus.mojo.resolver.bod.BuildOnDemandResolutionRequest;
import org.codehaus.mojo.resolver.bod.pom.rewrite.PomRewriteConfiguration;
import org.codehaus.mojo.resolver.bod.pom.rewrite.PomRewriter;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * @plexus.component role="org.codehaus.mojo.resolver.bod.build.DependencyBuilder" role-hint="default"
 * @author jdcasey
 */
public class DefaultDependencyBuilder
    implements DependencyBuilder
{

    /**
     * @plexus.requirement
     */
    private Invoker invoker;

    /**
     * @plexus.requirement role-hint="default"
     */
    PomRewriter rewriter;

    /**
     * @plexus.requirement
     */
    ArtifactFactory artifactFactory;
    
    public DefaultDependencyBuilder()
    {
        // needed for plexus instantiation.
    }

    public DefaultDependencyBuilder( Invoker invoker, PomRewriter rewriter, ArtifactFactory artifactFactory )
    {
        this.invoker = invoker;
        this.rewriter = rewriter;
        this.artifactFactory = artifactFactory;
    }

    public void buildDependencies( List dependencyProjects, Set completedBuilds, BuildOnDemandResolutionRequest request )
        throws BuildOnDemandResolutionException
    {
        MessageHolder errors = new DefaultMessageHolder();

        PomRewriteConfiguration rewriteConfig = request.getPomRewriteConfiguration();

        if ( rewriteConfig == null )
        {
            rewriteConfig = new PomRewriteConfiguration();
        }

        BuildConfiguration prototypeConfig = request.getBuildPrototype();

        // TODO: unit test when provided build prototype config is null.
        if ( prototypeConfig == null )
        {
            prototypeConfig = new BuildConfiguration();
        }

        File projectsDirectory = request.getProjectsDirectory();

        ArtifactRepository localRepository = request.getLocalRepository();

        for ( Iterator it = dependencyProjects.iterator(); it.hasNext(); )
        {
            MavenProject dependencyProject = (MavenProject) it.next();

            File projectDir =
                new File( projectsDirectory, dependencyProject.getArtifactId() + "-" + dependencyProject.getVersion() );

            projectDir.mkdirs();

            BuildConfiguration config = prototypeConfig.copy();
            config.setBaseDirectory( projectDir );

            Artifact pomArtifact =
                artifactFactory.createProjectArtifact( dependencyProject.getGroupId(),
                                                       dependencyProject.getArtifactId(),
                                                       dependencyProject.getVersion() );

            String relativePath = localRepository.pathOf( pomArtifact );
            File pomSourceFile = new File( localRepository.getBasedir(), relativePath );

            String pomFileName = config.getPomFileName();

            if ( pomFileName == null )
            {
                pomFileName = "pom.xml";
            }

            File pomFile = new File( projectDir, pomFileName );

            try
            {
                FileUtils.copyFile( pomSourceFile, pomFile );
            }
            catch ( IOException e )
            {
                errors.addErrorMessage( "Failed to copy pom: " + pomSourceFile + " to: " + projectDir + ". Error: "
                                + e.getMessage() );
            }

            rewriter.rewriteOnDisk( pomFile, rewriteConfig, errors );

            if ( buildProject( dependencyProject.getId(), projectDir, config, errors ) )
            {
                String key =
                    ArtifactUtils.versionlessKey( dependencyProject.getGroupId(), dependencyProject.getArtifactId() );

                completedBuilds.add( key );
                
                // remove the project from the dependencyProjects list, so we can track those that 
                // didn't build later on...
                it.remove();
            }
        }

        if ( !errors.isEmpty() )
        {
            throw new BuildOnDemandResolutionException( "While building missing dependencies:\n\n" + errors.render() );
        }
    }

    protected boolean buildProject( String projectId, File projectDir, BuildConfiguration configuration,
                                    MessageHolder errors )
    {
        InvocationResult result;
        try
        {
            result = invoker.execute( configuration );
        }
        catch ( MavenInvocationException e )
        {
            errors.addMessage( "Failed to build project: " + projectId, e );

            return false;
        }

        CommandLineException executionException = result.getExecutionException();
        if ( executionException != null )
        {
            errors.addMessage( "Failed invoke Maven to build project: " + projectId, executionException );

            return false;
        }
        else if ( result.getExitCode() != 0 )
        {
            errors.addMessage( "Build for project: " + projectId + " failed; returned exit code: "
                            + result.getExitCode() );
            return false;
        }
        else
        {
            return true;
        }
    }
}
