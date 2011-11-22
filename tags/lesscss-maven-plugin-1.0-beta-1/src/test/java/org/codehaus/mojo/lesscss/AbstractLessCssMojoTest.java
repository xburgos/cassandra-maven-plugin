package org.codehaus.mojo.lesscss;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.util.DirectoryScanner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest( AbstractLessCssMojo.class )
@RunWith( PowerMockRunner.class )
public class AbstractLessCssMojoTest
    extends AbstractMojoTestCase
{

    private AbstractLessCssMojo mojo;

    private File sourceDirectory = new File( "./source" );

    private String[] includes = new String[] { "include" };

    private String[] excludes = new String[] { "exclude" };

    private String[] files = new String[] { "file" };

    @Mock
    private DirectoryScanner directoryScanner;

    @Before
    public void setUp()
        throws Exception
    {
        mojo = new AbstractLessCssMojo()
        {
            public void execute()
                throws MojoExecutionException
            {
            }
        };

        setVariableValueToObject( mojo, "sourceDirectory", sourceDirectory );
        setVariableValueToObject( mojo, "includes", includes );
        setVariableValueToObject( mojo, "excludes", excludes );
    }

    @Test
    public void testGetFiles()
        throws Exception
    {
        whenNew( DirectoryScanner.class ).withNoArguments().thenReturn( directoryScanner );
        when( directoryScanner.getIncludedFiles() ).thenReturn( files );

        assertSame( files, mojo.getIncludedFiles() );

        verifyNew( DirectoryScanner.class ).withNoArguments();
        verify( directoryScanner ).setBasedir( same( sourceDirectory ) );
        verify( directoryScanner ).setIncludes( same( includes ) );
        verify( directoryScanner ).setExcludes( same( excludes ) );
        verify( directoryScanner ).scan();
    }

    @After
    public void tearDown()
    {
    }
}
