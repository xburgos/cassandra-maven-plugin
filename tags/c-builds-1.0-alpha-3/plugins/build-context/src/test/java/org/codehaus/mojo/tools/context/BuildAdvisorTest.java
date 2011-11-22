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

import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.DefaultContext;

import junit.framework.TestCase;

/**
 * BuildAdvisorTest
 * 
 * @author <a href="mailto:nramirez@exist.com">Napoleon Esmundo C. Ramirez</a>
 */
public class BuildAdvisorTest
    extends TestCase
{
    private Context context;
    
    private MavenProject project;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        context = new DefaultContext();
        
        Model model = new Model();
        model.setGroupId( "groupId" );
        model.setArtifactId( "artifactId" );
        model.setVersion( "1.0" );
        
        project = new MavenProject( model );
    }
    
    public void testSkipProjectBuild_ShouldReturnTrueAfterSkippingBuild()
    {
        BuildAdvisor.skipProjectBuild( project, context );
        assertTrue( BuildAdvisor.isProjectBuildSkipped( project, context ) );
    }
    
    public void testIsProjectBuildSkipped_ShouldReturnFalseWhenProjectBuildNotSkipped()
    {
        assertFalse( BuildAdvisor.isProjectBuildSkipped( project, context ) );
    }
    
    public void testIsProjectBuildSkipped_ShouldReturnTrueWhenProjectBuildSkipped()
    {
        BuildAdvisor.skipProjectBuild( project, context );
        assertTrue( BuildAdvisor.isProjectBuildSkipped( project, context ) );
    }

    public void testPurgeBuildContext_ShouldReturnNullOrEmpty()
    {
        BuildContextUtils.getContextContainerMap( BuildAdvisor.CONTAINER_CONTEXT_KEY, context, true );
        
        BuildAdvisor.purgeBuildContext( context );
        
        Map containerMap = BuildContextUtils.getContextContainerMap( BuildAdvisor.CONTAINER_CONTEXT_KEY, context, false );
        
        assertTrue( containerMap == null || containerMap.isEmpty() );
    }

    public void testPurgeProjectBuildContext_ShouldReturnNullOrEmpty()
    {
        Map projectContainerMap = BuildContextUtils.getContextContainerMapForProject( BuildAdvisor.CONTAINER_CONTEXT_KEY, project, context, true );
        projectContainerMap.put( "key", "value" );
        assertTrue( projectContainerMap.size() > 0 );
        
        BuildAdvisor.purgeProjectBuildContext( project, context );
        
        projectContainerMap = BuildContextUtils.getContextContainerMapForProject( BuildAdvisor.CONTAINER_CONTEXT_KEY, project, context, false );
        
        assertTrue( projectContainerMap == null || projectContainerMap.isEmpty() );
    }

}
