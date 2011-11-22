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
 * BuildContextUtilsTest
 * 
 * @author <a href="mailto:nramirez@exist.com">Napoleon Esmundo C. Ramirez</a>
 */
public class BuildContextUtilsTest
    extends TestCase
{
    private static final String CONTAINER_KEY = "container-key";
    
    private Context context;
    
    private MavenProject project;
    
    private Map containerMap;
    
    private Map projectContainerMap;
    
    
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
        
        containerMap = null;
        
        projectContainerMap = null;
    }

    public void testGetContextContainerMap_ShouldReturnNullInitially()
    {
        containerMap = BuildContextUtils.getContextContainerMap( CONTAINER_KEY, context, false );
        assertNull( containerMap );
    }
    
    public void testGetContextContainerMap_ShouldReturnNotNullWhenCreated()
    {
        containerMap = BuildContextUtils.getContextContainerMap( CONTAINER_KEY, context, true );
        assertNotNull( containerMap );
    }
    
    public void testGetContextContainerMap_ShouldReturnCreatedContextContainerMap()
    {
        containerMap = BuildContextUtils.getContextContainerMap( CONTAINER_KEY, context, true );
        Map otherContainerMap = BuildContextUtils.getContextContainerMap( CONTAINER_KEY, context, false );
        
        assertEquals( otherContainerMap, containerMap );
    }

    public void testClearContextContainerMap_ShouldReturnEmptyContextContainerMap()
    {
        containerMap = BuildContextUtils.getContextContainerMap( CONTAINER_KEY, context, true );
        
        BuildContextUtils.clearContextContainerMap( CONTAINER_KEY, context );
        
        containerMap = BuildContextUtils.getContextContainerMap( CONTAINER_KEY, context, false );
        
        assertTrue( containerMap.isEmpty() );
    }

    public void testGetContextContainerMapForProject_ShouldReturnNullInitially()
    {
        projectContainerMap = BuildContextUtils.getContextContainerMapForProject( CONTAINER_KEY, project, context, false );
        assertNull( projectContainerMap );
    }
    
    public void testGetContextContainerMapForProject_ShouldReturnNotNullWhenCreated()
    {
        projectContainerMap = BuildContextUtils.getContextContainerMapForProject( CONTAINER_KEY, project, context, true );
        assertNotNull( projectContainerMap );
    }
    
    public void testGetContextContainerMapForProject_ShouldReturnCreatedContextContainerMapForProject()
    {
        projectContainerMap = BuildContextUtils.getContextContainerMapForProject( CONTAINER_KEY, project, context, true );
        Map otherProjectContainerMap = BuildContextUtils.getContextContainerMapForProject( CONTAINER_KEY, project, context, false );
        
        assertEquals( otherProjectContainerMap, projectContainerMap );
    }
    
    public void testClearContextContainerMapForProject_ShouldReturnEmptyContextContainerMapForProject()
    {
        projectContainerMap = BuildContextUtils.getContextContainerMapForProject( CONTAINER_KEY, project, context, true );
        projectContainerMap.put( "key", "value" );
        assertFalse( projectContainerMap.isEmpty() );
        
        BuildContextUtils.clearContextContainerMapForProject( CONTAINER_KEY, project, context );
        
        projectContainerMap = BuildContextUtils.getContextContainerMapForProject( CONTAINER_KEY, project, context, false );
        
        assertTrue( projectContainerMap.isEmpty() );
    }

}
