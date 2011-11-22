/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.codehaus.mojo.groovy.extractor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.io.Reader;
import java.io.StringWriter;
import java.io.StringReader;

import com.thoughtworks.qdox.JavaDocBuilder;

/**
 * Renders a {@link org.codehaus.mojo.groovy.extractor.MojoMetaData.MojoClass} as a minimal Java source
 * suitable for loading with QDox.
 *
 * @version $Rev$ $Date$
 */
public class MojoClassQDoxRenderer
{
    public static final String SOURCE_TYPE_FIELD = "__SOURCE_TYPE__";

    public static final String SOURCE_TYPE_TAG = "source-type";

    public static final String SOURCE_TYPE_GROOVY = "groovy";

    public static final String SOURCE_TYPE_JAVA = "java";

    private MojoMetaData.MojoClass mojoClass;

    public MojoClassQDoxRenderer(final MojoMetaData.MojoClass mojoClass) {
        assert mojoClass != null;

        this.mojoClass = mojoClass;
    }

    public void write(final Writer writer) throws IOException {
        PrintWriter output = new PrintWriter(writer);
        writeHeader(output);
        writeClass(output);
        output.flush();
    }

    private void writeHeader(final PrintWriter output) {
        String packageName = mojoClass.getPackageName();
        if (packageName != null) {
            output.print("package ");
            output.print(packageName);
            output.println(";");
            output.println();
        }

        String[] imports = mojoClass.getImports();
        for (int i=0; i<imports.length; i++) {
            output.print("import ");
            output.print(imports[i]);
            output.println(";");
            output.println();
        }
    }

    private void writeJavadocs(final PrintWriter output, final String javadocs) {
        if (javadocs != null) {
            output.print("/**");
            output.print(javadocs);
            output.println("*/");
        }
    }

    private void writeClass(final PrintWriter output) {
        writeJavadocs(output, mojoClass.getJavadocs());
        output.print("class ");
        output.print(mojoClass.getName());

        String superName = mojoClass.getSuperName();
        if (superName != null) {
            output.print(" extends ");
            output.print(superName);
        }
        output.println();
        output.println("{");

        // Add fields
        writeFields(output);

        output.println();
        output.println("}");
    }

    private void writeFields(final PrintWriter output) {
        writeSourceType(output);

        MojoMetaData.MojoParameter[] params = mojoClass.getParameters();
        for (int i=0; i<params.length; i++) {
            writeJavadocs(output, params[i].getJavadocs());

            String type = params[i].getType();
            if (type == null) {
                type = "java.lang.Object"; // aka def
            }
            output.print(type);

            output.print(" ");
            output.print(params[i].getName());
            output.println(";");
            output.println();
        }
    }

    private void writeSourceType(final PrintWriter output) {
        // Add a synthetic field to allow us to tell which is Groovy and which is Java
        output.println();
        output.print("/** @");
        output.print(SOURCE_TYPE_TAG);
        output.print(" ");

        int type = mojoClass.getSourceType();
        switch (type) {
            case MojoMetaData.SOURCE_TYPE_GROOVY:
                output.print(SOURCE_TYPE_GROOVY);
                break;

            case MojoMetaData.SOURCE_TYPE_JAVA:
                output.print(SOURCE_TYPE_JAVA);
                break;
        }
        
        output.println(" */");
        output.print("org.codehaus.mojo.groovy.extractor.SourceTypeField ");
        output.print(SOURCE_TYPE_FIELD);
        output.println(";");
        output.println();
    }

    //
    // Helpers
    //

    /**
     * Creates a reader from the rendered QDox output.
     */
    public Reader reader() throws IOException {
        StringWriter writer = new StringWriter();
        write(writer);
        writer.close();

        /*
        System.err.println("----8<----");
        System.err.println(writer);
        System.err.println("---->8----");
        */
        
        return new StringReader(writer.toString());
    }

    /**
     * Adds sources for each class in the mojo metadata to the QDox builder.
     */
    public static void addSources(final MojoMetaData metaData, final JavaDocBuilder builder) throws IOException {
        assert metaData != null;
        assert builder != null;

        MojoMetaData.MojoClass[] classes = metaData.getClasses();
        for (int i=0; i<classes.length; i++) {
            builder.addSource(new MojoClassQDoxRenderer(classes[i]).reader());
        }
    }

    /**
     * Creates a new QDox builder for all classes in the given mojo metadata.
     */
    public static JavaDocBuilder createJavaDocBuilder(final MojoMetaData metaData) throws IOException {
        assert metaData != null;

        JavaDocBuilder builder = new JavaDocBuilder();

        MojoMetaData.MojoClass[] classes = metaData.getClasses();
        for (int i=0; i<classes.length; i++) {
            builder.addSource(new MojoClassQDoxRenderer(classes[i]).reader());
        }

        return builder;
    }
}
