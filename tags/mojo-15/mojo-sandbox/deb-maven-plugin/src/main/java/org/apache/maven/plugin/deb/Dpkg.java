package org.apache.maven.plugin.deb;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.io.File;

/**
 * Builds a DEB package using fakeroot dpkg-deb.  This command creates a DEB
 * package in buildDir using the contents of stageDir.  This classes
 * assumes that stageDir is going to be a direct descendent of buildDir.
 * 
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Dpkg
{
    public void buildPackage(File buildDir, File stageDir, File debFileName, boolean useFakeroot )
        throws Exception
    {
        SystemCommand systemCommand = new SystemCommand().setBaseDir(buildDir.getCanonicalPath());
        if (useFakeroot) {
            systemCommand.setCommand( "fakeroot" )
                .addArgument( "dpkg-deb" );
        } else {
            systemCommand.setCommand( "dpkg-deb" );
        }
        systemCommand.addArgument( "-b" )
            .addArgument( stageDir.getName() )
            .addArgument( new File( buildDir, debFileName.getName() ).getAbsolutePath() ).execute();
    }
}
