package org.codehaus.mojo.tools.project.extras;

public final class ProjectReleaseInfoUtils
{
    
    private static final String VERSION_WITH_RELEASE_PATTERN = ".+-[0-9]+";

    private ProjectReleaseInfoUtils()
    {
        
    }
    
    public static String getBaseVersion( String version )
    {
        if ( version.matches( VERSION_WITH_RELEASE_PATTERN ) )
        {
            int lastIdx = version.lastIndexOf( '-' );
            
            return version.substring( 0, lastIdx );
        }
        else
        {
            return version;
        }
    }

}
