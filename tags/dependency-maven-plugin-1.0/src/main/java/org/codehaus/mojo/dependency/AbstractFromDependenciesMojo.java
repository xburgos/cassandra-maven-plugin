/*
 *  Copyright 2005-2006 Brian Fox (brianefox@gmail.com)
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
package org.codehaus.mojo.dependency;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
/**
 * Abstract Parent class used by mojos that get Artifact information from the project dependencies.
 * @author brianf
 *
 */
public abstract class AbstractFromDependenciesMojo
    extends AbstractMojo
{

    /**
     * Output location used for mojo.
     * @parameter expression="${outputDirectory}" default-value="${project.build.directory}/dependency"
     * @required
     */
    protected File outputDirectory;

    /**
     * To look up Archiver/UnArchiver implementations
     * @parameter expression="${component.org.codehaus.plexus.archiver.manager.ArchiverManager}"
     * @required
     * @readonly
     */
    protected ArchiverManager archiverManager;

    /**
     * Contains the full list of projects in the reactor.
     * @parameter expression="${reactorProjects}"
     * @required
     * @readonly
     */
    private List reactorProjects;

    /**
     * Creates a Map of artifacts within the reactor using the groupId:artifactId:classifer:version as key
     *
     * @return A HashMap of all artifacts available in the reactor
     */
    protected Map getMappedReactorArtifacts()
    {
        Map mappedReactorArtifacts = new HashMap();

        for ( Iterator i = reactorProjects.iterator(); i.hasNext(); )
        {
            MavenProject reactorProject = (MavenProject) i.next();

            String key = reactorProject.getGroupId() + ":" + reactorProject.getArtifactId() + ":"
                + reactorProject.getVersion();

            mappedReactorArtifacts.put( key, reactorProject.getArtifact() );
        }

        return mappedReactorArtifacts;
    }

    /**
     * Retrieves all artifact dependencies within the reactor
     *
     * @return A HashSet of artifacts
     */
    protected Set getDependencies()
    {
        Map reactorArtifacts = getMappedReactorArtifacts();

        Map dependencies = new HashMap();

        for ( Iterator i = reactorProjects.iterator(); i.hasNext(); )
        {
            MavenProject reactorProject = (MavenProject) i.next();

            for ( Iterator j = reactorProject.getArtifacts().iterator(); j.hasNext(); )
            {
                Artifact artifact = (Artifact) j.next();

                //allow use of classifier in lookup
                String classifierKey = "";
                if ( artifact.getClassifier() != null )
                {
                    classifierKey = artifact.getClassifier() + ":";
                }

                String key = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + classifierKey
                    + artifact.getVersion();

                if ( !reactorArtifacts.containsKey( key ) && !dependencies.containsKey( key ) )
                {
                    dependencies.put( key, artifact );
                }
            }
        }

        return new HashSet( dependencies.values() );
    }
}
