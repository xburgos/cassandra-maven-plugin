package org.apache.maven.changes;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Represents a release as defined in the <tt>changes.xml</tt> file.
 *
 * @author <a href="snicoll@apache.org">Stephane Nicoll</a>
 * @version $Id$
 */
public final class Release
{

    private final String version;

    private final String releaseDate;

    private final String description;

    private final Action[] actions;

    public Release( String version, String date, String description, Action[] actions )
    {
        this.version = version;
        this.releaseDate = date;
        this.description = description;
        this.actions = actions;
    }

    /**
     * Returns the version of the release.
     *
     * @return the release version
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Returns the release date.
     *
     * @return the date of the release
     */
    public String getReleaseDate()
    {
        return releaseDate;
    }

    /**
     * Returns the release description.
     *
     * @return the description of the release
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Returns the actions associated with the release.
     *
     * @return the actions of the release.
     */
    public Action[] getActions()
    {
        return actions;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "release[" ).append( getVersion() ).append( "] on [" ).append( getReleaseDate() ).append( "]" );
        return sb.toString();
    }

}
