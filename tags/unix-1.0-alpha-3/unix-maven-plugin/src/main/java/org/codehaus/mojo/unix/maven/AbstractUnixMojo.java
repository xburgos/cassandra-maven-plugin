package org.codehaus.mojo.unix.maven;

/*
 * The MIT License
 *
 * Copyright 2009 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.apache.maven.artifact.transform.SnapshotTransformation;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.util.Map;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractUnixMojo
    extends AbstractMojo
{
    /**
     * @parameter
     */
    protected AssemblyOp[] assembly;

    /**
     * @parameter
     */
    protected Defaults defaults;

    /**
     * Optional parameter to specify the name of the package. If not set it will use the artifact id of the Maven
     * project.
     *
     * @parameter
     */
    protected String name;

    /**
     * Optional parameter to specify the version of the package. If not set it will be parsed from the
     * version information in the Maven project.
     *
     * @parameter
     */
    protected String version;

    /**
     * Optional parameter to specify the revision of the package. If not set it will be parsed from the
     * version information in the Maven project.
     *
     * @parameter
     */
    protected Integer revision;

    /**
     * @parameter expression="${project.description}"
     */
    protected String description;

    /**
     * @parameter
     * @required
     */
    protected String contact;

    /**
     * @parameter
     */
    protected String contactEmail;

    // TODO: Is this really common? Or just common enough?
    /**
     * @parameter
     */
    protected String architecture;

    /**
     * @parameter expression="${maven.unix.debug}"
     */
    protected boolean debug;

    // -----------------------------------------------------------------------
    // Internal
    // -----------------------------------------------------------------------

    /**
     * @parameter expression="${project}"
     * @readonly
     */
    protected MavenProject project;

    /**
     * @component role="org.codehaus.mojo.unix.maven.PackagingFormat"
     */
    protected Map formats;

    /**
     * @component role="org.apache.maven.artifact.transform.ArtifactTransformation" roleHint="snapshot"
     */
    protected SnapshotTransformation snapshotTransformation;

    /**
     * @component
     */
    protected MavenProjectHelper mavenProjectHelper;
}
