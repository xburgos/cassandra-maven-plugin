package org.codehaus.mojo.rpm;

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

import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.lang.reflect.Field;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * @author lecarma
 *
 * @goal remove-project-references
 * @phase initialize
 * @aggregator
 */
public class RemoveProjectRef
    extends AbstractMojo
{
    /**
     * MavenProject instance used to furnish information required to construct the RPM name in the
     * event the rpmName parameter is not specified.
     *
     * @parameter expression="${reactorProjects}"
     * @required
     * @readonly
     */
    private List projects;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {

        for ( Iterator it = projects.iterator(); it.hasNext(); )
        {
            MavenProject project = (MavenProject) it.next();
            
            try
            {
                Field field = MavenProject.class.getDeclaredField( "projectReferences" );
                field.setAccessible( true );
                field.set( project, Collections.EMPTY_MAP );
            }
            catch( Exception e )
            {
                throw new MojoExecutionException( "Failed to remove project references for: " + project, e );
            }
        }
    }
}
