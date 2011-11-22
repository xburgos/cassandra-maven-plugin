package org.codehaus.mojo.project.archive;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.patch.PatchContext;
import org.codehaus.mojo.tools.fs.archive.ArchiveFileExtensions;
import org.codehaus.mojo.tools.fs.archive.ArchiverManagerUtils;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.util.FileUtils;

/**
 * Package up the current project's sources into an archive, skipping the patch directory and the current build output
 * directory (usually /target). Then, create an artifact using the base version of the current project (without any
 * release number), and assign the newly created project-sources archive to the artifact's file attribute. If the
 * project sources originated from a tarball or other archive, simply set the artifact's file to the original archive
 * location.
 * 
 * @goal package-project-sources
 * @phase package
 * @author jdcasey
 * 
 */
public class PackageProjectSourcesMojo
    extends AbstractProjectSourcesMojo
{

    /**
     * This is the normal final name of the project, usually ${artifactId}-${version}.ext
     * 
     * @parameter default-value="${project.artifactId}-${project.version}"
     * @required
     * @readonly
     */
    private String archiveFilePrefix;

    /**
     * List of exclusion patterns (using Ant-style path expressions) to limit the contents of the project-sources
     * archive.
     * 
     * @parameter
     */
    private List excludes;

    /**
     * List of inclusion patterns (using Ant-style path expressions) to refine the contents of the project-sources
     * archive.
     * 
     * @parameter
     */
    private List includes;

    /**
     * Whether the patch directory in the project's working directory should be included in the project-sources
     * artifact. Patches are normally handled separately, to enable successive releases based on the same source
     * archive, but with different patch sets. Unless you know what you're doing, this should probably be false (the
     * default value).
     * 
     * @parameter default-value="false"
     */
    private boolean includePatchDirectory;

    /**
     * This is the target directory in which the project-sources archive should be created.
     * 
     * @parameter default-value="${project.build.directory}"
     * @required
     * @readonly
     */
    private File outputDirectory;

    /**
     * Handling mode for long file paths.
     * 
     * @parameter default-value="gnu"
     */
    private String tarLongFileMode;

    /**
     * Component used to create the project-sources archive.
     * 
     * @component
     */
    private ArchiverManager archiverManager;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        ProjectSourceContext context = loadContext();

        if ( !context.isSourceArtifactResolved() )
        {
            File originalLocation = context.getOriginalProjectSourceLocation();

            if ( originalLocation == null )
            {
                throw new MojoExecutionException(
                                                  "Original project-sources location not found. "
                                                                  + "\nPlease ensure that the resolve-project-sources mojo is bound to the current lifecycle." );
            }

            Artifact sourceArtifact;
            if ( originalLocation.isDirectory() )
            {
                // if we get here, we know:
                // (a) that project-sources weren't resolved from the repository
                // (b) the project sources don't exist in some archive on localhost
                // ...which means we need to package them up into an archive.
                sourceArtifact = createArchive( originalLocation );
            }
            else
            {
                String ext = ArchiveFileExtensions.getArchiveFileExtension( originalLocation );

                sourceArtifact = getProjectSourcesArtifact( ext );
                sourceArtifact.setFile( originalLocation );
            }

            context.setProjectSourceArtifact( sourceArtifact );
            storeContext( context );
        }
    }

    private Artifact createArchive( File archiveBasedir )
        throws MojoExecutionException
    {
        String assemblyBaseLocation = makeRelative( archiveBasedir );
        
        List archiveExcludes = new ArrayList();
        archiveExcludes.addAll( FileUtils.getDefaultExcludesAsList() );
        archiveExcludes.add( "target/**" );

        if ( !includePatchDirectory )
        {
            String patchExclude = getPatchExclude( assemblyBaseLocation );

            if ( patchExclude != null )
            {
                archiveExcludes.add( patchExclude );
            }
        }

        if ( excludes != null )
        {
            for ( Iterator it = excludes.iterator(); it.hasNext(); )
            {
                String exclude = (String) it.next();

                archiveExcludes.add( exclude );
            }
        }

        List archiveIncludes = new ArrayList();

        if ( includes != null )
        {
            for ( Iterator it = includes.iterator(); it.hasNext(); )
            {
                String include = (String) it.next();

                archiveIncludes.add( include );
            }
        }

        String fileName = archiveFilePrefix + "-" + getSourceArtifactClassifier();
        
        File projectSourcesArchive = new File( outputDirectory, fileName + getSourceArtifactType() );
        try
        {
            Map options = Collections.singletonMap( ArchiverManagerUtils.TAR_LONG_FILE_MODE_OPTION, tarLongFileMode );
            
            Archiver archiver = ArchiverManagerUtils.getArchiver( archiverManager, getSourceArtifactType(), options );
            
            archiver.setDestFile( projectSourcesArchive );

            String[] includeArry = (String[]) archiveIncludes.toArray( new String[archiveIncludes.size()] );
            String[] excludeArry = (String[]) archiveExcludes.toArray( new String[archiveExcludes.size()] );

            archiver.addDirectory( archiveBasedir, includeArry, excludeArry );

            archiver.createArchive();
        }
        catch ( NoSuchArchiverException e )
        {
            throw new MojoExecutionException( "Failed to lookup archiver for type: " + getSourceArtifactType(), e );
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "Failed to create project-sources archive.", e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to create project-sources archive.", e );
        }

        Artifact sourceArtifact = getProjectSourcesArtifact();
        sourceArtifact.setFile( projectSourcesArchive );

        return sourceArtifact;
    }

    private String makeRelative( File originalLocation )
    {
        File basedir = getProject().getFile().getParentFile();

        String basedirPath = basedir.getAbsolutePath().replace( '\\', '/' );
        String origPath = originalLocation.getAbsolutePath().replace( '\\', '/' );

        if ( origPath.startsWith( basedirPath ) )
        {
            String result = origPath.substring( basedirPath.length() );

            if ( result.startsWith( "/" ) )
            {
                result = result.substring( 1 );
            }

            return result;
        }

        return origPath;
    }

    private String getPatchExclude( String assemblyBaseLocation )
    {
        PatchContext patchContext = PatchContext.read( getSessionContext(), getProject() );

        if ( patchContext != null && !patchContext.isPatchArtifactResolved() )
        {
            // path-separator changes have already been handled in makeRelative(..) above.
            String originalPath = assemblyBaseLocation;

            String patchDir = patchContext.getPatchDirectory().getAbsolutePath();
            patchDir.replace( '\\', '/' );

            String patchExcludeBase = patchContext.getPatchDirectory().getPath();

            if ( patchDir.startsWith( originalPath ) )
            {
                patchExcludeBase = patchDir.substring( originalPath.length() );

                if ( patchExcludeBase.startsWith( "/" ) )
                {
                    patchExcludeBase = patchExcludeBase.substring( 1 );
                }
            }

            return patchExcludeBase + "/**";
        }

        return null;
    }

}
