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

package org.codehaus.mojo.pde.feature;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Organization;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.apache.maven.project.MavenProject;

public class FeatureMojoTest
    extends AbstractMojoTestCase
{
    private FeatureMojo mojo;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        File pluginXml = new File( getBasedir(), "src/test/resources/build/feature-plugin-config.xml" );
        mojo = (FeatureMojo) lookupMojo( "feature", pluginXml );
        assertNotNull( mojo );
    }

    public void testExecture()
        throws Exception
    {
        MavenProject project = new MavenProject();
        project.setGroupId( "a.b.c.d" );
        project.setArtifactId( "z" );
        project.setVersion( "1.0.0.20070101" );
        project.setOrganization( new Organization() );
        project.getOrganization().setName( "Apache Maven" );

        Artifact artifact = new ArtifactStub();
        artifact.setGroupId( project.getGroupId() );
        artifact.setArtifactId( project.getArtifactId() );
        artifact.setVersion( project.getVersion() );
        project.setArtifact( artifact );

        Collection artifacts = new ArrayList();
        Artifact a = new ArtifactStub();
        a.setGroupId( "a.b" );
        a.setArtifactId( "x" );
        a.setVersion( "1.1.2" );
        a.setFile( getTestFile( "src/test/resources/com.jcraft.jsch_0.1.27.jar" ) );
        artifacts.add( a );

        File featureFile = getTestFile( "target/test-classes/feature.xml" );
        mojo.setProject( project );
        mojo.setArtifacts( artifacts );
        mojo.setOutputDirectory( featureFile.getParentFile() );

        mojo.execute();
    }

}
