/*
 * Copyright (C) 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package some.thing

import a
import a.b
import a.b.c.*

import org.apache.maven.project.MavenProject

/**
 * This is a mojo.
 *
 * @goal mymojo
 */
class SomeClass
    extends MySuperClass
{
    /**
     * @parameter expression="${mymojo.flag}" default-value="false"
     */
    boolean flag

    /**
     * @parameter expression="${project}"
     */
    MavenProject project

    String[] messages
    
    def untyped
}