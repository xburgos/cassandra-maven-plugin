package org.codehaus.mojo.localconfig;

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.IOUtil;

/**
 * Touch a local file to write the serial number of current last update.
 * 
 * @author jdcasey
 * 
 * @goal touch
 *
 */
public class TouchLocalConfigMojo
    extends AbstractMojo
{
    
    /**
     * The check-file to write for lastUpdate.
     * 
     * @parameter
     * @required
     */
    private File checkFile;
    
    /**
     * The serial number to write to the check-file.
     * 
     * @parameter default-value="${project.version}"
     * @required
     */
    private String serialNumber;
    
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        File dir = checkFile.getParentFile();
        
        dir.mkdirs();
        
        FileWriter writer = null;
        
        try
        {
            writer = new FileWriter( checkFile );
            writer.write( serialNumber );
            writer.flush();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to write check-file: " + checkFile + " with serial number: " + serialNumber, e );
        }
        finally
        {
            IOUtil.close( writer );
        }
        
    }
    
}
