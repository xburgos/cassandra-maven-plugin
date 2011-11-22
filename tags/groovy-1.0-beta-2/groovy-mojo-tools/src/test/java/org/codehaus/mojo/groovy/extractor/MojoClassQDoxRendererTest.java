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

import junit.framework.TestCase;
import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.DocletTag;

/**
 * Unit tests for {@link MojoClassQDoxRenderer}.
 *
 * @version $Rev$ $Date$
 */
public class MojoClassQDoxRendererTest
    extends TestCase
{
    private MojoMetaData parse(final String source) throws Exception {
        URL url = getClass().getResource(source);
        MojoMetaDataParser parser = new MojoMetaDataParser();
        return parser.parse(url);
    }

    public void testTranslate1() throws Exception {
        MojoMetaData md = parse("MyMojo1.groovy");
        assertNotNull(md);

        JavaDocBuilder builder = MojoClassQDoxRenderer.createJavaDocBuilder(md);
        JavaClass[] classes = builder.getClasses();
        assertNotNull(classes);
        assertEquals(1, classes.length);

        JavaField field = classes[0].getFieldByName(MojoClassQDoxRenderer.SOURCE_TYPE_FIELD);
        assertNotNull(field);

        DocletTag tag = field.getTagByName(MojoClassQDoxRenderer.SOURCE_TYPE_TAG);
        assertNotNull(tag);
        assertEquals(MojoClassQDoxRenderer.SOURCE_TYPE_GROOVY, tag.getValue());
    }

    public void testTranslate2() throws Exception {
        MojoMetaData md = parse("MyMojo2.groovy");
        assertNotNull(md);

        JavaDocBuilder builder = MojoClassQDoxRenderer.createJavaDocBuilder(md);
        JavaClass[] classes = builder.getClasses();
        assertNotNull(classes);
        assertEquals(1, classes.length);

        JavaField field = classes[0].getFieldByName(MojoClassQDoxRenderer.SOURCE_TYPE_FIELD);
        assertNotNull(field);

        DocletTag tag = field.getTagByName(MojoClassQDoxRenderer.SOURCE_TYPE_TAG);
        assertNotNull(tag);
        assertEquals(MojoClassQDoxRenderer.SOURCE_TYPE_GROOVY, tag.getValue());
    }

    public void testTranslate3() throws Exception {
        MojoMetaData md = parse("MyMojo3.groovy");
        assertNotNull(md);

        JavaDocBuilder builder = MojoClassQDoxRenderer.createJavaDocBuilder(md);
        JavaClass[] classes = builder.getClasses();
        assertNotNull(classes);
        assertEquals(1, classes.length);

        JavaField field = classes[0].getFieldByName(MojoClassQDoxRenderer.SOURCE_TYPE_FIELD);
        assertNotNull(field);

        DocletTag tag = field.getTagByName(MojoClassQDoxRenderer.SOURCE_TYPE_TAG);
        assertNotNull(tag);
        assertEquals(MojoClassQDoxRenderer.SOURCE_TYPE_GROOVY, tag.getValue());
    }

    public void testTranslateWithClassResolve() throws Exception {
        MojoMetaData md = parse("MyMojo5.groovy");
        assertNotNull(md);

        JavaDocBuilder builder = MojoClassQDoxRenderer.createJavaDocBuilder(md);
        JavaClass[] classes = builder.getClasses();
        assertNotNull(classes);
        assertEquals(1, classes.length);

        JavaField field = classes[0].getFieldByName("project");
        assertNotNull(field);

        assertEquals("org.apache.maven.project.MavenProject", field.getType().getValue());
    }

    public void testTranslateWithSuper() throws Exception {
        MojoMetaData md1 = parse("MyMojo4.groovy");
        assertNotNull(md1);

        MojoMetaData md2 = parse("MojoSupport.groovy");
        assertNotNull(md2);

        JavaDocBuilder builder = new JavaDocBuilder();
        MojoClassQDoxRenderer.addSources(md1, builder);
        MojoClassQDoxRenderer.addSources(md2, builder);

        JavaClass[] classes = builder.getClasses();
        assertNotNull(classes);
        assertEquals(2, classes.length);

        DocletTag tag;
        for (int i=0; i<classes.length; i++) {
            System.err.println(classes[i].getFullyQualifiedName());

            JavaField field = classes[i].getFieldByName(MojoClassQDoxRenderer.SOURCE_TYPE_FIELD);
            assertNotNull(field);

            tag = field.getTagByName(MojoClassQDoxRenderer.SOURCE_TYPE_TAG);
            assertNotNull(tag);
            assertEquals(MojoClassQDoxRenderer.SOURCE_TYPE_GROOVY, tag.getValue());
            
            tag = classes[i].getTagByName(GroovyMojoDescriptorExtractor.GOAL);
            if (tag != null) {
                assertEquals("mymojo.MyMojo4", classes[i].getFullyQualifiedName());

                JavaClass superClass = classes[i].getSuperJavaClass();
                assertNotNull(superClass);
                assertEquals("mymojo.MojoSupport", superClass.getFullyQualifiedName());
            }
        }
    }
}
