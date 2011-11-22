package org.codehaus.mojo.tools.project.extras;

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

/**
 * ProjectReleaseInfoUtilsTest
 * 
 * @author <a href="mailto:nramirez@exist.com">Napoleon Esmundo C. Ramirez</a>
 */
public class ProjectReleaseInfoUtilsTest
    extends TestCase
{
    private static final String[] originalVersions = { "1", "1-1", "1.0", "1.0-1", "1.0.0", "1.0.0-1" };
    
    private static final String[] expectedVersions = { "1", "1", "1.0", "1.0", "1.0.0", "1.0.0" };

    private static final String[] expectedReleaseNumbers = { "0", "1", "0", "1", "0", "1" };
    
    private static final String[] impliedVersionRanges = { "[1,2)", "1-1", "[1.0,1.1)", "1.0-1", "[1.0.0,1.0.1)", "1.0.0-1" };
    
    public void testGetBaseVersion_ShouldReturnVersionStringWithoutBuildNumber()
    {
        for ( int i = 0; i < originalVersions.length; i++ )
        {
            String failureMessage = "Failed to retrieve base version: \'" + expectedVersions[i] + "\' from: \'" + originalVersions[i] + "\'.";
            
            assertEquals( failureMessage, expectedVersions[i],  ProjectReleaseInfoUtils.getBaseVersion( originalVersions[i] ) );
        }
    }

    public void testGetBaseVersion_ShouldReturnReleaseNumberWithoutBaseVersionAndAssumeZeroIfMissing()
    {
        for ( int i = 0; i < originalVersions.length; i++ )
        {
            String failureMessage = "Failed to retrieve release number: \'" + expectedReleaseNumbers[i] + "\' from: \'" + originalVersions[i] + "\'.";
            
            assertEquals( failureMessage, expectedReleaseNumbers[i], ProjectReleaseInfoUtils.getReleaseNumber( originalVersions[i] ) );
        }
    }

    public void testFormatImpliedReleaseNumberVersionRanges()
    {
        for ( int i = 0; i < originalVersions.length; i++ )
        {
            String failureMessage = "Failed to format version range: \'" + impliedVersionRanges[i] + "\' from version: \'" + originalVersions[i] + "\'.";
            
            assertEquals( failureMessage, impliedVersionRanges[i], ProjectReleaseInfoUtils.formatImpliedReleaseNumberVersionRanges( originalVersions[i] ) );
        }
    }

}
