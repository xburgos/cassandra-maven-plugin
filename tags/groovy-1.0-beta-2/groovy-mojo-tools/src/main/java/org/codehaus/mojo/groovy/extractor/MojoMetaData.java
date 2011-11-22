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

import java.util.List;
import java.util.ArrayList;

/**
 * Container for all of the raw-javadoc meta-data required to generate a Mojo descriptor.
 *
 * @version $Rev$ $Date$
 */
public class MojoMetaData
{
    public static final int SOURCE_TYPE_GROOVY = 1;

    public static final int SOURCE_TYPE_JAVA = 2;

    private String packageName;

    private List imports = new ArrayList();

    private List classes = new ArrayList();

    private int sourceType;

    public void setPackageName(final String name) {
        this.packageName = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void addImport(final String name) {
        imports.add(name);
    }
    
    public String[] getImports() {
        return (String[]) imports.toArray(new String[imports.size()]);
    }

    public void addClass(final String name, final String superName, final String javadocs) {
        MojoClass c = new MojoClass(name, superName, javadocs);
        classes.add(c);
    }

    public MojoClass[] getClasses() {
        return (MojoClass[]) classes.toArray(new MojoClass[classes.size()]);
    }

    public void addParameter(final String name, final String type, final String javadocs) {
        if (classes.size() == 0) {
            throw new IllegalStateException("Can not add parameter, no class has been added yet");
        }

        MojoClass current = (MojoClass) classes.get(classes.size() - 1);
        current.addParameter(name, type, javadocs);
    }

    public void setSourceType(final int sourceType) {
        this.sourceType = sourceType;
    }

    public int getSourceType() {
        return sourceType;
    }

    public class MojoClass
    {
        private String name;

        private String superName;

        private String javadocs;
        
        private List parameters = new ArrayList();

        public MojoClass(final String name, final String superName, final String javadocs) {
            this.name = name;
            this.superName = superName;
            this.javadocs = javadocs;
        }

        public String getPackageName() {
            return MojoMetaData.this.getPackageName();
        }

        public String[] getImports() {
            return MojoMetaData.this.getImports();
        }

        public int getSourceType() {
            return MojoMetaData.this.getSourceType();
        }

        public String getName() {
            return name;
        }

        public String getSuperName() {
            return superName;
        }

        public String getJavadocs() {
            return javadocs;
        }

        public void addParameter(final String name, final String type, final String javadocs) {
            MojoParameter p = new MojoParameter(name, type, javadocs);
            parameters.add(p);
        }

        public MojoParameter[] getParameters() {
            return (MojoParameter[]) parameters.toArray(new MojoParameter[parameters.size()]);
        }
    }

    public class MojoParameter
    {
        private String name;

        private String type;

        private String javadocs;

        public MojoParameter(final String name, final String type, final String javadocs) {
            this.name = name;
            this.type = type;
            this.javadocs = javadocs;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getJavadocs() {
            return javadocs;
        }
    }
}
