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

package org.codehaus.mojo.groovy.runtime.v10;

import groovy.lang.Closure;
import groovy.util.AntBuilder;
import org.codehaus.mojo.groovy.feature.Component;
import org.codehaus.mojo.groovy.feature.ComponentException;
import org.codehaus.mojo.groovy.feature.support.FeatureSupport;
import org.codehaus.mojo.groovy.runtime.ClassFactory;
import org.codehaus.mojo.groovy.runtime.ScriptExecutor;
import org.codehaus.mojo.groovy.runtime.support.ScriptExecutorSupport;
import org.codehaus.mojo.groovy.runtime.util.Callable;
import org.codehaus.mojo.groovy.runtime.util.MagicAttribute;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildLogger;
/**
 * ???
 *
 * @plexus.component role="org.codehaus.mojo.groovy.feature.Feature#1.0"
 *                   role-hint="org.codehaus.mojo.groovy.runtime.ScriptExecutor"
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ScriptExecutorFeature
    extends FeatureSupport
{
    public ScriptExecutorFeature() {
        super(ScriptExecutor.KEY);
    }

    protected Component doCreate() throws Exception {
        return new ScriptExecutorImpl();
    }

    //
    // ScriptExecutorImpl
    //

    private class ScriptExecutorImpl
        extends ScriptExecutorSupport
    {
        private ScriptExecutorImpl() throws Exception {
            super(ScriptExecutorFeature.this);
        }

        protected ClassFactory getClassFactory() {
            try {
                return (ClassFactory) provider().feature(ClassFactory.KEY).create(config());
            }
            catch (Exception e) {
                throw new ComponentException(e);
            }
        }

        protected Object createClosure(final Callable target) {
            assert target != null;

            return new Closure(this) {
                public Object call(final Object[] args) {
                    try {
                        return target.call(args);
                    }
                    catch (Exception e) {
                        return throwRuntimeException(e);
                    }
                }
            };
        }

        private AntBuilder createAntBuilder() {
            AntBuilder ant = new AntBuilder();

            BuildListener listener = (BuildListener) ant.getAntProject().getBuildListeners().elementAt(0);

            if (listener instanceof BuildLogger) {
                BuildLogger logger = (BuildLogger)listener;

                logger.setEmacsMode(true);
            }
            
            return ant;
        }
        
        protected Object createMagicAttribute(final MagicAttribute attr) {
            assert attr != null;

            if (attr == MagicAttribute.ANT_BUILDER) {
                return createAntBuilder();
            }

            throw new ComponentException("Unknown magic attribute: " + attr);
        }
    }
}