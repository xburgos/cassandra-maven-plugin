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

import org.codehaus.mojo.pluginsupport.MojoSupport;

import org.apache.maven.settings.Settings;

/**
 * Support for tool mojos.
 *
 * @version $Id$
 * 
 * @noinspection UnusedDeclaration
 */
public abstract class ToolMojoSupport
    extends MojoSupport
{
    //
    // HACK: For some crazy ass reason, the maven-plugin-plugin will barf with a NPE
    //       during site generation unless a mojo has at least one parameter... so *ucking lame!
    //       So, just inject the settings object to make it shut the *uck up.
    //

    /**
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;
}
