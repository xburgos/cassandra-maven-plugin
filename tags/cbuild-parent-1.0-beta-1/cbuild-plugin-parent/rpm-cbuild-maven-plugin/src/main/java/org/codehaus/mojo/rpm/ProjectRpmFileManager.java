package org.codehaus.mojo.rpm;

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
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.mojo.tools.rpm.RpmFormattingException;
import org.codehaus.mojo.tools.rpm.RpmInfoFormatter;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

/**
 * @plexus.component role="org.codehaus.mojo.rpm.ProjectRpmFileManager" role-hint="default"
 * @author jdcasey
 */
public class ProjectRpmFileManager
    implements LogEnabled
{
    /**
     * @plexus.requirement role-hint="default"
     */
    private RpmInfoFormatter rpmInfoFormatter;
    
    // injected.
    private Logger logger;

    public void formatAndSetProjectArtifactFile( MavenProject project, MavenProjectHelper projectHelper, 
        File topDir, String rpmBaseName, boolean skipPlatformPostfix ) throws MojoExecutionException
    {
        File rpmsDir;
        String myArch, classifier;
        try
        {
            myArch = skipPlatformPostfix ?  "noarch" : rpmInfoFormatter.formatPlatformArchitecture();
            rpmsDir = new File( topDir, "RPMS/" + myArch );
            classifier = skipPlatformPostfix ? myArch 
                : rpmInfoFormatter.formatRPMPlatformName() + "." + myArch;
        }
        catch ( RpmFormattingException e )
        {
            throw new MojoExecutionException( "Cannot format OS architecture name for RPM directory structure.", e );
        }

        File artifactFile = new File( rpmsDir, rpmBaseName + "." + myArch + ".rpm" );

        setProjectArtifactFile( project, projectHelper, classifier, myArch, artifactFile );
    }

    public void setProjectArtifactFile( MavenProject project, MavenProjectHelper projectHelper,
        String classifier, String myArch, File artifactFile )
    {
        // 1.0-beta switches to NAR style attached artifact 4/9/2009 
        getLogger().info( "Attatching artifact classifier :" + classifier + ": name :" + artifactFile );
        projectHelper.attachArtifact( project, "rpm", classifier, artifactFile );
        // Set up maven-install-plugin to keep the extension "rpm" instead of "jar"
        RpmAttachedArtifactHandler myHdlr = new RpmAttachedArtifactHandler( classifier, myArch );
        List < Artifact > attachedList = project.getAttachedArtifacts();
        for ( Iterator < Artifact > it = attachedList.iterator(); it.hasNext(); )
        {
            Artifact artifact = it.next();
            if ( artifact.getType() == "rpm" )
            {
                artifact.setArtifactHandler( myHdlr );
            }
        }
    }

    public void enableLogging( Logger log )
    {
        this.logger = log;
    }
    
    protected Logger getLogger()
    {
        return this.logger;
    }
}
