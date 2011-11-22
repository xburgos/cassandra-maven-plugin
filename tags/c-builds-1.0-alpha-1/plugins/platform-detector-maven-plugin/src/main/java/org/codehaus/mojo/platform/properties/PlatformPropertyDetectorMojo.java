package org.codehaus.mojo.platform.properties;

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

import java.util.Properties;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.platform.PlatformDetectionException;
import org.codehaus.mojo.tools.platform.detective.PlatformDetective;
import org.codehaus.mojo.tools.platform.detective.PlatformPropertyPatterns;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

/**
 * Detect OS and architecture (and possibly others), then map these to name tokens
 * which can be used to construct RPM names, artifact classifiers, etc.
 * 
 * @goal detect
 * @phase initialize
 * @requiresProject false
 * 
 * @author jdcasey
 */
public class PlatformPropertyDetectorMojo extends AbstractMojo
    implements Contextualizable
{

    /**
     * @component role-hint="default"
     */
    private PlatformDetective detective;
    
    /**
     * @parameter
     */
    private PlatformPropertyPatterns platformPatterns;
    
    /**
     * @parameter
     */
    private String osProperty;
    
    /**
     * @parameter
     */
    private String archProperty;
    
    /**
     * @parameter default-value="true"
     */
    private boolean propertiesOnly;
    
    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    // contextualized
    private Context pluginContext;

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        try
        {
            if ( platformPatterns != null )
            {
                platformPatterns.saveToContext( pluginContext );
                
                getLog().debug( "Saved custom platform-property patterns to context: " + pluginContext );
            }
            
            if ( archProperty != null )
            {
                try
                {
                    String archToken = detective.getArchitectureToken();
                    
                    getLog().debug( "Injecting architecture token: {key: \'" + archProperty + "\', value: \'" + archToken + "\'}." );
                    
                    set( archProperty, archToken );
                }
                catch ( PlatformDetectionException e )
                {
                    throw new MojoExecutionException( "Error scanning for platform architecture information.", e );
                }
            }
            
            if ( osProperty != null )
            {
                try
                {
                    String osToken = detective.getOperatingSystemToken();
                    
                    getLog().debug( "Injecting OS token: {key: \'" + osProperty + "\', value: \'" + osToken + "\'}." );
                    
                    set( osProperty, osToken );
                }
                catch ( PlatformDetectionException e )
                {
                    throw new MojoExecutionException( "Error scanning for platform distribution information.", e );
                }
            }
        }
        finally
        {
            if ( propertiesOnly )
            {
                getLog().debug( "Removing platform property-mappings." );
                PlatformPropertyPatterns.deleteFromContext( pluginContext );
            }
            
            getLog().debug( "done." );
        }
    }

    private void set( String key, String value )
    {
        System.setProperty( key, value );
        
        Model model = project.getModel();
        
        Properties modelProps = model.getProperties();
        modelProps.setProperty( key, value );
        
        model.setProperties( modelProps );
    }

    public void contextualize( Context context ) throws ContextException
    {
        this.pluginContext = context;
    }

}
