package org.codehaus.mojo.tomcat.log;

/*
 * Copyright 2006 Mark Hobson.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;

/**
 * A JCL log factory implementation that delegates to a Maven log.
 * 
 * <p>
 * The Maven log that logs produced by this class delegate to is configured by setting the log factory attribute
 * <code>maven.log</code>.
 * </p>
 * 
 * @author Mark Hobson <markhobson@gmail.com>
 */
public class MavenLogFactory extends LogFactory
{
    // ----------------------------------------------------------------------
    // Fields
    // ----------------------------------------------------------------------

    /**
     * The map of log factory attributes keyed by their name.
     * 
     * @todo Ideally this wouldn't be static, although since JCL instantiates a new LogFactory per context classloader
     *       we need a way of propagating the Maven log to all log factories.
     */
    private static final Map attributesByName = new HashMap();

    // ----------------------------------------------------------------------
    // LogFactory Methods
    // ----------------------------------------------------------------------

    /*
     * @see org.apache.commons.logging.LogFactory#getAttribute(java.lang.String)
     */
    public Object getAttribute( String name )
    {
        return attributesByName.get( name );
    }

    /*
     * @see org.apache.commons.logging.LogFactory#getAttributeNames()
     */
    public String[] getAttributeNames()
    {
        Set names = attributesByName.keySet();

        return (String[]) names.toArray( new String[names.size()] );
    }

    /*
     * @see org.apache.commons.logging.LogFactory#getInstance(java.lang.Class)
     */
    public Log getInstance( Class clazz ) throws LogConfigurationException
    {
        return getInstance( clazz.getName() );
    }

    /*
     * @see org.apache.commons.logging.LogFactory#getInstance(java.lang.String)
     */
    public Log getInstance( String name ) throws LogConfigurationException
    {
        return new MavenLog( this );
    }

    /*
     * @see org.apache.commons.logging.LogFactory#release()
     */
    public void release()
    {
        // no-op
    }

    /*
     * @see org.apache.commons.logging.LogFactory#removeAttribute(java.lang.String)
     */
    public void removeAttribute( String name )
    {
        attributesByName.remove( name );
    }

    /*
     * @see org.apache.commons.logging.LogFactory#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute( String name, Object value )
    {
        attributesByName.put( name, value );
    }
}
