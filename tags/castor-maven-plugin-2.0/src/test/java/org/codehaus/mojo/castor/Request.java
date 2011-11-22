package org.codehaus.mojo.castor;

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
 * @author nicolas <nicolas@apache.org>
 */
public class Request
{
    private String name;

    private String id;

    private String value;

    protected String getName()
    {
        return name;
    }

    protected void setName( String name )
    {
        this.name = name;
    }

    protected String getId()
    {
        return id;
    }

    protected void setId( String id )
    {
        this.id = id;
    }

    protected String getValue()
    {
        return value;
    }

    protected void setValue( String value )
    {
        this.value = value;
    }

}
