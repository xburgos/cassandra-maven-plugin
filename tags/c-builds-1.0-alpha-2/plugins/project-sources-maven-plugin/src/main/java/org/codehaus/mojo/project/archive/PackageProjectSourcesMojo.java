package org.codehaus.mojo.project.archive;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.assembly.AssemblerConfigurationSource;
import org.apache.maven.plugin.assembly.InvalidAssemblerConfigurationException;
import org.apache.maven.plugin.assembly.archive.ArchiveCreationException;
import org.apache.maven.plugin.assembly.archive.AssemblyArchiver;
import org.apache.maven.plugin.assembly.format.AssemblyFormattingException;
import org.apache.maven.plugin.assembly.model.Assembly;
import org.apache.maven.plugin.assembly.model.FileSet;
import org.codehaus.mojo.patch.PatchContext;
import org.codehaus.mojo.tools.fs.archive.ArchiveFileExtensions;

/**
 * Package up the current project's sources into an archive, skipping the patch directory and the
 * current build output directory (usually /target). Then, create an artifact using the base version
 * of the current project (without any release number), and assign the newly created project-sources
 * archive to the artifact's file attribute. If the project sources originated from a tarball or 
 * other archive, simply set the artifact's file to the original archive location.
 * 
 * @goal package-project-sources
 * @phase package
 * @author jdcasey
 *
 */
public class PackageProjectSourcesMojo
    extends AbstractProjectSourcesMojo
    implements AssemblerConfigurationSource
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
     * List of exclusion patterns (using Ant-style path expressions) to limit the contents of the
     * project-sources archive.
     * 
     * @parameter
     */
    private List excludes;
    
    /**
     * List of inclusion patterns (using Ant-style path expressions) to refine the contents of the
     * project-sources archive.
     * 
     * @parameter
     */
    private List includes;
    
    /**
     * Whether the patch directory in the project's working directory should be included in the 
     * project-sources artifact. Patches are normally handled separately, to enable successive 
     * releases based on the same source archive, but with different patch sets. Unless you
     * know what you're doing, this should probably be false (the default value).
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
     * Working directory for building up the contents of the project-sources archive.
     * 
     * @parameter default-value="${project.build.directory}/assembly-root"
     * @required
     */
    private File archiveTempRootDir;

    /**
     * Working directory for expanding/modifying any necessary files before they land in the 
     * project-sources archive.
     * 
     * @parameter default-value="${project.build.directory}/assembly-work-dir"
     * @required
     */
    private File archiveWorkDir;

    /**
     * Component used to create the project-sources archive.
     * 
     * @component
     */
    private AssemblyArchiver assemblyArchiver;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        ProjectSourceContext context = loadContext();

        if ( !context.isSourceArtifactResolved() )
        {
            File originalLocation = context.getOriginalProjectSourceLocation();
            
            if ( originalLocation == null )
            {
                throw new MojoExecutionException( "Original project-sources location not found. " +
                        "\nPlease ensure that the resolve-project-sources mojo is bound to the current lifecycle." );
            }

            Artifact sourceArtifact;
            if ( originalLocation.isDirectory() )
            {
                // if we get here, we know:
                // (a) that project-sources weren't resolved from the repository
                // (b) the project sources don't exist in some archive on localhost
                // ...which means we need to package them up into an archive.

                String assemblyBaseLocation = makeRelative( originalLocation );
                
                Assembly assembly = createAssemblyDescriptor( assemblyBaseLocation );
                sourceArtifact = createAssemblyArchive( assembly );
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

    private Artifact createAssemblyArchive( Assembly assembly )
        throws MojoExecutionException
    {
        String fileName = archiveFilePrefix + "-" + getSourceArtifactClassifier();
        
        File projectSourcesArchive;
        try
        {
            projectSourcesArchive = assemblyArchiver.createArchive( assembly, fileName, getSourceArtifactType(), this );
        }
        catch ( ArchiveCreationException e )
        {
            throw new MojoExecutionException( "Failed to create project-sources archive.", e );
        }
        catch ( AssemblyFormattingException e )
        {
            throw new MojoExecutionException( "Failed to create project-sources archive.", e );
        }
        catch ( InvalidAssemblerConfigurationException e )
        {
            throw new MojoExecutionException( "Failed to create project-sources archive.", e );
        }
        
        Artifact sourceArtifact = getProjectSourcesArtifact();
        sourceArtifact.setFile( projectSourcesArchive );
        
        return sourceArtifact;
    }

    private Assembly createAssemblyDescriptor( String assemblyBaseLocation )
    {
        Assembly assembly = new Assembly();
        assembly.addFormat( getSourceArtifactType() );
        assembly.setId( getSourceArtifactClassifier() );
        assembly.setIncludeBaseDirectory( false );
        assembly.setIncludeSiteDirectory( false );
        assembly.setBaseDirectory( "" );

        FileSet fs = new FileSet();
        fs.setDirectory( assemblyBaseLocation );
        fs.setUseDefaultExcludes( true );
        fs.addExclude( "target/**" );

        if ( !includePatchDirectory )
        {
            String patchExclude = getPatchExclude( assemblyBaseLocation );

            if ( patchExclude != null )
            {
                fs.addExclude( patchExclude );
            }
        }
        
        if ( excludes != null )
        {
            for ( Iterator it = excludes.iterator(); it.hasNext(); )
            {
                String exclude = (String) it.next();
                
                fs.addExclude( exclude );
            }
        }
        
        if ( includes != null )
        {
            for ( Iterator it = includes.iterator(); it.hasNext(); )
            {
                String include = (String) it.next();
                
                fs.addInclude( include );
            }
        }
        
        assembly.addFileSet( fs );
        
        return assembly;
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

    public File getArchiveBaseDirectory()
    {
        return null;
    }

    public File getBasedir()
    {
        return getProject().getFile().getParentFile();
    }

    public String getClassifier()
    {
        return getSourceArtifactClassifier();
    }

    public File getDescriptor()
    {
        return null;
    }

    public String getDescriptorId()
    {
        return null;
    }

    public String[] getDescriptorReferences()
    {
        return null;
    }

    public File getDescriptorSourceDirectory()
    {
        return null;
    }

    public File[] getDescriptors()
    {
        return null;
    }

    public List getFilters()
    {
        return null;
    }

    public String getFinalName()
    {
        return null;
    }

    public MavenArchiveConfiguration getJarArchiveConfiguration()
    {
        return null;
    }

    public File getOutputDirectory()
    {
        return outputDirectory;
    }

    public List getReactorProjects()
    {
        return Collections.singletonList( getProject() );
    }

    public List getRemoteRepositories()
    {
        return getRemoteRepositories();
    }

    public File getSiteDirectory()
    {
        return null;
    }

    public String getTarLongFileMode()
    {
        return tarLongFileMode;
    }

    public File getTemporaryRootDirectory()
    {
        return archiveTempRootDir;
    }

    public File getWorkingDirectory()
    {
        return archiveWorkDir;
    }

    public boolean isAssemblyIdAppended()
    {
        return true;
    }

    public boolean isSiteIncluded()
    {
        return false;
    }
    
}
