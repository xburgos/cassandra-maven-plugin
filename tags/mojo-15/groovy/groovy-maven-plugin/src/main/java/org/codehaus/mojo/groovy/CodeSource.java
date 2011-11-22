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

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;

import groovy.lang.GroovyCodeSource;

/**
 * Configuration for a scripts code-source and factory for the Groovy code-source for script execution.
 *
 * @version $Id$
 */
public class CodeSource
{
    protected URL url;

    protected File file;

    protected String body;

    public CodeSource(final URL url) {
        this.url = url;
    }

    public CodeSource(final File file) {
        this.file = file;
    }

    public CodeSource(final String body) {
        this.body = body;
    }

    protected CodeSource() {
        super();
    }
    
    public String toString() {
        return "{ url: " + url +
               ", file: " + file +
               ", body: " + body +
               " }";
    }

    public URL getUrl() {
        return url;
    }

    public File getFile() {
        return file;
    }

    public String getBody() {
        return body;
    }

    public void validate() throws MojoExecutionException {
        if (url == null && file == null && (body == null || body.trim().length() == 0)) {
            throw new MojoExecutionException("Must specify one of: file, url or body");
        }

        int count = 0;
        if (url != null) {
            count++;
        }
        if (file != null) {
            count++;
        }
        if (body != null) {
            count++;
        }

        if (count != 1) {
            throw new MojoExecutionException("Can only specify one of: file, url or body");
        }
    }
    
    public GroovyCodeSource toGroovyCodeSource() throws Exception {
        validate();
        
        GroovyCodeSource gcs;
        
        if (body != null) {
            gcs = new GroovyCodeSource(body, "script" + System.currentTimeMillis() + ".groovy", "/groovy/script");
        }
        else {
            if (file != null) {
                gcs = new GroovyCodeSource(file.toURL());
            }
            else {
                assert url != null;
                gcs = new GroovyCodeSource(url);
            }
        }
        
        return gcs;
    }
}
