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

package org.codehaus.mojo.groovy.runtime.v11;

import groovy.ui.InteractiveShell;
import org.codehaus.groovy.tools.shell.Groovysh;
import org.codehaus.groovy.tools.shell.IO;
import org.codehaus.groovy.tools.shell.Main;
import org.codehaus.groovy.tools.shell.util.Logger;
import org.codehaus.mojo.groovy.feature.Component;
import org.codehaus.mojo.groovy.feature.Configuration;
import org.codehaus.mojo.groovy.feature.support.ComponentSupport;
import org.codehaus.mojo.groovy.feature.support.FeatureSupport;
import org.codehaus.mojo.groovy.runtime.Shell;
import org.codehaus.mojo.groovy.runtime.support.util.NoExitSecurityManager;
import org.codehaus.mojo.groovy.util.StreamPair;

/**
 * ???
 *
 * @plexus.component role="org.codehaus.mojo.groovy.feature.Feature#1.1"
 *                   role-hint="org.codehaus.mojo.groovy.runtime.Shell"
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ShellFeature
    extends FeatureSupport
{
    public ShellFeature() {
        super(Shell.KEY);
    }

    protected Component doCreate() throws Exception {
        return new ShellImpl();
    }

    //
    // ShellImpl
    //
    
    private class ShellImpl
        extends ComponentSupport
        implements Shell, Shell.Keys
    {
        private ShellImpl() throws Exception {
            super(ShellFeature.this);
        }

        public void execute() throws Exception {
            boolean legacy = config().get(LEGACY, false);

            final StreamPair streams = StreamPair.system();

            // Put a nice blank before and after we run the shell
            streams.out.println();

            SecurityManager sm = System.getSecurityManager();
            System.setSecurityManager(new NoExitSecurityManager());

            try {
                if (!legacy) {
                    new DefaultTask(config()).run();
                }
                else {
                    new LegacyTask().run();
                }
            }
            finally {
                System.setSecurityManager(sm);

                StreamPair.system(streams);
            }

            // The blank after
            streams.out.println();
        }
    }

    //
    // Task
    //

    private interface Task
    {
        void run() throws Exception;
    }

    //
    // DefaultTask
    //

    private class DefaultTask
        implements Shell.Keys, Task
    {
        private final IO io;

        private final String args;

        public DefaultTask(final Configuration config) {
            assert config != null;

            io = new IO();
            
            Logger.io = io;

            if (config.get(VERBOSE, false)) {
                io.setVerbosity(IO.Verbosity.VERBOSE);
            }

            if (config.get(DEBUG, false)) {
                io.setVerbosity(IO.Verbosity.DEBUG);
            }

            if (config.get(QUIET, false)) {
                io.setVerbosity(IO.Verbosity.QUIET);
            }

            String color = config.get(COLOR, Boolean.TRUE.toString());
            if (color != null) {
                Main.setColor(color);
            }

            String term = config.get(TERMINAL, (String)null);
            if (term != null) {
                Main.setTerminalType(term);
            }

            args = config.get(ARGS, (String)null);
        }
        
        public void run() throws Exception {
            Groovysh shell = new Groovysh(io);
            
            shell.run(args);
        }
    }

    //
    // LegacyTask
    //
    
    private class LegacyTask
        implements Task
    {
        public void run() throws Exception {
            InteractiveShell shell = new InteractiveShell();
            shell.run();
        }
    }
}