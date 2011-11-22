package org.codehaus.mojo.dashboard.report.plugin.beans;

/*
 * Copyright 2006 David Vicente
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


import java.util.Date;

/**
 * 
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 * 
 */
public interface IDashBoardReportBean
{
    /**
     * get the Maven project name
     * @return String
     */
    String getProjectName();
    /**
     * set the Maven project name
     * @param projectName
     */
    void setProjectName( String projectName );
    /**
     * get the generation date of this report
     * @return Date
     */
    Date getDateGeneration();
    /**
     * set the generation date of this report
     * @param dateGeneration
     */
    void setDateGeneration( Date dateGeneration );
    /**
     * get the Maven project ArtefactId
     * @return String
     */
    String getArtefactId();
    /**
     * set the Maven project ArtefactId
     * @param artefactId
     */
    void setArtefactId( String artefactId );

}