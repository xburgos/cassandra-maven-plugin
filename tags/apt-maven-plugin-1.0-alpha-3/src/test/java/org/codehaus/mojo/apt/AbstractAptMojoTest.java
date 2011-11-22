package org.codehaus.mojo.apt;

/*
 * The MIT License
 *
 * Copyright 2006-2008 The Codehaus.
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.test.plugin.BuildTool;
import org.apache.maven.shared.test.plugin.PluginTestTool;
import org.apache.maven.shared.test.plugin.TestToolsException;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

/**
 * Runs the integration tests for an apt mojo.
 * 
 * @author <a href="mailto:markhobson@gmail.com">Mark Hobson</a>
 * @version $Id$
 */
public abstract class AbstractAptMojoTest extends PlexusTestCase
{
    // constants --------------------------------------------------------------

    private static final String LOCAL_REPOSITORY_PATH = "target/test-classes/repository/";

    private static final String PLUGIN_VERSION = "test";
    
    // fields -----------------------------------------------------------------

    private static boolean pluginInstalled = false;

    private static File localRepository;

    private BuildTool buildTool;

    // TestCase methods -------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        // install plugin to test local repository for use by tests
        if ( !pluginInstalled )
        {
            PluginTestTool pluginTestTool = (PluginTestTool) lookup( PluginTestTool.ROLE );

            File pomFile = new File( getBasedir(), "pom.xml" );

            localRepository = getTestFile( LOCAL_REPOSITORY_PATH );

            localRepository =
                pluginTestTool.preparePluginForUnitTestingWithMavenBuilds( pomFile, PLUGIN_VERSION, localRepository );

            pluginInstalled = true;
        }

        buildTool = (BuildTool) lookup( BuildTool.ROLE );
    }

    // tests ------------------------------------------------------------------

    public void testAptBasic() throws TestToolsException
    {
        executeProject( "apt-basic-test" );
        assertFileExists( "apt-basic-test", getTargetPath() + getClassPrefix() + "Class.txt" );
    }

    public void testAptIncludes() throws TestToolsException
    {
        executeProject( "apt-includes-test" );
        assertFileExists( "apt-includes-test", getTargetPath() + getClassPrefix() + "Class.txt" );
        assertNotFileExists( "apt-includes-test", getTargetPath() + getClassPrefix() + "ClassExcluded.txt" );
    }
    
    public void testAptAdditionalSourceRoots() throws TestToolsException
    {
        executeProject( "apt-source-roots-test" );
        assertFileExists( "apt-source-roots-test", getTargetPath() + getClassPrefix() + "Class.txt" );
        assertFileExists( "apt-source-roots-test", getTargetPath() + getClassPrefix() + "Class2.txt" );
    }
    
    public void testAptStaleSuffix() throws TestToolsException
    {
        testAptStale( "apt-stale-suffix-test", getTargetPath() + getClassPrefix() + "Class.txt" );
    }
    
    public void testAptStalePath() throws TestToolsException, FileNotFoundException, IOException
    {
        testAptStale( "apt-stale-path-test", getTargetPath() + "generated.txt" );

        // ensure all source files processed, not just stale ones
        File targetFile = getProjectFile( "apt-stale-path-test", getTargetPath() + "generated.txt" );
        assertLine( getClassPrefix() + "Class", targetFile );
        assertLine( getClassPrefix() + "Class2", targetFile );
    }
    
    public void testAptForked() throws Exception
    {
        executeProject( "apt-fork-test" );
        assertFileExists( "apt-fork-test", getTargetPath() + getClassPrefix() + "Class.txt" );
    }

    public void testAptPluginDependency() throws Exception
    {
        executeProject( "apt-plugin-dependency-test" );
        assertFileExists( "apt-plugin-dependency-test", getTargetPath() + getClassPrefix() + "Class.txt" );
    }

    public void testAptPluginDependencyWithExclusionOfToolsJar() throws Exception
    {
        executeProject( "apt-plugin-dependency-with-exclusion" );
        assertFileExists( "apt-plugin-dependency-with-exclusion", getTargetPath() + getClassPrefix() + "Class.txt" );
    }

    public void testAptSkip() throws TestToolsException
    {
        executeProject( "apt-skip-test" );
        assertNotFileExists( "apt-skip-test", getTargetPath() + getClassPrefix() + "Class.txt" );
    }

    // protected methods ------------------------------------------------------

    protected abstract String getGoal();

    protected abstract String getSourcePath();
    
    protected abstract String getTargetPath();
    
    protected abstract String getClassPrefix();
    
    // private methods --------------------------------------------------------

    private void testAptStale( String projectName, String targetPath ) throws TestToolsException
    {
        File pomFile = getProjectFile( projectName, "pom.xml" );
        executeMaven( pomFile, Arrays.asList( new String[] { "clean", getGoal() } ) );

        File targetFile = getProjectFile( projectName, targetPath );
        assertFileExists( targetFile );
        long lastModified = targetFile.lastModified();

        // ensure target unmodified
        executeMaven( pomFile, Collections.singletonList( getGoal() ) );
        assertTrue( "Expected output file to be unmodified", targetFile.lastModified() == lastModified );

        // touch source
        File sourceFile = getProjectFile( projectName, getSourcePath() + getClassPrefix() + "Class.java" );
        sourceFile.setLastModified( lastModified + 1000 );

        // ensure target modified
        executeMaven( pomFile, Collections.singletonList( getGoal() ) );
        assertFalse( "Expected output file to be modified", targetFile.lastModified() == lastModified );
    }
    
    private void executeProject( String projectName ) throws TestToolsException
    {
        File pomFile = getProjectFile( projectName, "pom.xml" );
        List<String> goals = Arrays.asList( "clean", getGoal() );

        executeMaven( pomFile, goals );
    }

    private void executeMaven( File pomFile, List<String> goals ) throws TestToolsException
    {
        Properties properties = new Properties();
        File buildLogFile = new File( pomFile.getParentFile(), "build.log" );

        InvocationRequest request = buildTool.createBasicInvocationRequest( pomFile, properties, goals, buildLogFile );
        request.setLocalRepositoryDirectory( localRepository );

        InvocationResult result = buildTool.executeMaven( request );

        assertTrue( "Maven failed, see build log " + buildLogFile, result.getExitCode() == 0 );
    }

    private static void assertFileExists( String projectName, String path )
    {
        assertFileExists( getProjectFile( projectName, path ) );
    }

    private static void assertFileExists( File file )
    {
        assertTrue( "Expected file: " + file, file.exists() );
    }

    private static void assertNotFileExists( String projectName, String path )
    {
        assertNotFileExists( getProjectFile( projectName, path ) );
    }

    private static void assertNotFileExists( File file )
    {
        assertFalse( "Unexpected file: " + file, file.exists() );
    }
    
    private static void assertLine( String line, File file ) throws FileNotFoundException, IOException
    {
        List<String> lines = CollectionUtils.genericList( FileUtils.loadFile( file ), String.class );

        assertTrue( "Expected line '" + line + "' in file: " + file, lines.contains( line ) );
    }

    private static File getProjectFile( String projectName, String path )
    {
        return getTestFile( "target/test-classes/projects/" + projectName + "/" + path );
    }
}
