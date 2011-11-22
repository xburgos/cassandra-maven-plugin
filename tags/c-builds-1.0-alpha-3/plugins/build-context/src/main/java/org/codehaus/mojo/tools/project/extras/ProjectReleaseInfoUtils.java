package org.codehaus.mojo.tools.project.extras;

public final class ProjectReleaseInfoUtils
{
    
    private static final String VERSION_WITH_RELEASE_PATTERN = ".+-[0-9]+";

    private ProjectReleaseInfoUtils()
    {
        
    }
    
    public static String getBaseVersion( String version )
    {
        String[] versionParts = splitVersionBaseAndRelease( version, false );
        
        return versionParts[0];
    }
    
    public static String getReleaseNumber( String version )
    {
        String[] versionParts = splitVersionBaseAndRelease( version, true );
        
        return versionParts[1];
    }
    
    public static String formatImpliedReleaseNumberVersionRanges( String version )
    {
        // make sure we're dealing with a real version here...if not, just pass back the original
        // with no fuss.
        if ( version == null || version.length() < 1 )
        {
            return version;
        }
        
        // TODO: What if the passed-in version string is already a range??
        // We need to make it release-number-aware in that case.
        String[] versionParts = splitVersionBaseAndRelease( version, false );
        
        String result = null;
        
        // if the second part is null, there was no release number supplied.
        if ( versionParts[1] == null )
        {
            int lastCharIdx = version.length() - 1;
            
            // we can only calculate the implied range if we know how to increment the last part
            // of the version.
            //
            // Current cases where this is NOT possible:
            // 1. if the original version is already a range
            // 2. if the original version ends in an alpha character, as in 1.0-beta
            //
            // If we can't increment, just leave things alone here, and we'll pass back the original
            // version later in this method.
            //
            // TODO: If the version is 1.0-beta-1, and there IS no 1.0-beta-2, what will Maven do?
            if ( Character.isDigit( version.charAt( lastCharIdx ) ) )
            {
                // 1.2 -> [1.2-0, 1.3)
                int lastDigit = Integer.parseInt( version.substring( lastCharIdx ) );
                
                result = "[" + version + "," + version.substring( 0, lastCharIdx ) + (lastDigit + 1) + ")";
            }
        }

        if ( result == null)
        {
            result = version;
        }
        
        return result;
    }
    
    
    private static String[] splitVersionBaseAndRelease( String version, boolean zeroIsDefaultReleaseNumber )
    {
        String[] result = new String[2];
        
        if ( version.matches( VERSION_WITH_RELEASE_PATTERN ) )
        {
            int lastIdx = version.lastIndexOf( '-' );
            
            result[0] = version.substring( 0, lastIdx );
            result[1] = version.substring( lastIdx + 1 );
        }
        else
        {
            result[0] = version;
            
            if ( zeroIsDefaultReleaseNumber )
            {
                result[1] = "0";
            }
            else
            {
                result[1] = null;
            }
        }
        
        return result;
    }

}
