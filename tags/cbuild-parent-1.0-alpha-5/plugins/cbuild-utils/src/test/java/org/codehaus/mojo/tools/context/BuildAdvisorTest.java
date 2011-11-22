package org.codehaus.mojo.tools.context;

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

import junit.framework.TestCase;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.context.DefaultContext;

/**
 * BuildAdvisorTest
 * 
 * @author <a href="mailto:nramirez@exist.com">Napoleon Esmundo C. Ramirez</a>
 */
public class BuildAdvisorTest extends PlexusTestCase {
    private static MavenSession session;
    private BuildAdvisor buildAdvisor;
    private MavenProject project;
    private static int testcnt = 0;

    @Before public void setUp() throws Exception {
        testcnt += 1;
        super.setUp();
        session = new MavenSession( this.getContainer(), null, null, null
            , null, null, null, null, null
        );
        buildAdvisor = (BuildAdvisor) lookup( BuildAdvisor.ROLE, BuildAdvisor.ROLE_HINT );    
        // Create a different project each time because maven caches old projects
        Model model = new Model();
        model.setGroupId( "groupId" );
        model.setArtifactId( "artifactId." + testcnt );
        model.setVersion( "1.0" );
        project = new MavenProject( model );
        // System.out.println(project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion());
        session.setCurrentProject(project);
    }
    
    @Test public void testIsProjectBuildSkipped_ShouldReturnFalseWhenProjectBuildAtDefault()
    {
        assertFalse( buildAdvisor.isProjectBuildSkipped( session ) );
    }
    
    @Test public void testSkipProjectBuild_ShouldReturnTrueAfterSkippingBuild()
    {
        buildAdvisor.skipProjectBuild( session );
        assertTrue( buildAdvisor.isProjectBuildSkipped( session ) );
    }
    
    @Test public void testIsProjectBuildSkipped_TestReEntrancy() throws Exception
    {
        assertFalse( buildAdvisor.isProjectBuildSkipped( session ) );
        // lwt - Check to make sure an old value is still there....
        BuildAdvisor myBA = (BuildAdvisor) lookup( BuildAdvisor.ROLE, BuildAdvisor.ROLE_HINT );    
        
        Model myModel = new Model();
        // Go back to an old stored project
        myModel.setGroupId( "groupId" );
        myModel.setArtifactId( "artifactId.2" );
        myModel.setVersion( "1.0" );
        MavenProject myProject = new MavenProject( myModel );
        session.setCurrentProject(myProject);
        assertTrue( myBA.isProjectBuildSkipped( session ) );
    }
    
    @Test public void testIsProjectBuildSkipped_ShouldReturnFalseWhenProjectBuildNotSkipped()
    {
        buildAdvisor.store( session, "is-project-build-skipped", Boolean.FALSE );
        assertFalse( buildAdvisor.isProjectBuildSkipped( session ) );
        buildAdvisor.store( session, "is-project-build-skipped", Boolean.TRUE );
        assertTrue( buildAdvisor.isProjectBuildSkipped( session ) );
    }

    @Test public void testIsProjectBuildSkipped_ShouldReturnTrueWhenProjectBuildSkipped()
    {
        buildAdvisor.skipProjectBuild( session );
        assertTrue( buildAdvisor.isProjectBuildSkipped( session ) );
    }

}
