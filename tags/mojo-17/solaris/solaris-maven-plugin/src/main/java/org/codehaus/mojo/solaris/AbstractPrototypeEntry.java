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
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractPrototypeEntry
{
    private String pkgClass;

    private String path;

    private String mode;

    private String user;

    private String group;

    private String realPath;

    protected AbstractPrototypeEntry()
    {
    }

    protected AbstractPrototypeEntry( String pkgClass, String path, String mode, String user, String group )
    {
        this.pkgClass = pkgClass;
        this.path = path;
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

    public String getPath()
    {
        return path;
    }

    public void setPath( String path )
    {
        this.path = path;
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

    public String getRealPath()
    {
        return realPath;
    }

    public void setRealPath( String realPath )
    {
        this.realPath = realPath;
    }

    // -----------------------------------------------------------------------
    // Object Overrides
    // -----------------------------------------------------------------------

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        AbstractPrototypeEntry that = (AbstractPrototypeEntry) o;

        return path.equals( that.path );
    }

    public int hashCode()
    {
        return path.hashCode();
    }

    public String toString()
    {
        return getPrototypeLine();
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    protected String getProcessedPath()
    {
        if ( realPath == null )
        {
            return getPath();
        }
        else
        {
            return getPath() + "=" + getRealPath();
        }
    }

    public final String getPrototypeLine()
    {
        validate();

        return generatePrototypeLine();
    }

    private void validate()
    {
        if ( path == null )
        {
            throw new RuntimeException( "Missing path in directory entry." );
        }
    }

    public abstract String generatePrototypeLine();
}
