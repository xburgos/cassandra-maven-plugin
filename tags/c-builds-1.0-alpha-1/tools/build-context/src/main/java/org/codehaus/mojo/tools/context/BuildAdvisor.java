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

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.logging.AbstractLogEnabled;

public final class BuildAdvisor extends AbstractLogEnabled
{

    public static final String ROLE = BuildAdvisor.class.getName();

    public static final String CONTAINER_CONTEXT_KEY = "buildAdvisor-container";

    public static final String SKIP_BUILD_KEY = "build.advisor.skipBuild";

    public static void skipProjectBuild( MavenProject project, Context context )
    {
        Map containerMap =
            BuildContextUtils.getContextContainerMapForProject( CONTAINER_CONTEXT_KEY, project, context, true );
        
        containerMap.put( SKIP_BUILD_KEY, Boolean.TRUE );
    }

    public static boolean isProjectBuildSkipped( MavenProject project, Context context )
    {
        Map containerMap =
            BuildContextUtils.getContextContainerMapForProject( CONTAINER_CONTEXT_KEY, project, context, false );
        
        Boolean result = null;
        
        if ( containerMap != null )
        {
            result = (Boolean) containerMap.get( SKIP_BUILD_KEY );
        }

        boolean shouldSkip = ( Boolean.TRUE == result );

        return shouldSkip;
    }

    public static void purgeBuildContext( Context context )
    {
        BuildContextUtils.clearContextContainerMap( CONTAINER_CONTEXT_KEY, context );
    }

    public static void purgeProjectBuildContext( MavenProject project, Context context )
    {
        BuildContextUtils.clearContextContainerMapForProject( CONTAINER_CONTEXT_KEY, project, context );
    }

    protected static void setProjectBuildContextValue( MavenProject project, Context context, Object key, Object value )
       
    {
        Map projectBuildContext =
            BuildContextUtils.getContextContainerMapForProject( CONTAINER_CONTEXT_KEY, project, context, true );

        if ( projectBuildContext != null )
        {
            projectBuildContext.put( key, value );
        }
    }

    protected static Object getProjectBuildContextValue( MavenProject project, Context context, Object key,
                                                         boolean create )
    {
        Map projectBuildContext =
            BuildContextUtils.getContextContainerMapForProject( CONTAINER_CONTEXT_KEY, project, context, create );

        Object result = null;

        if ( projectBuildContext != null )
        {
            result = projectBuildContext.get( key );
        }

        return result;
    }
}
