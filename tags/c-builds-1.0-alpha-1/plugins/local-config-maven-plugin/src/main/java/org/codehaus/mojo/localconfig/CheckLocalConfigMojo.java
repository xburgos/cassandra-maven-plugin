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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.IOUtil;

/**
 * Check a local file for the serial number of the last update.
 * 
 * @author jdcasey
 * 
 * @goal check
 *
 */
public class CheckLocalConfigMojo
    extends AbstractMojo
{
    
    /**
     * The check-file to look at for lastUpdate.
     * 
     * @parameter
     * @required
     */
    private File checkFile;
    
    /**
     * The serial number to check the check-file for.
     * 
     * @parameter default-value="${project.version}"
     * @required
     */
    private String serialNumber;
    
    /**
     * Fail the build (read: configuration) if the serial number is present and up-to-date.
     * @parameter default-value="true"
     * @required
     */
    private boolean failIfUpToDate;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( checkFile.exists() )
        {
            BufferedReader reader = null;
            
            try
            {
                reader = new BufferedReader( new FileReader( checkFile ) );
                
                String serialNum = reader.readLine();
                
                if ( serialNum.equals( serialNumber ) && failIfUpToDate )
                {
                    throw new MojoFailureException( "Local Configuration serial number: " + serialNumber, "Local configuration is up to date.", "Cancelling configuration build." );
                }
            }
            catch( IOException e )
            {
                getLog().info( "Failed to read check-file: " + checkFile + "; allowing build to continue." );
                getLog().debug( "Failed to read check-file.", e );
            }
            finally
            {
                IOUtil.close( reader );
            }
        }
    }
    
}
