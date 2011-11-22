/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.maven.plugin.rar;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.plugin.PluginExecutionRequest;
import org.apache.maven.plugin.PluginExecutionResponse;
import org.apache.maven.project.MavenProject;


/**
 * A mojo that build a J2EE RAR archive.
 * @version $Revision$ $Date$
 * @maven.plugin.id rar
 * @maven.plugin.description Maven plugin to build rars
 *
 * @parameter rarName String true validator description
 * @parameter outputDirectory String true validator description
 * @parameter project org.apache.maven.project.MavenProject true validator description
 * @parameter basedir String true validator description
 *
 * @goal.name rar
 * @goal.rar.description build a rar
 * @goal.rar.prereq resources
 * @goal.rar.parameter rarName #maven.final.name
 * @goal.rar.parameter outputDirectory #maven.build.dir
 * @goal.rar.parameter project #project
 * @goal.rar.parameter basedir #maven.build.dest
 */
public class RarPlugin extends AbstractJarPlugin {
    public void execute(PluginExecutionRequest request, PluginExecutionResponse response) throws Exception {
        String outputDirectory = (String) request.getParameter("outputDirectory");
        String rarName = (String) request.getParameter("rarName");
        MavenProject project = (MavenProject) request.getParameter("project");
        String baseDir = (String) request.getParameter("basedir");

        Map includes = new LinkedHashMap();
        addTaggedDependencies(includes, project, "rar");
        addDirectory(includes, new File(baseDir));

        File rarFile = new File(outputDirectory, rarName + ".rar");
        createJar(rarFile, includes);
    }
}
