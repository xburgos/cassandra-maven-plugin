/*
 * Copyright 2010 jglick.
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
 * under the License.
 */

package org.codehaus.mojo.nbm;

import java.io.File;
import java.io.PrintWriter;
import junit.framework.TestCase;
import org.apache.maven.plugin.logging.SystemStreamLog;

public class ExamineManifestTest extends TestCase
{
    
    public ExamineManifestTest( String testName )
    {
        super(testName);
    }

    public void testDependencyParsing()
            throws Exception
    {
        ExamineManifest em = new ExamineManifest( new SystemStreamLog() );
        File mf = File.createTempFile( "ExamineManifestTes", ".mf" );
        mf.deleteOnExit();
        PrintWriter w = new PrintWriter( mf );
        w.println( "OpenIDE-Module: org.netbeans.modules.nbjunit/1" );
        w.println( "OpenIDE-Module-Module-Dependencies: org.netbeans.insane/1, org.netbeans.libs.junit4 > 1.0" );
        w.flush();
        w.close();
        em.setManifestFile( mf );
        em.setPopulateDependencies( true );
        em.checkFile();
        assertEquals( "[org.netbeans.insane, org.netbeans.libs.junit4]", em.getDependencyTokens().toString() );
        assertEquals( "org.netbeans.modules.nbjunit", em.getModule() );
        assertEquals( "org.netbeans.modules.nbjunit/1", em.getModuleWithRelease() );
        em = new ExamineManifest( new SystemStreamLog() );
        mf.delete();
        w = new PrintWriter( mf );
        w.println( "Manifest-Version: 1.0" );
        w.flush();
        w.close();
        em.setManifestFile( mf );
        em.setPopulateDependencies( true );
        em.checkFile();
        assertEquals( null, em.getModule() );
        assertEquals( null, em.getModuleWithRelease() );
    }

}
