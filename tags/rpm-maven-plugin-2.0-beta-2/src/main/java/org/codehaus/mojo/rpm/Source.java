package org.codehaus.mojo.rpm;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.List;

/**
 * A description of a location where files to be packaged can be found.
 * 
 * @version $Id$
 */
public class Source
{

    // // // Properties

    /** The source location. */
    private File location;

    /** The list of inclusions. */
    private List includes;

    /** The list of exclusions. */
    private List excludes;
    
    /**
     * Optional destination name for the file identified by {@link #location}.<br/>
     * 
     * <b>NOTE:</b> This is only applicable if the {@link #location} is a {@link File#isFile() file},
     * not a {@link File#isDirectory() directory}.
     */
    private String destination;

    /** <code>true</code> to omit the default exclusions. */
    private boolean noDefaultExcludes;
    
    /**
     * If populated, this indicates that the files defined are only applicable if this value matches the
     * <code>RPMMojo.needarch</code> value.
     */
    private String targetArchitecture;

    // // // Bean methods

    /**
     * Retrieve the location holding the file(s) to install.
     * 
     * @return The location holding the file(s) to install.
     */
    public File getLocation()
    {
        return location;
    }

    /**
     * Set the location holding the file(s) to install.
     * 
     * @param loc The new location holding the file(s) to install.
     */
    public void setLocation( File loc )
    {
        location = loc;
    }

    /**
     * Retrieve the list of files to include in the package.
     * 
     * @return The list of files to include in the package.
     */
    public List getIncludes()
    {
        return includes;
    }

    /**
     * Set the list of files to include in the package.
     * 
     * @param incl The new list of files to include in the package.
     */
    public void setIncludes( List incl )
    {
        includes = incl;
    }

    /**
     * Retrieve the list of files to exclude from the package.
     * 
     * @return The list of files to exclude from the package.
     */
    public List getExcludes()
    {
        return excludes;
    }

    /**
     * Set the list of files to exclude from the package.
     * 
     * @param excl The new list of files to exclude from the package.
     */
    public void setExcludes( List excl )
    {
        excludes = excl;
    }

    /**
     * Retrieve the default exclude status.
     * 
     * @return <code>true</code> if the default excludes should be omitted.
     */
    public boolean getNoDefaultExcludes()
    {
        return noDefaultExcludes;
    }

    /**
     * Set the default exclude status.
     * 
     * @param noDefExcl <code>true</code> if the default excludes should be omitted.
     */
    public void setNoDefaultExcludes( boolean noDefExcl )
    {
        noDefaultExcludes = noDefExcl;
    }

    // // // Public methods

    /**
     * @return Returns the {@link #destination}.
     * @see #setDestination(String)
     */
    public String getDestination()
    {
        return this.destination;
    }

    /**
     * Sets the destination file name.
     * <p>
     * <b>NOTE:</b> This is only applicable if the {@link #getLocation() location} is a {@link File#isFile() file},
     * not a {@link File#isDirectory() directory}.
     * </p>
     * 
     * @param destination The destination that the {@link #getLocation() location} should be in the final rpm.
     */
    public void setDestination( String destination )
    {
        this.destination = destination;
    }

    /**
     * @return Returns the {@link #targetArchitecture}.
     */
    public String getTargetArchitecture()
    {
        return this.targetArchitecture;
    }

    /**
     * Sets the target architecture for which files defined by this source are applicable.<br/>
     * 
     * @param targetArch The target architecture to set.
     */
    public void setTargetArchitecture( String targetArch )
    {
        this.targetArchitecture = targetArch;
    }
    
    /**
     * Indicates if the {@link #getTargetArchitecture()} matches the <i>archicture</i>.
     * 
     * @param architecture The target architecture for the rpm.
     * @return if the target architecture matches the <i>archicture</i>.
     */
    boolean matchesArchitecture( String architecture )
    {
        return targetArchitecture == null ? true : targetArchitecture.equalsIgnoreCase( architecture );
    }

    /** {@inheritDoc} */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "{" );

        if ( location == null )
        {
            sb.append( "nowhere" );
        }
        else
        {
            sb.append( "\"" + location + "\"" );
        }

        if ( includes != null )
        {
            sb.append( " incl:" + includes );
        }

        if ( excludes != null )
        {
            sb.append( " excl:" + excludes );
        }
        
        if ( destination != null )
        {
            sb.append( " destination: " );
            sb.append( destination );
        }

        if ( noDefaultExcludes )
        {
            sb.append( " [no default excludes]" );
        }
        
        if ( targetArchitecture != null )
        {
            sb.append( " targetArch: " );
            sb.append( targetArchitecture );
        }

        sb.append( "}" );
        return sb.toString();
    }
}
