package org.codehaus.mojo.minijar;

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
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
 */


import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.mojo.minijar.resource.MinijarResourceMatcher;
import org.codehaus.mojo.minijar.resource.NoopResourceTransformer;
import org.vafer.dependency.ClazzpathUnit;
import org.vafer.dependency.utils.ResourceRenamer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.Collections;
import java.util.zip.ZipException;


/**
 * Creates stripped down versions of the dependencies.
 * 
 * @goal minijars
 * @requiresDependencyResolution compile
 * @execute phase="package"
 */
public final class MiniJarsMojo
    extends AbstractPluginMojo
{

    /**
     * Creates individual stripped jars of the dependencies
     * @param remove Set of classes that are supposed to be removed
     * @throws MojoExecutionException on error
     */
    private void createMiniJars( final Set remove )
        throws MojoExecutionException    
    {
        Collection transformers = new ArrayList();

        transformers.add( new NoopResourceTransformer() );
        
        final MinijarResourceMatcher matcher = createMatcher( remove, transformers );
        
        final ResourceRenamer renamer = new ResourceRenamer()
        {
            public String getNewNameFor( final String pResourceName )
            {
                return pResourceName;
            }                        
        };

        
        final ClazzpathUnit[] units = clazzpath.getUnits();
        for ( int i = 0; i < units.length; i++ )
        {
            ClazzpathUnit unit = units[i];
            
            final File inputJar = unit.getFile();
            
            // TODO: make output file name construction configurable
            final String oldName = inputJar.getName();
            final String newName = oldName.substring( 0, oldName.lastIndexOf( '.' ) ) + "-mini.jar";

            final File outputJar = new File( buildDirectory, newName );
            
            try
            {
                JarUtils.combineJars(
                        new File[] { inputJar },
                        new MinijarResourceMatcher[] { matcher },
                        new ResourceRenamer[] { renamer },
                        outputJar, Collections.EMPTY_SET
                        );
            }
            catch ( ZipException ze )
            {
                getLog().info( "No references to jar " + inputJar.getName() + ". You can safely omit that dependency." );
                
                if ( outputJar.exists() )
                {
                    outputJar.delete();
                }
                continue;
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Could not create mini jar " + outputJar, e );
            }
            
            getLog().info( "Original length of " + inputJar.getName() + " was " + inputJar.length() + " bytes. " + "Was able shrink it to " + outputJar.getName() + " at " + outputJar.length() + " bytes (" + (int) ( 100 * outputJar.length() / inputJar.length() ) + "%)" );
        }
    }
    
    /**
     * Main entry point
     * @throws MojoExecutionException on error
     */
    public void execute()
        throws MojoExecutionException
    {
        final Set remove;
        
        getLog().info( "Calculating transitive hull of dependencies." );
        
        try
        {
            remove = getNotRequiredClazzes();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not analyse classpath dependencies", e );
        }

        // create minimal versions of every jar dependency
        createMiniJars( remove );
    }
}
