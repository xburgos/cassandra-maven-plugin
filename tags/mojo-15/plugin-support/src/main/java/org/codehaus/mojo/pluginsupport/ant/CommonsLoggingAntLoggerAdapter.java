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

package org.codehaus.mojo.pluginsupport.ant;

import java.io.PrintStream;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.commons.logging.Log;

/**
 * Adapts Ant logging to Jakarta Commons Logging.
 *
 * @version $Id$
 */
public class CommonsLoggingAntLoggerAdapter
    extends DefaultLogger
{
    protected Log log;

    public CommonsLoggingAntLoggerAdapter(final Log log) {
        super();

        assert log != null;

        this.log = log;
    }

    protected void printMessage(final String message, final PrintStream stream, final int priority) {
        assert message != null;
        assert stream != null;

        switch (priority) {
            case Project.MSG_ERR:
                log.error(message);
                break;

            case Project.MSG_WARN:
                log.warn(message);
                break;

            case Project.MSG_INFO:
                log.info(message);
                break;

            case Project.MSG_VERBOSE:
            case Project.MSG_DEBUG:
                log.debug(message);
                break;
        }
    }
}
