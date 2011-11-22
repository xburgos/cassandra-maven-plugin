/*
  * The MIT License
  *
  * Original work sponsored and donated by National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
  * Further enhancement before move to Codehaus sponsored and donated by Lakeside A/S (http://www.lakeside.dk)
  *
  * Copyright (c) to all contributors
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  *
  * $HeadURL$
  * $Id$
  */
package org.codehaus.mojo.chronos.jmeter;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.jfree.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Manages dependencies for the current maven project (making them available for jmeter).
 *
 * @author ksr@lakeside.dk
 */
public final class DependencyUtil
{
    private String jmeterhome;

    private Log log;

    public DependencyUtil( String jmeterHome, Log log )
    {
        this.jmeterhome = jmeterHome;
        this.log = log;
    }

    public List getDependencies( MavenProject project )
    {
        List result = new ArrayList();

        Iterator it = project.getAttachedArtifacts().iterator();
        while ( it.hasNext() )
        {
            Artifact artifact = (Artifact) it.next();
            File attachedArtifactFile = artifact.getFile();
            result.add( attachedArtifactFile );
        }
        Artifact artifact = project.getArtifact();
        if ( artifact == null )
        {
            log.warn( "Artifact not found. Note that if Your JMeter test contains JUnittestcases, "
                + "You can only invoke this goal through the default lifecycle." );
        }
        else
        {
            File artifactFile = project.getArtifact().getFile();
            if ( artifactFile == null )
            {
                log.warn( "Artifact not found. Note that if Your JMeter test contains JUnittestcases, "
                    + "You can only invoke this goal through the default lifecycle." );
            }
            else
            {
                result.add( artifactFile );
            }
        }
        Set dependencyArtifacts = project.getArtifacts();
        if ( dependencyArtifacts != null )
        {
            Iterator deps = dependencyArtifacts.iterator();
            while ( deps.hasNext() )
            {
                Artifact dependency = (Artifact) deps.next();
                result.add( dependency.getFile() );
            }
        }
        return result;
    }

    List copyDependencies( MavenProject project )
        throws IOException
    {
        final List copied = new ArrayList();
        Iterator it = getDependencies( project ).iterator();
        while ( it.hasNext() )
        {
            File artifactFile = (File) it.next();
            copyFileToDir( artifactFile, copied );
        }
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            public void run()
            {
                cleanUpDependencies( copied );
            }
        } );
        return copied;
    }

    void cleanUpDependencies( List copied )
    {
        for ( Iterator iterator = copied.iterator(); iterator.hasNext(); )
        {
            File file = (File) iterator.next();
            if ( file.exists() )
            {
                file.delete();
            }
        }
    }

    void copyFileToDir( File file, List copied )
        throws IOException
    {
        File lib = new File( jmeterhome, "lib" );
        File junitdir = new File( lib, "junit" );
        File target = new File( junitdir, file.getName() );

        /* Merge from Atlassion */
        int i = 0;
        while ( target.exists() )
        {
            target = new File( junitdir, String.valueOf( i ) + "-" + file.getName() );
            i++;
        }
        /* End */

        target.createNewFile();
        InputStream input = new BufferedInputStream( new FileInputStream( file ) );
        OutputStream output = new BufferedOutputStream( new FileOutputStream( target ) );
        IOUtils.getInstance().copyStreams( input, output );
        output.close();
        input.close();
        log.debug( "Dependency copied to jmeter distribution at: " + target );
        copied.add( target );
    }
}
