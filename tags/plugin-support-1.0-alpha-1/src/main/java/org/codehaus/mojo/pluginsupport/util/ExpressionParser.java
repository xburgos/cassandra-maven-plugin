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

package org.codehaus.mojo.pluginsupport.util;

import java.util.Map;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.jexl.resolver.FlatResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Parses expressions using <a href="http://jakarta.apache.org/commons/jexl/">Commons Jexl</a>.
 *
 * @version $Id$
 */
public class ExpressionParser
{
    private static final Log log = LogFactory.getLog(ExpressionParser.class);

    protected JexlContext context;

    public ExpressionParser(final Map vars) {
        if (vars == null) {
            throw new IllegalArgumentException("vars");
        }

        context = JexlHelper.createContext();
        context.setVars(vars);

        if (log.isTraceEnabled()) {
            log.trace("Using variables: " + context.getVars());
        }
    }

    public ExpressionParser() {
        this(System.getProperties());
    }

    public Map getVariables() {
        return context.getVars();
    }

    public Object getVariable(final Object name) {
        if (name == null) {
            throw new IllegalArgumentException("name");
        }

        return getVariables().get(name);
    }

    public Object setVariable(final Object name, final Object value) {
        if (name == null) {
            throw new IllegalArgumentException("name");
        }

        return getVariables().put(name, value);
    }

    public Object unsetVariable(final Object name) {
        if (name == null) {
            throw new IllegalArgumentException("name");
        }

        return getVariables().remove(name);
    }

    public void addVariables(final Map map) {
        if (map == null) {
            throw new IllegalArgumentException("map");
        }

        getVariables().putAll(map);
    }

    private FlatResolver resolver = new FlatResolver(true);

    protected Expression createExpression(final String expression) throws Exception {
        // assert expression != null;

        Expression expr = ExpressionFactory.createExpression(expression);
        expr.addPreResolver(resolver);

        return expr;
    }

    public Object evaluate(final String expression) throws Exception {
        if (expression == null) {
            throw new IllegalArgumentException("expression");
        }

        boolean trace = log.isTraceEnabled();
        if (trace) {
            log.trace("Evaluating expression: " + expression);
        }

        Expression expr = createExpression(expression);
        Object obj = expr.evaluate(context);
        if (trace) {
            log.trace("Result: " + obj);
        }

        return obj;
    }

    public String parse(final String input) {
        if (input == null) {
            throw new IllegalArgumentException("input");
        }

        boolean trace = log.isTraceEnabled();
        if (trace) {
            log.trace("Parsing input: " + input);
        }

        StringBuffer buff = new StringBuffer();

        int cur = 0;
        int prefixLoc;
        int suffixLoc;

        while (cur < input.length()) {
            prefixLoc = input.indexOf("${", cur);

            if (prefixLoc < 0) {
                break;
            }

            suffixLoc = input.indexOf("}", prefixLoc);
            if (suffixLoc < 0) {
                throw new RuntimeException("Missing '}': " + input);
            }

            String expr = input.substring(prefixLoc + 2, suffixLoc);
            buff.append(input.substring(cur, prefixLoc));

            try {
                buff.append(evaluate(expr));
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to evaluate: " + expr, e);
            }

            cur = suffixLoc + 1;
        }

        buff.append(input.substring(cur));

        if (trace) {
            log.trace("Parsed result: " + buff);
        }

        return buff.toString();
    }

    public String parse(final String input, final boolean trim) {
        String output = parse(input);
        if (trim && output != null) {
            output = output.trim();
        }

        return output;
    }
}
