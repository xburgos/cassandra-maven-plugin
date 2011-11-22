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

import groovy.ui.InteractiveShell;

/**
 * Launches the Groovy Shell (aka. <tt>groovysh</tt>).
 *
 * @goal shell
 * @requiresProject false
 * @since 1.0-beta-2
 * 
 * @version $Id$
 *
 * @noinspection UnusedDeclaration
 */
public class ShellMojo
    extends ToolMojoSupport
{
    protected void doExecute() throws Exception {
        InteractiveShell s = new InteractiveShell();
        
        log.info("Launching the Groovy Shell...");
        
        s.run(null); // NOTE: Args are not used
    }
}
