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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 * 
 */
public class DashBoardMultiReportBean extends AbstractReportBean
{
    /**
     * 
     */
    private List modules = new ArrayList();

    /**
     * 
     */
    private DashBoardReportBean summary;

    /**
     * 
     * @param artefactId
     * @param dateGeneration
     */
    public DashBoardMultiReportBean( String artefactId, Date dateGeneration )
    {
        super( artefactId, dateGeneration );
    }

    /**
     * 
     * @param artefactId
     * @param projectName
     * @param dateGeneration
     */
    public DashBoardMultiReportBean( String artefactId, String projectName, Date dateGeneration )
    {
        super( artefactId, projectName, dateGeneration );
    }

    /**
     * 
     * @param artefactId
     * @param projectName
     */
    public DashBoardMultiReportBean( String artefactId, String projectName )
    {
        super( artefactId, projectName );
    }

    /**
     * 
     * @param artefactId
     */
    public DashBoardMultiReportBean( String artefactId )
    {
        super( artefactId );
    }

    /**
     * 
     * @return
     */
    public List getModules()
    {
        return modules;
    }

    /**
     * 
     * @param modules
     */
    public void setModules( List modules )
    {
        this.modules = modules;
    }
    /**
     * 
     * @param report
     */
    public void addReport( IDashBoardReportBean report )
    {
        this.modules.add( report );
        fillSummary( report );
    }
    /**
     * 
     * @return
     */
    public IDashBoardReportBean getSummary()
    {
        return this.summary;
    }
    /**
     * 
     * @param summary
     */
    public void setSummary( IDashBoardReportBean summary )
    {
        this.summary = (DashBoardReportBean) summary;
    }
    /**
     * 
     * @param dashboardReport
     */
    private void fillSummary( IDashBoardReportBean dashboardReport )
    {

        if ( dashboardReport instanceof DashBoardReportBean )
        {
            if ( summary == null )
            {
                summary = (DashBoardReportBean) ( (DashBoardReportBean) dashboardReport ).clone();
                summary.setArtefactId( this.getArtefactId() );
                summary.setProjectName( this.getProjectName() );
                summary.setDateGeneration( this.getDateGeneration() );
            }
            else
            {
                summary.merge( dashboardReport );
            }

        }
        else
        {
            fillSummary( ( (DashBoardMultiReportBean) dashboardReport ).getSummary() );
        }
    }
}
