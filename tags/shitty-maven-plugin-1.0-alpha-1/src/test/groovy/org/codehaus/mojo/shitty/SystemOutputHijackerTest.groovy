/*
 * Copyright (C) 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package org.codehaus.mojo.shitty

//
// TODO: When the groovy mojo 1.0-beta-3 muck is released drop this and use its version.
//

/**
 * Tests for the {@link SystemOutputHijacker} class.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class SystemOutputHijackerTest
    extends GroovyTestCase
{
    void testInstallWithStream() {
        assert !SystemOutputHijacker.isInstalled()
        
        def buff = new ByteArrayOutputStream()
        def out = new PrintStream(buff)
        
        println 'before'
        
        SystemOutputHijacker.install(out)
        
        assert SystemOutputHijacker.isInstalled()
        assert SystemOutputHijacker.isRegistered()
        
        try {
            print 'hijacked'
        }
        finally {
            SystemOutputHijacker.deregister()
            assert !SystemOutputHijacker.isRegistered()
            
            SystemOutputHijacker.uninstall()
            assert !SystemOutputHijacker.isInstalled()
        }
        
        println 'after'
        
        def msg = new String(buff.toByteArray())
        
        assert msg == 'hijacked'
    }
    
    void testInstallRegisterWithStream() {
        assert !SystemOutputHijacker.isInstalled()
        
        def buff = new ByteArrayOutputStream()
        def out = new PrintStream(buff)
        
        println 'before'
        
        SystemOutputHijacker.install()
        assert SystemOutputHijacker.isInstalled()
        
        SystemOutputHijacker.register(out)
        assert SystemOutputHijacker.isRegistered()
        
        try {
            print 'hijacked'
        }
        finally {
            SystemOutputHijacker.deregister()
            assert !SystemOutputHijacker.isRegistered()
            
            SystemOutputHijacker.uninstall()
            assert !SystemOutputHijacker.isInstalled()
        }
        
        println 'after'
        
        def msg = new String(buff.toByteArray())
        
        assert msg == 'hijacked'
    }
    
    void testDualStreams() {
        assert !SystemOutputHijacker.isInstalled()
        
        def outBuff = new ByteArrayOutputStream()
        def out = new PrintStream(outBuff)
        
        def errBuff = new ByteArrayOutputStream()
        def err = new PrintStream(errBuff)
        
        println 'before'
        System.err.println('BEFORE')
        
        SystemOutputHijacker.install(out, err)
        
        assert SystemOutputHijacker.isInstalled()
        assert SystemOutputHijacker.isRegistered()
        
        try {
            print 'hijacked'
            System.err.print('HIJACKED')
        }
        finally {
            SystemOutputHijacker.deregister()
            assert !SystemOutputHijacker.isRegistered()
            
            SystemOutputHijacker.uninstall()
            assert !SystemOutputHijacker.isInstalled()
        }
        
        println 'after'
        System.err.println('AFTER')
        
        assert 'hijacked' == new String(outBuff.toByteArray())
        assert 'HIJACKED' == new String(errBuff.toByteArray())
    }
    
    void testChildThreads() {
        assert !SystemOutputHijacker.isInstalled()
        
        def buff = new ByteArrayOutputStream()
        def out = new PrintStream(buff)
        
        println 'before'
        
        SystemOutputHijacker.install(out)
        
        assert SystemOutputHijacker.isInstalled()
        assert SystemOutputHijacker.isRegistered()
        
        def task = {
            print 'hijacked'
        }
        
        try {
            print '<'
            
            def t = new Thread(task)
            t.start()
            t.join()
            
            print '>'
        }
        finally {
            SystemOutputHijacker.deregister()
            assert !SystemOutputHijacker.isRegistered()
            
            SystemOutputHijacker.uninstall()
            assert !SystemOutputHijacker.isInstalled()
        }
        
        println 'after'
        
        def msg = new String(buff.toByteArray())
        
        assert msg == '<hijacked>'
    }
    
    void testNestedRegistration() {
        assert !SystemOutputHijacker.isInstalled()
        
        def buff = new ByteArrayOutputStream()
        def out = new PrintStream(buff)
        
        println 'before'
        
        SystemOutputHijacker.install(out)
        
        assert SystemOutputHijacker.isInstalled()
        assert SystemOutputHijacker.isRegistered()
        
        try {
            print 'hijacked'
            
            def childBuff = new ByteArrayOutputStream()
            def childOut = new PrintStream(childBuff)
            
            print '!'
            
            SystemOutputHijacker.register(childOut)
            
            try {
                print 'child'
            }
            finally {
                SystemOutputHijacker.deregister()
            }
            
            assert 'child' == new String(childBuff.toByteArray())
            
            print '!'
        }
        finally {
            SystemOutputHijacker.deregister()
            assert !SystemOutputHijacker.isRegistered()
            
            SystemOutputHijacker.uninstall()
            assert !SystemOutputHijacker.isInstalled()
        }
        
        println 'after'
        
        def msg = new String(buff.toByteArray())
        
        assert msg == 'hijacked!!'
    }
    
    void testToFile() {
        assert !SystemOutputHijacker.isInstalled()
        
        def file = File.createTempFile('test', '.txt')
        def out = new PrintStream(file.newOutputStream())
        
        println 'before'
        
        SystemOutputHijacker.install(out)
        
        assert SystemOutputHijacker.isInstalled()
        assert SystemOutputHijacker.isRegistered()
        
        try {
            print 'hijacked'
            
            def childBuff = new ByteArrayOutputStream()
            def childOut = new PrintStream(childBuff)
            
            print '!'
            
            SystemOutputHijacker.register(childOut)
            
            try {
                print 'child'
            }
            finally {
                SystemOutputHijacker.deregister()
            }
            
            assert 'child' == new String(childBuff.toByteArray())
            
            print '!'
        }
        finally {
            SystemOutputHijacker.deregister()
            assert !SystemOutputHijacker.isRegistered()
            
            SystemOutputHijacker.uninstall()
            assert !SystemOutputHijacker.isInstalled()
        }
        
        out.flush()
        
        println 'after'
        
        assert 'hijacked!!' == file.text
        
        file.delete()
    }
}
