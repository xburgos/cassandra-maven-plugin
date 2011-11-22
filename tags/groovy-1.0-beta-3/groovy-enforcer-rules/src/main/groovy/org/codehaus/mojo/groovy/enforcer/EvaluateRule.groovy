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

package org.codehaus.mojo.groovy.enforcer

import org.apache.maven.shared.enforcer.rule.api.EnforcerRule
import org.apache.maven.shared.enforcer.rule.api.EnforcerRuleHelper

import org.codehaus.mojo.groovy.feature.ProviderManager
import org.codehaus.mojo.groovy.feature.Component
import org.codehaus.mojo.groovy.feature.Configuration
import org.codehaus.mojo.groovy.feature.Feature

import org.codehaus.mojo.groovy.runtime.ScriptExecutor
import org.codehaus.mojo.groovy.runtime.util.Callable
import org.codehaus.mojo.groovy.runtime.util.ClassSource
import org.codehaus.mojo.groovy.runtime.util.MagicAttribute
import org.codehaus.mojo.groovy.runtime.util.ResourceLoader
import org.codehaus.mojo.groovy.runtime.support.util.ResourceLoaderImpl

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * ???
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class EvaluateRule
    implements EnforcerRule
{
    private final Logger log = LoggerFactory.getLogger(getClass())

    /**
     * A comma-seperated list of provider keys, in order of preference of selection.
     *
     * @parameter
     * @required
     */
    String providerSelection = 'default'
    
    /**
     * The condition to be evaluated.
     * 
     * @parameter
     * @required
     */
    String condition

    //
    // TODO: Add pass and fail function/eval support?
    //
    
    void execute(final EnforcerRuleHelper context) {
        def manager = context.getComponent(ProviderManager.class)
        log.trace('Provider manager: ', manager)

        log.debug('Selecting provider matching: ', providerSelection)
        def provider = manager.select(providerSelection)
        log.trace('Provider: ', provider)
        
        
        //
        // TODO: ...
        //
        
        
        /*
        ClassSource classSource = ClassSource.forValue(condition)
        
        // Use the providers class-loader as a parent to resolve the Groovy runtime correctly
        ClassLoader parent = provider.getClass().getClassLoader()
        
        URL[] classPath = []
        URLClassLoader classLoader = new URLClassLoader(classPath, parent)
        ResourceLoader resourceLoader = null // new MojoResourceLoader(classLoader, classSource)
        
        // Execute feature w/in the runtime CL
        def tcl = Thread.currentThread().contextClassLoader
        Thread.currentThread().contextClassLoader = provider.getClass().classLoader

        try { 
            Feature feature = provider.feature(ScriptExecutor.class)

            Configuration ctx = new Configuration()
            configure(ctx)

            def executor = feature.create(ctx)
            def result = executor.execute(classSource, classLoader, resourceLoader, ctx)
        }
        finally {
            Thread.currentThread().contextClassLoader = tcl
        }
        */
    }
}
