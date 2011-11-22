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

import org.apache.maven.plugin.MojoFailureException;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PrototypeEntryCollection
{
    public static final String TYPE_FILE = "file";

    public static final String TYPE_DIR = "dir";

    public static final String TYPE_EDITABLE = "editable";

    private String[] includes;

    private String[] excludes;

    private String type;

    private String pkgClass;

    private String mode;

    private String user;

    private String group;

    public PrototypeEntryCollection()
    {
    }

    public PrototypeEntryCollection( String type, String user, String group, String mode )
    {
        this.type = type;
        this.user = user;
        this.group = group;
        this.mode = mode;
    }

    public PrototypeEntryCollection( String[] includes, String[] excludes, String type, String pkgClass, String mode,
                                     String user, String group )
    {
        this.includes = includes;
        this.excludes = excludes;
        this.type = type;
        this.pkgClass = pkgClass;
        this.mode = mode;
        this.user = user;
        this.group = group;
    }

    public String[] getIncludes()
    {
        return includes;
    }

    public String[] getExcludes()
    {
        return excludes;
    }

    public String getType()
    {
        return type;
    }

    public String getPkgClass()
    {
        return pkgClass;
    }

    public void setClass( String pkgClass )
    {
        this.pkgClass = pkgClass;
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

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public static String[] asList( String string )
    {
        return new String[]{string};
    }

    public void validate( PrototypeEntryCollection defaultDirectoryEntry, PrototypeEntryCollection defaultFileEntry )
        throws MojoFailureException
    {
        PrototypeEntryCollection defaultEntryCollection;

        if ( type == null )
        {
            throw new MojoFailureException( "Missing type." );
        }

        if ( TYPE_DIR.equals( type ) )
        {
            defaultEntryCollection = defaultDirectoryEntry;
        }
        else if ( TYPE_FILE.equals( type ) || TYPE_EDITABLE.equals( type ) )
        {
            defaultEntryCollection = defaultFileEntry;
        }
        else
        {
            throw new MojoFailureException( "Unknown type: " + type );
        }

        if ( includes == null )
        {
            includes = defaultEntryCollection.getIncludes();
        }

        if ( excludes == null )
        {
            excludes = defaultEntryCollection.getExcludes();
        }

        if ( pkgClass == null )
        {
            pkgClass = defaultEntryCollection.getPkgClass();
        }

        if ( mode == null )
        {
            mode = defaultEntryCollection.getMode();
        }

        if ( user == null )
        {
            user = defaultEntryCollection.getUser();
        }

        if ( group == null )
        {
            group = defaultEntryCollection.getGroup();
        }
    }
}
