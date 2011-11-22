package org.codehaus.mojo.openjpa;

/**
 * Copyright 2007  Rahul Thakur
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.openjpa.enhance.PersistenceCapable;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.0.0
 */
public class OpenJpaEnhancerMojoTest
    extends AbstractOpenJpaMojoTest
{
    private static final String TEST_PROJECT_01 = "project-01";

    public void testLookup()
        throws Exception
    {
        OpenJpaEnhancerMojo mojo = (OpenJpaEnhancerMojo) lookup( OpenJpaEnhancerMojo.ROLE );
        assertNotNull( mojo );
    }

    public void testExecution()
        throws Exception
    {
        OpenJpaEnhancerMojo mojo = (OpenJpaEnhancerMojo) lookup( OpenJpaEnhancerMojo.ROLE );
        assertNotNull( mojo );

        List goals = new ArrayList();
        goals.add( "clean" );
        //goals.add( "compile" );
        String pluginSpec = getPluginCLISpecification();
        goals.add( pluginSpec + "enhance" );

        buildProject( TEST_PROJECT_01, new Properties(), goals );

        // load the enhanced class
        String pathToClass = getBasedir() + "/target/test-classes/projects/" + TEST_PROJECT_01 + "/target/classes/";
        // System.out.println( "Path to class: " + pathToClass );
        assertTrue( new File( pathToClass ).exists() );

        URL url = new URL( "file:////" + pathToClass );
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = new URLClassLoader( new URL[] { url }, contextClassLoader );
        Class klass = Class.forName( "Person", false, loader );
        assertNotNull( klass );

        // verify class is enhanced
        Object obj = klass.newInstance();
        assertTrue( obj instanceof PersistenceCapable );
    }

}
