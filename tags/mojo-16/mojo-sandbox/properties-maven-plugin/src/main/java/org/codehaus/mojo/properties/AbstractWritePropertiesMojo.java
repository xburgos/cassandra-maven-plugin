package org.codehaus.mojo.properties;

/*
 * Copyright 2006 The Codehaus.
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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author <a href="mailto:zarars@gmail.com">Zarar Siddiqi</a>
 * @version $Id$
 */
public abstract class AbstractWritePropertiesMojo extends AbstractMojo
{

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The properties file that will be used when writing properties.
     *
     * @parameter
     * @required
     */
    protected File outputFile;

    protected void writeProperties( Properties properties, File file )
    {
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream( file );
        }
        catch ( FileNotFoundException e )
        {
            getLog().error( "Could not create FileOutputStream: " + fos );
            e.printStackTrace();
        }
        try
        {
            properties.store( fos, "Properties" );
        }
        catch ( IOException e )
        {
            getLog().error( "Error writing properties: " + fos );
            e.printStackTrace();
        }
        try
        {
            fos.close();
        }
        catch ( IOException e )
        {
            getLog().error( "Error closing FileOutputStream: " + fos );
            e.printStackTrace();
        }
    }

    protected void validateOutputFile()
        throws MojoExecutionException
    {
        if ( outputFile.isDirectory() )
        {
            throw new MojoExecutionException( "outputFile must be a file and not a directory" );
        }
        // ensure path exists
        if ( outputFile.getParentFile() != null )
        {
            outputFile.getParentFile().mkdirs();
        }
    }
}
