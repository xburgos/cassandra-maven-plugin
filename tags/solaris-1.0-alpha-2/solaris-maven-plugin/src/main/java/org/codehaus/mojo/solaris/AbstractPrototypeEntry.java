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
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractPrototypeEntry
{
    static final String EOL = System.getProperty( "line.separator" );

    private String pkgClass;

    private String mode;

    private String user;

    private String group;

    protected AbstractPrototypeEntry()
    {
    }

    protected AbstractPrototypeEntry( String pkgClass, String mode, String user, String group )
    {
        this.pkgClass = pkgClass;
        this.mode = mode;
        this.user = user;
        this.group = group;
    }

    public String getPkgClass()
    {
        return pkgClass == null ? "none" : pkgClass;
    }

    public void setPkgClass( String pkgClass )
    {
        this.pkgClass = pkgClass;
    }

    public void setClass( String clazz )
    {
        setPkgClass( clazz );
    }

    public String getMode()
    {
        return mode;
    }

    public void setMode( String mode )
    {
        this.mode = mode;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser( String user )
    {
        this.user = user;
    }

    public String getGroup()
    {
        return group;
    }

    public void setGroup( String group )
    {
        this.group = group;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public void validate( Defaults defaults )
    {
        if ( pkgClass == null )
        {
            this.pkgClass = defaults.getPkgClass();
        }

        if ( mode == null )
        {
            this.mode = defaults.getMode();
        }

        if ( user == null )
        {
            this.user = defaults.getUser();
        }

        if ( group == null )
        {
            this.group = defaults.getGroup();
        }
    }
}
