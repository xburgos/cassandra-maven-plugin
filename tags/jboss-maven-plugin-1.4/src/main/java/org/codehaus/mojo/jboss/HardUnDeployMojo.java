package org.codehaus.mojo.jboss;

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

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Delete file form <code>$JBOSS_HOME/server/[serverName]/deploy</code> directory.
 * 
 * @author <a href="mailto:bjkuczynski@gmial.com">Bartek 'Koziolek' Kuczynski</a>
 * @goal hard-undeploy
 * @since 1.4
 */
public class HardUnDeployMojo
    extends AbstractJBossMojo
{
    /**
     * The name of the file or directory to undeploy.
     * 
     * @parameter default-value="${project.build.directory}/${project.build.finalName}.${project.packaging}"
     */
    protected String fileName;

    /**
     * Main plugin execution.
     * 
     * @throws MojoExecutionException
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        checkConfig();

        File tmp = new File( fileName );

        File earFile = new File( jbossHome + "/server/" + serverName + "/deploy/" + tmp.getName() );
        getLog().info( "Undeploy file: " + earFile.getName() );
        if ( !earFile.exists() )
        {
            getLog().info( "File " + earFile.getAbsolutePath() + " doesn't exist!" );
            return;
        }
        if ( earFile.delete() )
        {
            getLog().info( "File " + earFile.getName() + " undeployed!\nhave a nice day!" );
        }
    }
}
