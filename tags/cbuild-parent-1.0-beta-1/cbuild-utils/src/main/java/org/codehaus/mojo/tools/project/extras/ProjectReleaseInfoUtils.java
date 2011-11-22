package org.codehaus.mojo.tools.project.extras;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Object looks at version numbers and release numbers of a project
 */
public final class ProjectReleaseInfoUtils
{

    /**
     * pattern used to parse the version string
     */
    private static final String VERSION_WITH_RELEASE_PATTERN = ".+-[0-9]+";

    /**
     * Object constructor
     */
    private ProjectReleaseInfoUtils()
    {
    }

    /**
     * Splits a version string that includes a release number and returns the version part (from the beginning)
     * 
     * @param version maven project version which may include a rpm release number
     * @return the version of the project with the rpm release number stripped out
     */
    public static String getBaseVersion( String version )
    {
        String[] versionParts = splitVersionBaseAndRelease( version, false );
        
        return versionParts[0];
    }

    /**
     * Splits a version string that includes a release number and returns the release part (from the end)
     * 
     * @param version maven project version which may include a rpm release number
     * @return the rpm release number
     */
    public static String getReleaseNumber( String version )
    {
        String[] versionParts = splitVersionBaseAndRelease( version, true );
        
        return versionParts[1];
    }

    /**
     * Method will take a version and return a range complete with added release numbers
     * 
     * @param version maven project version which may include a rpm release number
     * @return string of version range
     */
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
            char lastChar = version.charAt( lastCharIdx );
            
            if ( Character.isDigit( lastChar ) )
            {
                // 1.2 -> [1.2-0, 1.3)
                int lastDigit = Integer.parseInt( version.substring( lastCharIdx ) );
                
                result = "[" + version + "," + version.substring( 0, lastCharIdx ) + ( lastDigit + 1 ) + ")";
            }
            else if ( Character.isLetter( lastChar ) )
            {
                result = "[" + version + "," + version.substring( 0, lastCharIdx ) + (char) ( lastChar + 1 ) + ")";
            }
        }

        if ( result == null )
        {
            result = version;
        }
        
        return result;
    }

    /**
     * Splits version and release number.
     * If no release number is in the version string and zeroIsDefaultReleaseNumber is specified,
     * a <code>0</code> will be returned as the release number, otherwise the release number will
     * be <code>null</code>.
     *  
     * @param version maven project version which may include a rpm release number
     * @param zeroIsDefaultReleaseNumber boolean value to return a zero if a release number is not present
     * @return array of two strings, one is the version, and the other is the rpm release number
     */
    public static String[] splitVersionBaseAndRelease( String version, boolean zeroIsDefaultReleaseNumber )
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
