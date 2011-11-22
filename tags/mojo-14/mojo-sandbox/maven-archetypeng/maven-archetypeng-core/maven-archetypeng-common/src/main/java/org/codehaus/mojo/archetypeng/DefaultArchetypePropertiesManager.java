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

/*
 * DefaultArchetypePropertiesManager.java
 *
 * Created on 15 février 2007, 22:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codehaus.mojo.archetypeng;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Iterator;
import java.util.Properties;

/**
 * @plexus.component
 */
public class DefaultArchetypePropertiesManager
extends AbstractLogEnabled
implements ArchetypePropertiesManager
{
    public void readProperties ( Properties properties, File propertyFile )
    throws FileNotFoundException, IOException
    {
        FileReader propertyReader = new FileReader ( propertyFile );

        try
        {
            properties.load ( propertyReader );
        }
        finally
        {
            IOUtil.close ( propertyReader );
        }
    }

    public void writeProperties ( Properties properties, File propertyFile )
    throws IOException
    {
        Properties storedProperties = new Properties ();
        try
        {
            readProperties ( storedProperties, propertyFile );
        }
        catch ( FileNotFoundException ex )
        {
            // ignore and create a new file
        }

        Iterator propertiesIterator = properties.keySet ().iterator ();
        while ( propertiesIterator.hasNext () )
        {
            String propertyKey = (String) propertiesIterator.next ();
            storedProperties.setProperty ( propertyKey, properties.getProperty ( propertyKey ) );
        }

        FileWriter propertyWriter = new FileWriter ( propertyFile );

        try
        {
            storedProperties.store ( propertyWriter, "" );
        }
        finally
        {
            IOUtil.close ( propertyWriter );
        }
    }
}
