package org.codehaus.mojo.bod.rewrite;

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
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.shared.io.logging.MessageHolder;
import org.codehaus.mojo.bod.PomRewriteConfiguration;

public interface PomRewriter
{

    List rewrite( List candidates, PomRewriteConfiguration configuration, ArtifactRepository localRepository,
                  MessageHolder errors );

    void rewriteOnDisk( File pomFile, PomRewriteConfiguration configuration, MessageHolder errors );

}
