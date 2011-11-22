package org.codehaus.mojo.projectArchive;

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.projectArchive.archive.ProjectArchiver;
import org.codehaus.mojo.projectArchive.files.Fileset;
import org.codehaus.mojo.tools.fs.archive.ArchiveFileExtensions;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;


public abstract class AbstractProjectArchiveMojo
    extends AbstractMojo
{

    public static final String ATTACHED_ARTIFACT_CLASSIFIER = "project-archive";

    private static final List DEFAULT_FILESETS;

    static
    {
        List filesets = new ArrayList();

        Fileset fs = new Fileset();

        fs.setDirectory( "${basedir}" );

        fs.addInclude( "pom.xml" );
        fs.addInclude( "src/**" );

        fs.addExclude( "target/**" );

        filesets.add( fs );

        DEFAULT_FILESETS = filesets;
    }

    /**
     * This is a list of org.codehaus.mojo.projectArchive.files.Fileset
     * instances, specified as:
     * <br/>
     * <pre>
     * <filesets>
     *   <fileset/>
     *   <fileset/>
     *   <fileset/>
     * </filesets>
     * </pre>
     * <br/>
     * It works a lot like the clean plugin, or the assembly plugin, to specify
     * a list of file patterns for inclusion/exclusion in the project archive.
     * 
     * @parameter
     */
    private List filesets = DEFAULT_FILESETS;
    
    /**
     * The root directory of files in the project archive.
     * 
     * @parameter expression="${archiveRootPath}" default-value="${project.artifactId}-${project.version}"
     * @required
     */
    private String archiveRootPath;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * This is the output file for the project archive. BE VERY CAREFUL IF YOU
     * DECIDE TO CHANGE THE FILE EXTENSION, as this could have implications for
     * the build-on-demand plugin and finding the project-source locations.
     * 
     * @parameter expression="${outputFile}" default-value="${project.build.directory}/${project.artifactId}-${project.version}-project-sources.zip"
     * @required
     */
    private File outputFile;

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @component
     */
    private ProjectArchiver projectArchiver;
    
    private Artifact artifact;
    
    protected AbstractProjectArchiveMojo()
    {
    }
    
    protected void createProjectArchive() throws MojoExecutionException
    {
        try
        {
            projectArchiver.create( project, outputFile, archiveRootPath, filesets, getLog() );
        }
        catch ( NoSuchArchiverException e )
        {
            throw new MojoExecutionException( "Could not find Archiver implementation to match output file: "
                + outputFile, e );
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "Failed to create project archive. Reason: " + e.getMessage(), e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to create project archive. Reason: " + e.getMessage(), e );
        }
    }

    protected Artifact getProjectArchiveArtifact()
    {
        if ( artifact == null )
        {
            File outputFile = getOutputFile();

            String type = ArchiveFileExtensions.getArchiveFileExtension( outputFile );

            artifact = artifactFactory.createArtifactWithClassifier( project.getGroupId(), project.getArtifactId(),
                                                                     project.getVersion(), type,
                                                                     ATTACHED_ARTIFACT_CLASSIFIER );

            artifact.setFile( outputFile );
            artifact.setResolved( true );
        }

        return artifact;
    }

    protected File getOutputFile()
    {
        return outputFile;
    }

    protected void setOutputFile( File outputFile )
    {
        this.outputFile = outputFile;
    }

    protected MavenProject getProject()
    {
        return project;
    }

    protected void setProject( MavenProject project )
    {
        this.project = project;
    }

    protected List getFilesets()
    {
        return filesets;
    }

    protected void setFilesets( List filesets )
    {
        this.filesets = filesets;
    }

    protected ProjectArchiver getProjectArchiver()
    {
        return projectArchiver;
    }

    protected void setProjectArchiver( ProjectArchiver projectArchiver )
    {
        this.projectArchiver = projectArchiver;
    }

}