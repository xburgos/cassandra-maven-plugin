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
package org.codehaus.mojo.minijar;


import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.maven.plugin.MojoExecutionException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.vafer.dependency.Console;
import org.vafer.dependency.asm.RenamingVisitor;
import org.vafer.dependency.utils.ResourceMatcher;
import org.vafer.dependency.utils.ResourceRenamer;
import org.codehaus.mojo.minijar.resource.MinijarResourceMatcher;
import org.codehaus.mojo.minijar.resource.ResourceTransformer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;


public final class JarUtils
{

    // TODO: add a class to the output jar and wrap calls to getResource() and getResourceStream() for runtime renaming of resources
    public static boolean combineJars( final File[] pInputJars,
                                       final MinijarResourceMatcher[] pMatchers,
                                       final ResourceRenamer[] pRenamers,
                                       final File pOutputJar,
                                       final Collection transformers )
        throws IOException
    {

        Console pConsole = null;

        boolean changed = false;

        final JarInputStream[] inputStreams = new JarInputStream[pInputJars.length];
        for ( int i = 0; i < inputStreams.length; i++ )
        {
            inputStreams[i] = new JarInputStream( new FileInputStream( pInputJars[i] ) );
        }

        final JarOutputStream outputJar = new JarOutputStream( new FileOutputStream( pOutputJar ) );

        for ( int i = 0; i < inputStreams.length; i++ )
        {
            final JarInputStream inputStream = inputStreams[i];
            while ( true )
            {
                final JarEntry entry = inputStream.getNextJarEntry();

                if ( entry == null )
                {
                    break;
                }

                if ( entry.isDirectory() )
                {
                    // ignore directory entries
                    if ( pConsole != null )
                    {
                        pConsole.println( "removing directory entry " + entry );
                    }
                    IOUtils.copy( inputStream, new NullOutputStream() );
                    continue;
                }

                final String oldName = entry.getName();

                if ( pMatchers[i].keepResourceWithName( oldName, inputStream ) )
                {
                    final String newName = pRenamers[i].getNewNameFor( oldName );

                    if ( newName.equals( oldName ) )
                    {
                        if ( pConsole != null )
                        {
                            pConsole.println( "keeping original resource " + oldName );
                        }

                        outputJar.putNextEntry( new JarEntry( newName ) );
                        IOUtils.copy( inputStream, outputJar );
                    }
                    else
                    {
                        if ( pConsole != null )
                        {
                            pConsole.println( "renaming resource " + oldName + "->" + newName );
                        }
                        changed = true;

                        outputJar.putNextEntry( new JarEntry( newName ) );
                        if ( oldName.endsWith( ".class" ) )
                        {
                            final byte[] oldClassBytes = IOUtils.toByteArray( inputStream );

                            if ( pConsole != null )
                            {
                                pConsole.println( "adjusting class " + oldName + "->" + newName );
                            }

                            final ClassReader r = new ClassReader( oldClassBytes );
                            final ClassWriter w = new ClassWriter( true );
                            r.accept( new RenamingVisitor( w, pRenamers[i] ), false );

                            final byte[] newClassBytes = w.toByteArray();
                            IOUtils.copy( new ByteArrayInputStream( newClassBytes ), outputJar );
                        }
                        else
                        {
                            IOUtils.copy( inputStream, outputJar );
                        }
                    }


                }
                else
                {
                    changed = true;
                    if ( pConsole != null )
                    {
                        pConsole.println( "removing resource " + oldName );
                    }

                    IOUtils.copy( inputStream, new NullOutputStream() );
                }
            }
            inputStream.close();
        }

        for ( Iterator i = transformers.iterator(); i.hasNext(); )
        {
            ResourceTransformer transformer = (ResourceTransformer) i.next();

            if ( transformer.hasTransformedResource() )
            {
                File file = transformer.getTransformedResource();

                Reader reader = new FileReader( file );

                outputJar.putNextEntry( new JarEntry( "META-INF/plexus/components.xml" ) );

                IOUtils.copy( reader, outputJar );

                reader.close();
            }
        }

        outputJar.close();

        return changed;
    }

}
