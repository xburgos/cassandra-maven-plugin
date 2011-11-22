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

import java.util.Map;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.mojo.groovy.feature.Version;
import org.codehaus.mojo.groovy.feature.support.ProviderSupport;

/**
 * Provides support for Groovy 1.1.
 *
 * @plexus.component role="org.codehaus.mojo.groovy.feature.Provider" role-hint="1.1"
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class GroovyRuntime_v11
    extends ProviderSupport
{
    public static final String KEY = "1.1";

    /**
     * ???
     * 
     * @plexus.requirement role="org.codehaus.mojo.groovy.feature.Feature#1.1"
     *
     * @noinspection UnusedDeclaration,MismatchedQueryAndUpdateOfCollection
     */
    private Map features;

    public GroovyRuntime_v11() {
        super(KEY);
    }

    protected Map detectFeatures() {
        return features;
    }

    protected Version detectVersion() {
        return new Version(1, 1);
    }

    public String name() {
        return "Groovy v" + InvokerHelper.getVersion();
    }
}