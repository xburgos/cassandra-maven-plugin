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

package org.codehaus.mojo.groovy.tools;

import java.util.EventObject;

import groovy.ui.Console;

import org.apache.maven.settings.Settings;

/**
 * Launches the Groovy GUI console (aka. <tt>groovyConsole</tt>).
 *
 * @goal console
 * @requiresProject false
 * @since 1.0-beta-2
 * 
 * @version $Id$
 *
 * @noinspection UnusedDeclaration
 */
public class ConsoleMojo
    extends ToolMojoSupport
{
    //
    // HACK: For some crazy ass reason, the maven-plugin-plugin will barf with a NPE
    //       during site generation unless this mojo has a parameter... so *ucking lame!
    //       So, just inject the settings object to make it shut the *uck up.
    //
    
    /**
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;
    
    private final Object lock = new Object();
    
    protected void doExecute() throws Exception {
        Console c = new Console() {
            public void exit(final EventObject event) {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        };
        
        log.info("Launching the Groovy Console...");
        
        c.run();
        
        synchronized (lock) {
            lock.wait();
        }

        //
        // FIXME: Looks like when the console window is closed System.exit() maybe called... should fix that
        //
    }
}
