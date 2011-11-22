package org.apache.maven.plugin.eve;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.io.File;
import java.util.List;
import java.util.Iterator;

import org.apache.eve.tools.schema.EveSchemaToolTask;
import org.apache.maven.plugin.AbstractPlugin;
import org.apache.maven.plugin.PluginExecutionRequest;
import org.apache.maven.plugin.PluginExecutionResponse;

import org.codehaus.plexus.util.FileUtils;

/**
 * @goal transform-open-ldap-schemas
 *
 * @requiresDependencyResolution
 *
 * @description Transforms a OpenLDAP schema to a Eve schema bean.
 *
 * @parameter
 *  name="schemaDirectory"
 *  type="java.lang.String"
 *  required="true"
 *  validator=""
 *  expression="#basedir/src/schema"
 *  description=""
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class TransformOpenLDAPSchemasMojo
    extends AbstractPlugin
{
    public void execute( PluginExecutionRequest request, PluginExecutionResponse response )
        throws Exception
    {
        File schemaDirectory = new File( (String) request.getParameter( "schemaDirectory" ) );

        if ( !schemaDirectory.exists() )
        {
            System.err.println( "Schema directory '" + schemaDirectory.getAbsolutePath() + "' doesn't exists." );

            return;
        }

        List schemas = FileUtils.getFiles( schemaDirectory, "**/*.schema", "" );

        for ( Iterator it = schemas.iterator(); it.hasNext(); )
        {
            File file = (File) it.next();

            EveSchemaToolTask schemaTool = new EveSchemaToolTask();

            String owner = "cn=admin,ou=system";

            String packageName = "org.apache.eve.schema.bootstrap" + file.getName();

            String dependencies = "";

            // ----------------------------------------------------------------------
            // Generate the beans
            // ----------------------------------------------------------------------

            schemaTool.setName( file.getAbsolutePath() );

            schemaTool.setOwner( owner );

            schemaTool.setPackage( packageName );

            schemaTool.setDependencies( dependencies );

            schemaTool.execute();
        }
    }
}
