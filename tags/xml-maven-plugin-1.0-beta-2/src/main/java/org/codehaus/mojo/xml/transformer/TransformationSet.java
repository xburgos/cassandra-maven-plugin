/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.codehaus.mojo.xml.transformer;

import java.io.File;

import org.codehaus.plexus.components.io.filemappers.FileMapper;


/**
 * An instance of this class is used to specify a set of files,
 * which are transformed by a common XSLT stylesheet.
 */
public class TransformationSet {
    private File stylesheet;

    private File dir;

    private File outputDir;

    private boolean addedToClasspath;

    private String[] includes;

    private String[] excludes;

    private boolean skipDefaultExcludes;

    private String[] otherDepends;

    private Parameter[] parameters;

    private FileMapper[] fileMappers;

    /**
     * Sets patterns of files, which are being excluded from
     * the transformation set.
     */
    public void setExcludes( String[] excludes )
    {
        this.excludes = excludes;
    }

    /**
     * Sets patterns of files, which are being included into
     * the transformation set.
     */
    public void setIncludes( String[] includes )
    {
        this.includes = includes;
    }

    /**
     * Sets patterns of additional files, which are being considered
     * for the uptodate check.
     */
    public void setOtherDependd( String[] otherDepends )
    {
        this.otherDepends = otherDepends;
    }

    /**
     * Sets the stylesheet parameters.
     */
    public void setParameters( Parameter[] parameters )
    {
        this.parameters = parameters;
    }

    /**
     * Returns a directory, which is scanned
     * for files to transform.
     */
    public File getDir()
    {
        return dir;
    }

    /**
     * Returns patterns of files, which are being excluded from
     * the transformation set.
     */
    public String[] getExcludes()
    {
        return excludes;
    }

    /**
     * Returns patterns of files, which are being included into
     * the transformation set.
     */
    public String[] getIncludes()
    {
        return includes;
    }

    /**
     * Returns patterns of additional files, which are being considered
     * for the uptodate check.
     */
    public String[] getOtherDepends()
    {
        return otherDepends;
    }

    /**
     * Returns the output directory,
     * where the generated files are being placed. Defaults to
     * {project.build.directory}/generated-resources/xml/xslt.
     */
    public File getOutputDir()
    {
        return outputDir;
    }

    /**
     * Returns the stylesheet parameters.
     */
    public Parameter[] getParameters()
    {
        return parameters;
    }

    /**
     * Returns the XSLT stylesheet, which is being used to control
     * the transformation.
     */
    public File getStylesheet()
    {
        return stylesheet;
    }

    /**
     * Returns, whether the output directory is added to the classpath.
     * Defaults to false.
     */
    public boolean isAddedToClasspath()
    {
        return addedToClasspath;
    }

    /**
     * Returns, whether Maven's default excludes are being ignored.
     * Defaults to false (Default excludes are being used).
     */
    public boolean isSkipDefaultExcludes()
    {
        return skipDefaultExcludes;
    }

    /**
     * Sets, whether the output directory is added to the classpath.
     * Defaults to false.
     */
    public void setAddedToClasspath(boolean addedToClasspath)
    {
        this.addedToClasspath = addedToClasspath;
    }

    /**
     * Sets the name of a directory, which is scanned
     * for files to transform.
     */
    public void setDir( File dir )
    {
        this.dir = dir;
    }

    /**
     * Sets the output directory,
     * where the generated files are being placed. Defaults to
     * {project.build.directory}/generated-resources/xml/xslt.
     */
    public void setOutputDir( File outputDir )
    {
        this.outputDir = outputDir;
    }

    /**
     * Sets, whether Maven's default excludes are being ignored.
     * Defaults to false (Default excludes are being used).
     */
    public void setSkipDefaultExcludes(boolean skipDefaultExcludes)
    {
        this.skipDefaultExcludes = skipDefaultExcludes;
    }

    /**
     * Sets the XSLT stylesheet, which is being used to control
     * the transformation.
     */
    public void setStylesheet( File stylesheet )
    {
        this.stylesheet = stylesheet;
    }

    /**
     * Returns a set of file mappers, which are being used to
     * convert the generated files name.
     */
    public FileMapper[] getFileMappers()
    {
        return fileMappers;
    }

    /**
     * Sets a set of file mappers, which are being used to
     * convert the generated files name.
     */
    public void setFileMappers( FileMapper[] fileMappers )
    {
        this.fileMappers = fileMappers;
    }
}
