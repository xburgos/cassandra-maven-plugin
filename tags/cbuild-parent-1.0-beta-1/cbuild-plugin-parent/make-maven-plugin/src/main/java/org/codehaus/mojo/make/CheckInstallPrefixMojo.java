package org.codehaus.mojo.make;

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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.tools.project.extras.RequiredPOMPropertyChecker;
import org.codehaus.mojo.tools.project.extras.RequiredPOMPropertyMissingException;


/**
 * Validate that the required project property <code>prefix</code> is available in the
 * current project before allowing it to continue. This property is required
 * to allow <code>@pathOf(..)@</code> style references work, as in the make plugin's mojos.
 * 
 * @requiresProject true
 * @goal validate-pom
 * @phase validate
 */
public class CheckInstallPrefixMojo
    extends AbstractMojo
{
    /**
     * Defines the property that needs to be defined which is <code>prefix</code>
     */
    public static final String MAKE_INSTALL_PREFIX = "prefix";
    
    /**
     * Project instance to validate before building the RPM.
     * 
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Make sure the project has a maven property <code>prefix</code> defined
     * @throws MojoExecutionException thrown when <code>prefix</code> is not defined in the project
     * @throws MojoFailureException inherited interface, not thrown
     */
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        try
        {
            RequiredPOMPropertyChecker.checkForRequiredPOMProperty( project, MAKE_INSTALL_PREFIX );
        }
        catch ( RequiredPOMPropertyMissingException e )
        {
            throw new MojoExecutionException( "Invalid RPM Project. Reason: " + e.getLongMessage(), e );
        }
    }

}
