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

import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

/**
 * Plexus converter for {@link CodeSource} objects.
 *
 * @version $Id$
 */
public class CodeSourceConverter
    extends AbstractConfigurationConverter
    implements LogEnabled
{
    private static final String URL_TYPE = "url";

    private static final String FILE_TYPE = "file";

    private static final String BODY_TYPE = "body";

    private Logger log;

    public void enableLogging(final Logger logger) {
        this.log = logger;
    }

    public boolean canConvert(final Class type) {
        return CodeSource.class.isAssignableFrom(type);
    }

    public Object fromConfiguration(final ConverterLookup converterLookup,
                                    final PlexusConfiguration configuration,
                                    final Class type,
                                    final Class baseType,
                                    final ClassLoader classLoader,
                                    final ExpressionEvaluator expressionEvaluator,
                                    final ConfigurationListener listener)
            throws ComponentConfigurationException
    {
        try {
            CodeSource source = null;

            if (configuration.getChildCount() != 0) {
                throw new ComponentConfigurationException("Detected old-style <source> configuration; please update your pom to use the new style.");
            }

            // New-style source
            String value = String.valueOf(expressionEvaluator.evaluate(configuration.getValue()));

            // Support type hint for edge cases
            String sourceType = configuration.getAttribute("type");

            if (sourceType != null) {
                sourceType = sourceType.toLowerCase();

                log.debug("Using source type hint: " + sourceType);

                if (sourceType.equals(URL_TYPE)) {
                    source = new CodeSource(new URL(value));
                }
                else if (sourceType.equals(FILE_TYPE)) {
                    source = new CodeSource(new File(value));
                }
                else if (sourceType.equals(BODY_TYPE)) {
                    source = new CodeSource(value);
                }
                else {
                    throw new ComponentConfigurationException("Invalid <source> type attribute value: " + sourceType + "; must be one of 'url', 'file' or 'body'");
                }
            }
            else {
                // Detect from the text; First try a URL
                try {
                    URL url = new URL(value);
                    source = new CodeSource(url);
                }
                catch (MalformedURLException e) {
                    if (value.indexOf("\n") != -1) {
                        // Multi-line strings are probably not files ;-)
                        source = new CodeSource(configuration.getValue());
                    }
                    else {
                        // Then try a File
                        File file = new File(value);

                        //
                        // NOTE: This isn't very nice, but we catch most cases above for multi-line which should
                        //       minimize any headache caused by this...
                        //

                        if (file.exists()) {
                            source = new CodeSource(file);
                        }
                        else {
                            // Create using unevaluated body
                            source = new CodeSource(configuration.getValue());
                        }
                    }
                }
            }

            // source should not be null here
            return source;
        }
        catch (PlexusConfigurationException e) {
            throw new ComponentConfigurationException(e);
        }
        catch (MalformedURLException e) {
            throw new ComponentConfigurationException(e);
        }
        catch (ExpressionEvaluationException e) {
            throw new ComponentConfigurationException(e);
        }
    }
}