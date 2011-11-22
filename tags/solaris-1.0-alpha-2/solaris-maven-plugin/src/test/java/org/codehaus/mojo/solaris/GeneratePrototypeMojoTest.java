package org.codehaus.mojo.solaris;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class GeneratePrototypeMojoTest
    extends AbstractMojoTestCase
{
    public void testProject1()
        throws Exception
    {
        String expectedPrototype = getTestPath( "src/test/projects/project-1/prototype" );
        String generatedPrototype = getTestPath( "target/project-1/solaris/assembled-pkg/generated-prototype" );

        FileUtils.copyDirectoryStructure( getTestFile( "src/test/projects/project-1/target/root" ),
                                          getTestFile( "target/project-1/solaris/assembled-pkg" ) );

        Mojo mojo = lookupMojo( "generate-prototype", getTestFile( "src/test/projects/project-1/test-pom.xml" ) );

        mojo.execute();

        String expected = FileUtils.fileRead( expectedPrototype );
        String actual = FileUtils.fileRead( generatedPrototype );

        assertEquals( expected, actual );
    }

    public void testProject2()
        throws Exception
    {
        String expectedPrototype = getTestPath( "src/test/projects/project-2/prototype" );
        String generatedPrototype = getTestPath( "target/project-2/solaris/assembled-pkg/generated-prototype" );

        FileUtils.copyDirectoryStructure( getTestFile( "src/test/projects/project-2/target/root" ),
                                          getTestFile( "target/project-2/solaris/assembled-pkg" ) );

        Mojo mojo = lookupMojo( "generate-prototype", getTestFile( "src/test/projects/project-2/test-pom.xml" ) );

        mojo.execute();

        String expected = FileUtils.fileRead( expectedPrototype );
        String actual = FileUtils.fileRead( generatedPrototype );

        assertEquals( expected, actual );
    }
}
