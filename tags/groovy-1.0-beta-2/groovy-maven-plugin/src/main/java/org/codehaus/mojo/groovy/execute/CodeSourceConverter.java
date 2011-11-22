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

package org.codehaus.mojo.groovy.execute;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.AbstractConfigurationConverter;
import org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;
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
        assert logger != null;
        
        this.log = logger;
    }

    public boolean canConvert(final Class type) {
        assert type != null;
        
        return CodeSource.class.isAssignableFrom(type);
    }

    public Object fromConfiguration(final ConverterLookup converterLookup,
                                    final PlexusConfiguration configuration,
                                    final Class type,
                                    final Class baseType,
                                    final ClassLoader classLoader,
                                    final ExpressionEvaluator evaluator,
                                    final ConfigurationListener listener)
            throws ComponentConfigurationException
    {
        try {
            return createCodeSource(configuration, evaluator);
        }
        catch (Exception e) {
            throw new ComponentConfigurationException(e);
        }
    }

    /**
     * Helper to help reduce duplicate puddles of code.
     */
    private static class Helper
    {
        private final ExpressionEvaluator evaluator;

        public Helper(final ExpressionEvaluator evaluator) {
            assert evaluator != null;

            this.evaluator = evaluator;
        }

        //
        // TODO: Check if we really even need to do this evaluate()... seems like the value is already
        //       processed some, as its expanding known things like ${basedir}, which I'd rather it didn't :-(
        //
        
        private String eval(final String expr) throws ExpressionEvaluationException {
            return String.valueOf(evaluator.evaluate(expr));
        }

        public CodeSource asURL(final String expr) throws ExpressionEvaluationException, MalformedURLException {
            return new CodeSource(new URL(eval(expr)));
        }

        public CodeSource asFile(final String expr) throws ExpressionEvaluationException {
            return new CodeSource(new File(eval(expr)));
        }

        public CodeSource asBody(final String value) throws ExpressionEvaluationException {
            return new CodeSource(value); // Do not evaluate; pass asis
        }
    }

    private CodeSource createCodeSource(final PlexusConfiguration configuration, final ExpressionEvaluator evaluator)
        throws Exception
    {
        assert configuration != null;
        assert evaluator != null;
        
        if (configuration.getChildCount() != 0) {
            throw new ComponentConfigurationException("Detected old-style <source> configuration; Please update your POM to use the new style.");
        }

        CodeSource source;
        final Helper helper = new Helper(evaluator);

        final String value = configuration.getValue();
        if (log.isDebugEnabled()) {
            log.debug("Configuration value: \n" + value);
        }
        
        String sourceType = configuration.getAttribute("type");

        // If there is a type hint, then follow what it says
        if (sourceType != null) {
            sourceType = sourceType.trim().toLowerCase();

            log.debug("Using source type hint: " + sourceType);

            if (sourceType.equals(URL_TYPE)) {
                source = helper.asURL(value);
            }
            else if (sourceType.equals(FILE_TYPE)) {
                source = helper.asFile(value);
            }
            else if (sourceType.equals(BODY_TYPE)) {
                source = helper.asBody(value);
            }
            else {
                throw new ComponentConfigurationException("Invalid <source> 'type' attribute value: " + sourceType + "; must be one of 'url', 'file' or 'body'");
            }
        }
        else {
            // Else try to detect from the text; First try a URL
            try {
                source = helper.asURL(value);
            }
            catch (MalformedURLException e) {
                // Wasn't a URL...
                if (value.indexOf("\n") != -1) {
                    // Multi-line strings are probably not files ;-)
                    source = helper.asBody(value);
                }
                else {
                    // And then see if its a valid file ref...
                    CodeSource tmp = helper.asFile(value);
                    if (tmp.getFile().getCanonicalFile().exists()) {
                        source = tmp;
                    }
                    else {
                        // Else its probably a single-line body
                        source = helper.asBody(value);
                    }
                }
            }
        }

        return source;
    }
}