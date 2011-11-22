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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.mojo.tools.fs.archive.ArchiveFileExtensions;


/**
 * Assembles a project archive from the current project, and attaches it for
 * installation or deployment.
 * <br/>
 * Project archives consist of everything needed to build a project, rather than
 * just the source files. In particular, they preserve the working directory
 * layout, and include the POM for the project.
 *
 * @goal create
 * 
 * @phase package
 */
public class CreateProjectArchiveMojo
    extends AbstractProjectArchiveMojo
{
    /**
     * @component
     */
    private MavenProjectHelper projectHelper;

    /**
     * @parameter default-value="${project.packaging}"
     * @required
     * @readonly
     */
    private String packaging;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( "pom".equals( packaging ) )
        {
            getLog().info( "Skipping project-archive operations for POM project." );
            return;
        }
        
        createProjectArchive();

        MavenProject project = getProject();

        File outputFile = getOutputFile();

        String type = ArchiveFileExtensions.getArchiveFileExtension( outputFile );

        projectHelper.attachArtifact( project, type, ATTACHED_ARTIFACT_CLASSIFIER, outputFile );
    }

}
