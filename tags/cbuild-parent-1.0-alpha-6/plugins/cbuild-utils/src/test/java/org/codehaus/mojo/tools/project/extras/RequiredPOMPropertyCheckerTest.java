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

import java.util.Collection;
import java.util.HashSet;

import junit.framework.TestCase;

import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;

/**
 * RequiredPOMPropertyCheckerTest
 * 
 * @author <a href="mailto:nramirez@exist.com">Napoleon Esmundo C. Ramirez</a>
 */
public class RequiredPOMPropertyCheckerTest
    extends TestCase
{
    private static final String POM_PROPERTY = "pom-property";
    
    private static final String ANOTHER_POM_PROPERTY = "another-pom-property";
    
    private static final Collection REQUIRED_POM_PROPERTIES = new HashSet();
    
    static
    {
        REQUIRED_POM_PROPERTIES.add( POM_PROPERTY );
        REQUIRED_POM_PROPERTIES.add( ANOTHER_POM_PROPERTY );
    }
    
    private MavenProject project;
    
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        Model model = new Model();
        model.setGroupId( "groupId" );
        model.setArtifactId( "artifactId" );
        model.setVersion( "1.0" );
        
        project = new MavenProject( model );
    }

    public void testCheckForRequiredPOMProperty_ShouldThrowRequiredPOMPropertyMissingExceptionWhenPOMPropertyIsMissing()
    {
        try
        {
            RequiredPOMPropertyChecker.checkForRequiredPOMProperty( project, POM_PROPERTY );
            
            fail( "Should throw RequiredPOMPropertyMissingException.  POM does not contain required property '" + POM_PROPERTY + "'" );
        }
        catch ( RequiredPOMPropertyMissingException e )
        {
        }
    }
    
    public void testCheckForRequiredPOMProperty_ShouldNotThrowRequiredPOMPropertyMissingExceptionWhenPOMPropertyIsPresent()
    {
        project.getProperties().put( POM_PROPERTY, POM_PROPERTY );
        
        try
        {
            RequiredPOMPropertyChecker.checkForRequiredPOMProperty( project, POM_PROPERTY );
        }
        catch ( RequiredPOMPropertyMissingException e )
        {
            fail( "Should not throw RequiredPOMPropertyMissingException.  POM contains required property '" + POM_PROPERTY + "'" );
        }
    }

    public void testCheckForRequiredPOMProperties_ShouldThrowRequiredPOMPropertyMissingExceptionWhenPOMPropertiesAreMissing()
    {
        try
        {
            RequiredPOMPropertyChecker.checkForRequiredPOMProperties( project, REQUIRED_POM_PROPERTIES );
            
            fail( "Should throw RequiredPOMPropertyMissingException.  POM does not contain any of the required properties '" + POM_PROPERTY + "' nor '" + ANOTHER_POM_PROPERTY + "'" );
        }
        catch ( RequiredPOMPropertyMissingException e )
        {
        }
    }
    
    public void testCheckForRequiredPOMProperties_ShouldNotThrowRequiredPOMPropertyMissingExceptionWhenPOMPropertiesArePresent()
    {
        project.getProperties().put( POM_PROPERTY, POM_PROPERTY );
        project.getProperties().put( ANOTHER_POM_PROPERTY, ANOTHER_POM_PROPERTY );
        
        try
        {
            RequiredPOMPropertyChecker.checkForRequiredPOMProperties( project, REQUIRED_POM_PROPERTIES );
        }
        catch ( RequiredPOMPropertyMissingException e )
        {
            fail( "Should not throw RequiredPOMPropertyMissingException.  POM contains all required properties '" + POM_PROPERTY + "' and '" + ANOTHER_POM_PROPERTY + "'" );
        }
    }

}
