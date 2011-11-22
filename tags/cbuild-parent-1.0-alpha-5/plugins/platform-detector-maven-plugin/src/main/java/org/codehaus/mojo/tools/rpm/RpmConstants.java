package org.codehaus.mojo.tools.rpm;

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

import java.util.ArrayList;
import java.util.List;

public final class RpmConstants
{

    public static final List RPM_PACKAGINGS;
    
    public static final List RPM_TYPES;
    
    public static final String RPM_DB_PATH = "rpmDbPath";
    
    static
    {
        List packagings = new ArrayList();
        packagings.add( "rpm" );
        packagings.add( "jrpm" );
        
        RPM_PACKAGINGS = packagings;
        
        RPM_TYPES = packagings;
    }
    
    private RpmConstants()
    {
    }
    
    public static boolean isRpmPackaging( String packaging )
    {
        return RPM_PACKAGINGS.contains( packaging );
    }

    public static boolean isRpmType( String type )
    {
        return RPM_TYPES.contains( type );
    }

}
