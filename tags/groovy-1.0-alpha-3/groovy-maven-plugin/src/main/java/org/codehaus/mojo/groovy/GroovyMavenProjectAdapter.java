/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.codehaus.mojo.groovy;

import java.util.Properties;
import java.util.Map;

import org.apache.maven.project.MavenProject;
import org.apache.maven.execution.MavenSession;

import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

/**
 * Provides property resolution access to Groovy executions.
 *
 * @version $Id$
 */
public class GroovyMavenProjectAdapter
    extends MavenProject
{
    private final MavenSession session;

    private final Map properties;

    private final Map defaults;
    
    private Properties props;

    public GroovyMavenProjectAdapter(final MavenProject project, final MavenSession session, final Map properties, final Map defaults) {
        super(project);

        // Copy constructor disowns its parent, so re-establish again here
        setParent(project.getParent());
        
        this.session = session;
        this.properties = properties;
        this.defaults = defaults;
    }

    public synchronized Properties getProperties() {
        // Lazily construct a custom properties class to handle resolving properties as we want them
        if (props == null) {
            props = new EvaluatingProperties();
        }

        return props;
    }

    /**
     * Custom properties handling to resolve for Groovy executions.
     */
    private class EvaluatingProperties
        extends Properties
    {
        private final ExpressionEvaluator evaluator = new ExpressionEvaluatorImpl(session, GroovyMavenProjectAdapter.this);

        public EvaluatingProperties() {
            // Populate the base properties from the original model properties (so iter-based operations work as expected)
            putAll(getModel().getProperties());

            // Add custom execution properties
            if (properties != null) {
                putAll(properties);
            }
        }

        private Object lookup(final Object key) {
            // First try ourself (pom + custom)
            Object value = super.get(key);

            // Then try execution (system) properties
            if (value == null) {
                value = session.getExecutionProperties().get(key);
            }

            // Then try defaults (from adapter, not from properties, which is not used)
            if (value == null && GroovyMavenProjectAdapter.this.defaults != null) {
                value = GroovyMavenProjectAdapter.this.defaults.get(key);
            }

            return value;
        }

        private Object get(final Object key, final boolean resolve) {
            Object value = lookup(key);

            // If the value is a string, evaluate it to get expressions to expand
            if (resolve && value instanceof String) {
                try {
                    value = evaluator.evaluate((String)value);
                }
                catch (ExpressionEvaluationException e) {
                    // If something bad happens just puke it up
                    throw new RuntimeException(e);
                }
            }

            return value;
        }

        public Object get(final Object key) {
            return get(key, true);
        }

        public String getProperty(final String name) {
            // We have to override getProperty() as the default impl gets the value from super.get() instead of get()
            Object value = get(name);

            return value != null ? String.valueOf(value) : null;
        }
    }
}