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

import java.net.URL;
import java.util.Iterator;

import org.codehaus.mojo.groovy.feature.Component;
import org.codehaus.mojo.groovy.feature.support.FeatureSupport;
import org.codehaus.mojo.groovy.runtime.StubCompiler;
import org.codehaus.mojo.groovy.runtime.support.CompilerSupport;

/**
 * ???
 *
 * @plexus.component role="org.codehaus.mojo.groovy.feature.Feature#1.1"
 *                   role-hint="org.codehaus.mojo.groovy.runtime.StubCompiler"
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class StubCompilerFeature
    extends FeatureSupport
{
    public StubCompilerFeature() {
        super(StubCompiler.KEY);
    }

    protected Component doCreate() throws Exception {
        return new StubCompilerImpl();
    }

    //
    // StubCompilerImpl
    //

    private class StubCompilerImpl
        extends CompilerSupport
        implements StubCompiler
    {
        private StubCompilerImpl() throws Exception {
            super(StubCompilerFeature.this);
        }

        public int compile() throws Exception {
            if (sources.isEmpty()) {
                log.debug("No sources added to compile; skipping");

                return 0;
            }

            QDoxStubTranslator translator = new QDoxStubTranslator();

            log.debug("Compiling {} stubs for source(s)", String.valueOf(sources.size()));

            int count = 0;

            for (Iterator iter = sources.iterator(); iter.hasNext();) {
                URL url = (URL) iter.next();

                log.debug("    {}", url);

                count += translator.write(url, getTargetDirectory());
            }

            log.debug("Compiled {} stubs", String.valueOf(count));

            return count;
        }
    }
}