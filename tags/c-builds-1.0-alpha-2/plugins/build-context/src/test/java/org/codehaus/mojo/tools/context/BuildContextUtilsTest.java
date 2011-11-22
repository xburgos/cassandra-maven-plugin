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

    public void testGetContextContainerMap()
    {
        // containerMap is initially null
        containerMap = BuildContextUtils.getContextContainerMap( CONTAINER_KEY, context, false );
        assertNull( containerMap );
        
        // containerMap is created
        containerMap = BuildContextUtils.getContextContainerMap( CONTAINER_KEY, context, true );
        assertNotNull( containerMap );
        
        // getContextContainerMap() returns the created and same containerMap
        Map otherContainerMap = BuildContextUtils.getContextContainerMap( CONTAINER_KEY, context, false );
        assertNotNull( otherContainerMap );
        assertEquals( otherContainerMap, containerMap );
    }

    public void testClearContextContainerMap()
    {
        // containerMap is initially null
        containerMap = BuildContextUtils.getContextContainerMap( CONTAINER_KEY, context, false );
        assertNull( containerMap );
        
        // containerMap is created
        containerMap = BuildContextUtils.getContextContainerMap( CONTAINER_KEY, context, true );
        assertNotNull( containerMap );
        
        // containerMap is empty but not null
        BuildContextUtils.clearContextContainerMap( CONTAINER_KEY, context );
        containerMap = BuildContextUtils.getContextContainerMap( CONTAINER_KEY, context, false );
        assertNotNull( containerMap );
        assertTrue( containerMap.isEmpty() );
    }

    public void testGetContextContainerMapForProject()
    {
        // projectContainerMap is initially null
        projectContainerMap = BuildContextUtils.getContextContainerMapForProject( CONTAINER_KEY, project, context, false );
        assertNull( projectContainerMap );
        
        // containerMap is initially null
        containerMap = BuildContextUtils.getContextContainerMap( CONTAINER_KEY, context, false );
        assertNull( containerMap );
        
        // projectContainerMap is created
        projectContainerMap = BuildContextUtils.getContextContainerMapForProject( CONTAINER_KEY, project, context, true );
        assertNotNull( projectContainerMap );
        
        // containerMap is created when projectContainerMap is created
        containerMap = BuildContextUtils.getContextContainerMap( CONTAINER_KEY, context, false );
        assertNotNull( containerMap );
        
        // containerMap contains the projectContainerMap of the project
        assertEquals( containerMap.get( project.getId() ), projectContainerMap );
    }

    public void testClearContextContainerMapForProject()
    {
        // projectContainerMap is initially null
        projectContainerMap = BuildContextUtils.getContextContainerMapForProject( CONTAINER_KEY, project, context, false );
        assertNull( projectContainerMap );
        
        // containerMap is initially null
        containerMap = BuildContextUtils.getContextContainerMap( CONTAINER_KEY, context, false );
        assertNull( containerMap );
        
        // projectContainerMap is created
        projectContainerMap = BuildContextUtils.getContextContainerMapForProject( CONTAINER_KEY, project, context, true );
        assertNotNull( projectContainerMap );
        
        // containerMap is created when projectContainerMap is created
        containerMap = BuildContextUtils.getContextContainerMap( CONTAINER_KEY, context, false );
        assertNotNull( containerMap );
        
        // containerMap contains the projectContainerMap of the project
        assertEquals( containerMap.get( project.getId() ), projectContainerMap );
        
        // projectContainerMap is empty but not null, 
        BuildContextUtils.clearContextContainerMapForProject( CONTAINER_KEY, project, context );
        projectContainerMap = BuildContextUtils.getContextContainerMapForProject( CONTAINER_KEY, project, context, false );
        assertNotNull( projectContainerMap );
        assertTrue( projectContainerMap.isEmpty() );
        
        // containerMap is not modified and it still contains the emptied projectContainerMap
        Map otherContainerMap = BuildContextUtils.getContextContainerMap( CONTAINER_KEY, context, false );
        assertNotNull( otherContainerMap );
        assertEquals( otherContainerMap, containerMap );
        assertEquals( containerMap.get( project.getId() ), projectContainerMap );
    }

}
