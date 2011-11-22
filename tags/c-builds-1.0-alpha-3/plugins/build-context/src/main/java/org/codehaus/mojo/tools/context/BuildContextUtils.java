package org.codehaus.mojo.tools.context;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;

public final class BuildContextUtils
{

    private BuildContextUtils()
    {
    }

    public static Map getContextContainerMap( String containerKey, Context context, boolean create )
    {
        Map containerMap = null;

        if ( context.contains( containerKey ) )
        {
            try
            {
                containerMap = (Map) context.get( containerKey );
            }
            catch ( ContextException e )
            {
                throw new IllegalStateException( "Failed to retrieve BuildAdvisor "
                                + "serialization map from context, though the context claims it exists. Error: "
                                + e.getMessage() );
            }
        }
        else if ( create )
        {
            containerMap = new HashMap();
            context.put( containerKey, containerMap );
        }

        return containerMap;
    }

    public static Map getContextContainerMapForProject( String containerKey, MavenProject project, Context context,
                                                        boolean create )
    {
        Map containerMap = getContextContainerMap( containerKey, context, create );
        Map projectContainerMap = null;

        if ( containerMap != null )
        {
            projectContainerMap = (Map) containerMap.get( project.getId() );

            if ( create && projectContainerMap == null )
            {
                projectContainerMap = new HashMap();
                containerMap.put( project.getId(), projectContainerMap );
            }
        }

        return projectContainerMap;
    }

    public static void clearContextContainerMap( String containerKey, Context context )
    {
        Map containerMap = getContextContainerMap( containerKey, context, false );

        if ( containerMap != null )
        {
            containerMap.clear();
        }
    }

    public static void clearContextContainerMapForProject( String containerKey, MavenProject project, Context context )
    {
        Map containerMap = getContextContainerMapForProject( containerKey, project, context, false );

        if ( containerMap != null )
        {
            containerMap.clear();
        }
    }

}
