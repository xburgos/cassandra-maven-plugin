package org.codehaus.mojo.make;

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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.project.extras.RequiredPOMPropertyChecker;
import org.codehaus.mojo.tools.project.extras.RequiredPOMPropertyMissingException;


/**
 * Validate that the required project property 'prefix' is available in the
 * current project before allowing it to continue. This property is required
 * to allow @pathOf(..)@ style references work, as in the make plugin's mojos.
 * 
 * @requiresProject true
 * @goal validate-pom
 * @phase validate
 */
public class CheckInstallPrefixMojo
    extends AbstractMojo
{
    
    public static final String MAKE_INSTALL_PREFIX = "prefix";
    
    /**
     * Project instance to validate before building the RPM.
     * 
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            RequiredPOMPropertyChecker.checkForRequiredPOMProperty( project, MAKE_INSTALL_PREFIX );
//            
//            String prefixRoot = System.getProperty( "prefixRoot" );
//            if( prefixRoot != null ){
//                project.getProperties().setProperty( "prefixRoot", prefixRoot );
//            }
//
//            System.out.println( project.getGroupId()+":"+project.getArtifactId()+"===========================*+*>"+prefixRoot );
        }
        catch ( RequiredPOMPropertyMissingException e )
        {
            throw new MojoExecutionException( "Invalid RPM Project. Reason: " + e.getLongMessage(), e );
        }
    }

}
