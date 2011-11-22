/*
 * Copyright 2005 Jeff Genender.
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

package org.codehaus.mojo.jboss;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.*;
import java.nio.channels.FileChannel;

/**
 * Hard deploys the file by copying it to the $JBOSS_HOME/server/[serverName]/deploy directory
 *
 * @author <a href="mailto:jgenender@apache.org">Jeff Genender</a>
 * @goal harddeploy
 * @description Maven 2 JBoss plugin
 */
public class HardDeployMojo extends AbstractJBossMojo {

    /**
     * The name of the file or directory to deploy or undeploy.
     *
     * @parameter expression="${project.build.directory}/${project.build.finalName}.${project.packaging}"
     * @required
     */
    protected String fileName;

    public void execute() throws MojoExecutionException {

        checkConfig();
        try{

            //Fix the ejb packaging to a jar
            String fixedFile = null;
            if (fileName.toLowerCase().endsWith("ejb")){
                fixedFile = fileName.substring(0, fileName.length() - 3) + "jar";
            } else {
                fixedFile = fileName;
            }

            File src = new File(fixedFile);
            File dst = new File(jbossHome + "/server/" + serverName + "/deploy/" + src.getName());
            getLog().info("Copying " + src.getAbsolutePath() + " to " + dst.getAbsolutePath());
            copy(src, dst);
        } catch (Exception e){
            throw new MojoExecutionException( "Mojo error occurred: " + e.getMessage(), e );
        }

    }

    private void copy(File srcDir, File dstDir) throws IOException {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdir();
            }

            String[] children = srcDir.list();
            for (int i = 0; i < children.length; i++) {
                copy(new File(srcDir, children[i]), new File(dstDir, children[i]));
            }
        } else {
            copyFile(srcDir, dstDir);
        }
    }

    private void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}
