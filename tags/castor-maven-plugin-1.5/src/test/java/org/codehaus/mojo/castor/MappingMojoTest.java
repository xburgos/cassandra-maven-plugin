package org.codehaus.mojo.castor;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

/**
 * @author nicolas <nicolas@apache.org>
 */
public class MappingMojoTest
    extends AbstractMojoTestCase
{

    public void testGenerate()
        throws Exception
    {
        File testPom = new File( getBasedir(), "src/test/resources/mapping.pom" );
        Mojo mojo = (Mojo) lookupMojo( "mapping", testPom );
        assertNotNull( "Failed to configure the plugin", mojo );
        mojo.execute();

        assertTrue( "expected mapping file missing", new File( "target/mapping/request-mapping.xml" )
            .exists() );
    }
}
