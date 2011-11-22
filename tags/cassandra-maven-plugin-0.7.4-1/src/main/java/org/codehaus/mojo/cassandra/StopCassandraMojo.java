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
package org.codehaus.mojo.cassandra;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Stops a background Cassandra instance.
 *
 * @author stephenc
 * @goal stop
 * @threadSafe
 * @phase post-integration-test
 */
public class StopCassandraMojo extends AbstractMojo
{
    /**
     * Skip the execution.
     *
     * @parameter expression="${cassandra.skip}" default-value="false"
     */
    private boolean skip;

    /**
     * Port to send stop command over
     *
     * @parameter expression="${cassandra.stopPort}" default-value="8081"
     * @required
     */
    protected int stopPort;

    /**
     * Key to provide when stopping cassandra
     *
     * @parameter expression="${cassandra.stopKey}" default-value="cassandra-maven-plugin"
     * @required
     */
    protected String stopKey;

    /**
     * Address to use for the RPC interface. Do not change this unless you really know what you are doing.
     *
     * @parameter default-value="127.0.0.1"
     */
    private String rpcAddress;

    /**
     * Port to listen to for the RPC interface.
     *
     * @parameter expression="${cassandra.rpcPort}" default-value="9160"
     */
    protected int rpcPort;

    /**
     * {@inheritDoc}
     */
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if (skip)
        {
            getLog().info("Skipping cassandra: cassandra.skip==true");
            return;
        }
        if (stopPort <= 0)
        {
            throw new MojoExecutionException("Please specify a valid port");
        }
        if (stopKey == null)
        {
            throw new MojoExecutionException("Please specify a valid stopKey");
        }

        Utils.stopCassandraServer(rpcAddress, rpcPort, stopPort, stopKey, getLog());
    }
}
