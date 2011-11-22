package org.codehaus.mojo.solaris;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Defaults
{
    private String pkgClass;

    private String mode;

    private String user;

    private String group;

    private String[] includes;

    private String[] excludes;

    public Defaults( String pkgClass, String mode, String user, String group, String[] includes, String[] excludes )
    {
        this.pkgClass = pkgClass;
        this.mode = mode;
        this.user = user;
        this.group = group;
        this.includes = includes;
        this.excludes = excludes;
    }

    public String getPkgClass()
    {
        return pkgClass;
    }

    public String getMode()
    {
        return mode;
    }

    public String getUser()
    {
        return user;
    }

    public String getGroup()
    {
        return group;
    }

    public String[] getIncludes()
    {
        return includes;
    }

    public String[] getExcludes()
    {
        return excludes;
    }
}
