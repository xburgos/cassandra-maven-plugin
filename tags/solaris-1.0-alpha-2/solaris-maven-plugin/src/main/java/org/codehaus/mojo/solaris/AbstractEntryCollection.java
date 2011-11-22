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
public abstract class AbstractEntryCollection
    extends AbstractPrototypeEntry
{
    private String[] includes;

    private String[] excludes;

    public AbstractEntryCollection()
    {
    }

    public AbstractEntryCollection( String pkgClass, String mode, String user, String group,
                                    String[] includes, String[] excludes )
    {
        super( pkgClass, mode, user, group );
        this.includes = includes;
        this.excludes = excludes;
    }

    public String[] getIncludes()
    {
        return includes;
    }

    public String[] getExcludes()
    {
        return excludes;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public void validate( Defaults defaults )
    {
        super.validate( defaults );

        if ( includes == null )
        {
            includes = defaults.getIncludes();
        }

        if ( excludes == null )
        {
            excludes = defaults.getExcludes();
        }
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public String toString()
    {
        StringBuffer builder = new StringBuffer();

        builder.append( getClass().getName() ).append( ": [" ).append( EOL ).
            append( "includes: " ).append( toString( includes ) ).append( EOL ).
            append( "excludes: " ).append( toString( excludes ) ).append( EOL ).
            append( "pkgClass: " ).append( getPkgClass() ).append( EOL ).
            append( "mode: " ).append( getMode() ).append( EOL ).
            append( "user: " ).append( getUser() ).append( EOL ).
            append( "group: " ).append( getGroup() ).append( EOL ).
            append( "]" );

        return builder.toString();
    }

    public static String toString( Object[] a )
    {
        if ( a == null )
        {
            return "null";
        }

        int max = a.length - 1;

        if ( max == -1 )
        {
            return "[]";
        }

        StringBuffer b = new StringBuffer();
        b.append( '[' );
        for ( int i = 0; ; i++ )
        {
            b.append( String.valueOf( a[i] ) );

            if ( i == max )
            {
                return b.append( ']' ).toString();
            }

            b.append( ", " );
        }
    }
}
