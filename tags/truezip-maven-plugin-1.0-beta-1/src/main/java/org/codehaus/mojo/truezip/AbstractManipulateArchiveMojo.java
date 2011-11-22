package org.codehaus.mojo.truezip;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */


import java.util.ArrayList;
import java.util.List;

/**
 * @author Dan T. Tran
 */
public abstract class AbstractManipulateArchiveMojo
    extends AbstractArchiveMojo
{
    
    /**
     * The list of FileSet to manipulate the archive.
     *
     * @parameter
     * @since beta-1
     */
    protected List filesets = new ArrayList( 0 );

    /**
     * A single FileSet to manipulate the archive.
     *
     * @parameter
     * @since beta-1
     */
    protected Fileset fileset;

    
    /**
     * Enable verbose mode
     * @parameter default-value="false"
     * @since beta-1
     */
    protected boolean verbose;
    
}
