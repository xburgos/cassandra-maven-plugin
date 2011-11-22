package org.codehaus.mojo.cis;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.war.AbstractWarMojo;
import org.apache.maven.plugin.war.WarMojo;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.war.WarArchiver;


/**
 * Build a war/webapp.
 *
 * @goal war
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class CisWarMojo extends AbstractCisWarMojo {
    /**
     * The directory for the generated WAR.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private String outputDirectory;

    /**
     * The name of the generated WAR.
     *
     * @parameter expression="${project.build.finalName}"
     * @required
     */
    private String warName;

    /**
     * Classifier to add to the artifact generated. If given, the artifact will be an attachment instead.
     *
     * @parameter expression="${cis.classifier}"
     */
    private String classifier;

    /**
     * The Jar archiver.
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#war}"
     * @required
     */
    private WarArchiver warArchiver;


    /**
     * @component
     */
    private MavenProjectHelper projectHelper;

    /**
     * Whether this is the main artifact being built. Set to <code>false</code> if you don't want to install or
     * deploy it to the local repository instead of the default one in an execution.
     *
     * @parameter expression="${primaryArtifact}" default-value="true"
     */
    private boolean primaryArtifact;

    protected void initWarMojo( AbstractWarMojo warMojo )
        throws MojoExecutionException
    {
        super.initWarMojo( warMojo );
        setParameter( warMojo, "classifier", classifier );
        setParameter( warMojo, "outputDirectory", outputDirectory );
        setParameter( warMojo, "projectHelper", projectHelper );
        setParameter( warMojo, "primaryArtifact", Boolean.valueOf( primaryArtifact ) );
        setParameter( warMojo, "warArchiver", warArchiver );
        setParameter( warMojo, "warName", warName );
    }

    protected AbstractWarMojo newWarMojo()
    {
        return new WarMojo();
    }
}
