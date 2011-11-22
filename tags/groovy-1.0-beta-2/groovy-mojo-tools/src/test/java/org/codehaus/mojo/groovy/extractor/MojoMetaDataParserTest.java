/*
 * Copyright (C) 2006-2007 the original author or authors.
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

package org.codehaus.mojo.groovy.extractor;

import java.net.URL;

import groovyjarjarantlr.RecognitionException;
import junit.framework.TestCase;

/**
 * Unit tests for {@link MojoMetaDataParser}.
 *
 * @version $Rev$ $Date$
 */
public class MojoMetaDataParserTest
    extends TestCase
{
    private MojoMetaData parse(final String source) throws Exception {
        URL url = getClass().getResource(source);
        assertNotNull("Expected non-null URL for resource: " + source, url);
        MojoMetaDataParser parser = new MojoMetaDataParser();
        return parser.parse(url);
    }

    public void testParseGroovyBasicElements() throws Exception {
        MojoMetaData md = parse("MojoMetaDataParserTest1.groovy");
        assertNotNull(md);

        assertEquals("some.thing", md.getPackageName());

        String[] imports = md.getImports();
        assertNotNull(imports);
        assertEquals(4, imports.length);
        assertEquals("a", imports[0]);
        assertEquals("a.b", imports[1]);
        assertEquals("a.b.c.*", imports[2]);
        assertEquals("org.apache.maven.project.MavenProject", imports[3]);

        //
        // TODO: Validate the javadocs
        //

        MojoMetaData.MojoClass[] classes = md.getClasses();
        assertNotNull(classes);
        assertEquals(1, classes.length);
        assertEquals("SomeClass", classes[0].getName());
        assertEquals("MySuperClass", classes[0].getSuperName());

        MojoMetaData.MojoParameter[] params = classes[0].getParameters();
        assertNotNull(params);
        assertEquals(4, params.length);

        assertEquals("flag", params[0].getName());
        assertEquals("boolean", params[0].getType());

        assertEquals("project", params[1].getName());
        assertEquals("MavenProject", params[1].getType());

        assertEquals("messages", params[2].getName());
        assertEquals("String[]", params[2].getType());

        assertEquals("untyped", params[3].getName());
        assertEquals(null, params[3].getType());
    }

    public void testParseJavaBasicElements() throws Exception {
        MojoMetaData md = parse("MojoMetaDataParserTest1.java.txt");
        assertNotNull(md);

        assertEquals("some.thing", md.getPackageName());

        String[] imports = md.getImports();
        assertNotNull(imports);
        assertEquals(4, imports.length);
        assertEquals("a", imports[0]);
        assertEquals("a.b", imports[1]);
        assertEquals("a.b.c.*", imports[2]);
        assertEquals("org.apache.maven.project.MavenProject", imports[3]);

        //
        // TODO: Validate the javadocs
        //

        MojoMetaData.MojoClass[] classes = md.getClasses();
        assertNotNull(classes);
        assertEquals(1, classes.length);
        assertEquals("SomeClass", classes[0].getName());
        assertEquals("MySuperClass", classes[0].getSuperName());

        MojoMetaData.MojoParameter[] params = classes[0].getParameters();
        assertNotNull(params);
        assertEquals(4, params.length);

        assertEquals("flag", params[0].getName());
        assertEquals("boolean", params[0].getType());

        assertEquals("project", params[1].getName());
        assertEquals("MavenProject", params[1].getType());

        assertEquals("messages", params[2].getName());
        assertEquals("String[]", params[2].getType());

        assertEquals("untyped", params[3].getName());
        assertEquals("Object", params[3].getType());
    }

    public void testParseComplexMojo() throws Exception {
        MojoMetaData md = parse("MojoMetaDataParserTest3.groovy");
        assertNotNull(md);

        MojoMetaData.MojoClass[] classes = md.getClasses();
        assertNotNull(classes);
        assertEquals(1, classes.length);

        MojoMetaData.MojoParameter[] params = classes[0].getParameters();
        assertNotNull(params);
        assertEquals(5, params.length);

        //
        // NOTE: Checking to make sure things like this work:
        //
        //       @parameter expression="${project.build.outputDirectory}/META-INF"
        //
        //       Seems the parser had trouble with that :-\
        //
        
        for (int i=0; i<params.length; i++) {
            if ("outputDirectory".equals(params[i].getName())) {
                assertNotNull(params[i].getJavadocs());
            }
        }
    }

    public void testParseBadSyntax() throws Exception {
        try {
            parse("MojoMetaDataParserTest2.groovy");
            fail();
        }
        catch (RecognitionException expected) {
            // ignore
        }
    }
}
