/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.maven.plugin.rar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.maven.artifact.MavenArtifact;
import org.apache.maven.plugin.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * Base class for tasks that build archives in JAR file format.
 * @version $Revision$ $Date$
 */
public abstract class AbstractJarPlugin implements Plugin {
    private byte[] buffer = new byte[4096];

    /**
     * Add artifacts from tagged dependencies to the archive. For example, the definition:
     * <code>
     *   <dependency>
     *     <artifactId>my-library</artifactId>
     *     <version>1.0.1</version>
     *     <property>
     *        <jar>my-library.jar
     *     <property>
     *   </dependency>
     * </code>
     * would result in the archive <code>my-library-1.0.1.jar</code> being included
     * in the output jar as /my-library.jar. The entry name will default to the base
     * name of the archive in the root: <code>/my-library-1.0.1.jar</code>
     * @param includes a map <String, File> of items to be include in the outpur
     * @param project the project object model
     * @param tag the property tag to look for; for example "jar"
     */
    protected void addTaggedDependencies(Map includes, MavenProject project, String tag) {
        for (Iterator i = project.getArtifacts().iterator(); i.hasNext();) {
            MavenArtifact artifact = (MavenArtifact) i.next();
            Properties properties = artifact.getDependency().getProperties();
            if (properties.containsKey(tag)) {
                File file = new File(artifact.getPath());
                String name = (String) properties.get(tag);
                if (name == null || name.length() == 0) {
                    name = file.getName();
                }
                includes.put(name, file);
            }
        }
    }

    /**
     * Add all files in the specified directory to the archive.
     * @param includes a map <String, File> of items to be include in the outpur
     * @param baseDir the directory to add
     */
    protected void addDirectory(Map includes, File baseDir) throws IOException {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(baseDir);
        scanner.scan();
        String[] files = scanner.getIncludedFiles();
        for (int i = 0; i < files.length; i++) {
            String file = files[i];
            file = file.replace('\\', '/'); // todo shouldn't the scanner return platform independent names?
            includes.put(file, new File(baseDir, file));
        }
    }

    /**
     * Create the jar file specified and include the listed files.
     * @param jarFile the jar file to create
     * @param includes a Map<String, File>of items to include; the key is the jar entry name
     * @throws IOException if there is a problem writing the archive or reading the sources
     */
    protected void createJar(File jarFile, Map includes) throws IOException {
        JarOutputStream jos = createJar(jarFile, createManifest());
        try {
            addEntries(jos, includes);
        } finally {
            jos.close();
        }
    }

    /**
     * Create a manifest for the jar file
     * @return a default manifest; the Manifest-Version and Created-By attributes are initialized
     */
    protected Manifest createManifest() {
        Manifest mf = new Manifest();
        Attributes attrs = mf.getMainAttributes();
        attrs.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
        attrs.putValue("Created-By", "2.0 (Apache Maven)");
        return mf;
    }

    /**
     * Create the specified jar file and return a JarOutputStream to it
     * @param jarFile the jar file to create
     * @param mf the manifest to use
     * @return a JarOutputStream that can be used to write to that file
     * @throws IOException if there was a problem opening the file
     */
    protected JarOutputStream createJar(File jarFile, Manifest mf) throws IOException {
        jarFile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(jarFile);
        try {
            return new JarOutputStream(fos, mf);
        } catch (IOException e) {
            try {
                fos.close();
                jarFile.delete();
            } catch (IOException e1) {
                // ignore
            }
            throw e;
        }
    }

    /**
     * Add all entries in the supplied Map to the jar
     * @param jos a JarOutputStream that can be used to write to the jar
     * @param includes a Map<String, File> of entries to add
     * @throws IOException if there is a problem writing the archive or reading the sources
     */
    protected void addEntries(JarOutputStream jos, Map includes) throws IOException {
        for (Iterator i = includes.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String name = (String) entry.getKey();
            File file = (File) entry.getValue();
            addEntry(jos, name, file);
        }
    }

    /**
     * Add a single entry to the jar
     * @param jos a JarOutputStream that can be used to write to the jar
     * @param name the entry name to use; must be '/' delimited
     * @param source the file to add
     * @throws IOException if there is a problem writing the archive or reading the sources
     */
    protected void addEntry(JarOutputStream jos, String name, File source) throws IOException {
        FileInputStream fis = new FileInputStream(source);
        try {
            jos.putNextEntry(new JarEntry(name));
            int count;
            while ((count = fis.read(buffer)) > 0) {
                jos.write(buffer, 0, count);
            }
            jos.closeEntry();
        } finally {
            fis.close();
        }
    }
}
