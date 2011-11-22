/**
 *
 */
package org.codehaus.mojo.castor;

/*
 * Copyright 2005 The Codehaus.
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
 * A mojo that uses Castor MappingTool to generate mapping files from a single Class.
 * <a href="http://castor.codehaus.org/javadoc/org/exolab/castor/tools/MappingTool.html">
 * MappingTool</a>.
 * 
 * 
 * @goal mapping
 * @phase process-classes
 * @author nicolas <nicolas@apache.org>
 */
public class MappingMojo
    extends AbstractMappingMojo
{
    /**
     * @parameter
     * @required
     */
    private String className;

    /**
     * @parameter
     * @required
     */
    private String mappingName;

    protected String getClassName()
    {
        return className;
    }

    protected String getMappingName()
    {
        return mappingName;
    }
}
