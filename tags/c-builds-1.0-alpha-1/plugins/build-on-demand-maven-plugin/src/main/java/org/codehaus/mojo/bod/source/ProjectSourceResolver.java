package org.codehaus.mojo.bod.source;

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

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.io.logging.MessageHolder;
import org.codehaus.mojo.bod.build.DependencyBuildRequest;

public interface ProjectSourceResolver
{

    String ROLE = ProjectSourceResolver.class.getName();

    String PROJECT_SOURCE_ARCHIVE_EXTENSION = "sourceExtension";

    String PROJECT_SOURCE_URL = "sourceUrl";

    String PROJECT_SOURCE_DIRECTORY_NAME = "sourceDirectoryName";

    String DEFAULT_PROJECT_ARCHIVE_ARTIFACT_CLASSIFIER = "project-archive";

    String DEFAULT_SOURCE_ARCHIVE_TYPE = "zip";
    
    String DEFAULT_SOURCE_DIRECTORY_NAME_PATTERN = "${project.artifactId}-${project.version}";
    
    String DEFAULT_LOCAL_SOURCE_INCLUDES = "**/**";
    
    String DEFAULT_LOCAL_SOURCE_EXCLUDES = "**/CVS/**,**/.svn/**,**/src/releases/**";
    
    File resolveProjectSources( MavenProject project, File projectsDirectory, ArtifactRepository localRepository,
                                MessageHolder errors );
    
    File resolveLatestProjectSources( MavenProject project, MessageHolder errors, DependencyBuildRequest request );

}
