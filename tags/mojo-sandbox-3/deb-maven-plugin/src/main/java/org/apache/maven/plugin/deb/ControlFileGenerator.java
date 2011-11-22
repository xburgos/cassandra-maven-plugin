package org.apache.maven.plugin.deb;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface ControlFileGenerator
{
    String ROLE = ControlFileGenerator.class.getName();

    void setDependencies( Set artifacts );

    void setGroupId( String groupId );

    void setArtifactId( String artifactId );

    void setVersion( String version );

    void setDescription( String description );

    void setShortDescription( String shortDescription);

    void setArchitecture( String architecture );

    void setMaintainer( String maintainer );

    void setPackageName( String packageName );

    void setPriority( String priority );

    void setSection( String section );

    void setMaintainerRevision( String maintainerRevision );

    void setDebFileName( String debFileName );

    void generateControl( File basedir )
        throws IOException, MojoFailureException;

    String getDebFileName()
        throws MojoFailureException;

    public String getDepends();

    public String getDebianVersion()
        throws MojoFailureException;

    public String getDebianPackageName()
        throws MojoFailureException;

    public String getDebianDescription()
        throws MojoFailureException;
}
