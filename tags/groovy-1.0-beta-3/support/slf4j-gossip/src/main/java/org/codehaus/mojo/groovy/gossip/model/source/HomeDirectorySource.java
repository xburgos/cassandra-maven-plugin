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

package org.codehaus.mojo.groovy.gossip.model.source;

import java.io.File;

import org.codehaus.mojo.groovy.gossip.config.ConfigurationException;
import org.codehaus.mojo.groovy.gossip.model.Configuration;
import org.codehaus.mojo.groovy.gossip.model.Source;

/**
 * ???
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class HomeDirectorySource
    extends Source
{
    private String path;
    
    public HomeDirectorySource() {}

    public HomeDirectorySource(final String path) {
        setPath(path);
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public Configuration load() throws Exception {
        if (path == null) {
            throw new ConfigurationException("Missing property: path");
        }

        File homeDir = new File(System.getProperty("user.home"));

        File file = new File(homeDir, getPath());

        return load(file);
    }
}