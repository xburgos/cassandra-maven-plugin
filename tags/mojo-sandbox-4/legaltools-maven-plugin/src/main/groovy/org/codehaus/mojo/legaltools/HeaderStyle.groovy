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

package org.codehaus.mojo.legaltools

import org.apache.maven.plugin.MojoExecutionException

import org.apache.maven.shared.model.fileset.FileSet

/**
 * ???
 *
 * @version $Id$
 */
class HeaderStyle
{
    String header
    
    FileSet[] sources
    
    int preHeaderIgnoreLines = 0
    
    def validate() {
        if (!header) {
            throw new MojoExecutionException("Missing required style parameter: header")
        }
        if (sources == null || sources.length == 0) {
            throw new MojoExecutionException("Missing required style parameter: sources")
        }
    }
    
    List loadLines() {
        def url = getClass().getResource(header)
        if (!url) {
            throw new MojoExecutionException("Missing header source: $header")
        }
        
        def lines
        url.withReader { lines = it.readLines() }
        
        println '----8<----'
        lines.each {
            println it
        }
        println '---->8----'
        
        return lines
    }
}
