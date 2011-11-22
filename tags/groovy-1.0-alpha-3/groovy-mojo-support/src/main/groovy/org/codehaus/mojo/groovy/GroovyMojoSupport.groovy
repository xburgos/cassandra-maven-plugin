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

package org.codehaus.mojo.groovy

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException

/**
 * Provides support for Maven 2 plugins implemented in Groovy.
 *
 * @version $Id$
 */
abstract class GroovyMojoSupport
    extends AbstractMojo
{
    //
    // NOTE: Using full packagename for clarity, this is our custom AntBuilder
    //

    private org.codehaus.mojo.groovy.AntBuilder ant

    protected org.codehaus.mojo.groovy.AntBuilder getAnt() {
        // Lazilly initialize the AntBuilder, so we can pick up the log impl correctly
        if (ant == null) {
            ant = new org.codehaus.mojo.groovy.AntBuilder(log)
        }
        return ant
    }

    protected def fail(msg) {
        assert msg
        
        if (msg instanceof Throwable) {
            fail(msg.message, msg);
        }
        throw new MojoExecutionException("$msg")
    }

    protected def fail(msg, Throwable cause) {
        assert msg
        assert cause
        
        throw new MojoExecutionException("$msg", cause)
    }
}
