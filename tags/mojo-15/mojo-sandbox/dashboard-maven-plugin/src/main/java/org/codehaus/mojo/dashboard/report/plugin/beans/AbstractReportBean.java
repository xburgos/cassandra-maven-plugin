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

/**
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 *
 */
public abstract class AbstractReportBean implements IDashBoardReportBean, Cloneable
{
    /**
     * 
     */
    private String projectName;
    /**
     * 
     */
    private Date dateGeneration;
    /**
     * 
     */
    private String artefactId;
    /**
     * 
     *
     */
    public AbstractReportBean()
    {
    }
    /**
     * 
     * @param artefactId
     */
    public AbstractReportBean( String artefactId )
    {
        this.artefactId = artefactId;
    }
    /**
     * 
     * @param artefactId
     * @param dateGeneration
     */
    public AbstractReportBean( String artefactId, Date dateGeneration )
    {
        this.artefactId = artefactId;
        this.dateGeneration = dateGeneration;
    }
    /**
     * 
     * @param artefactId
     * @param projectName
     */
    public AbstractReportBean( String artefactId, String projectName )
    {
        this.artefactId = artefactId;
        this.projectName = projectName;
    }
    /**
     * 
     * @param artefactId
     * @param projectName
     * @param dateGeneration
     */
    public AbstractReportBean( String artefactId, String projectName, Date dateGeneration )
    {
        this.artefactId = artefactId;
        this.projectName = projectName;
        this.dateGeneration = dateGeneration;
    }
    /**
     * 
     */
    public String getProjectName()
    {
        return projectName;
    }
    /**
     * 
     */
    public void setProjectName( String projectName )
    {
        this.projectName = projectName;
    }
    
    /* (non-Javadoc)
     * @see org.codehaus.mojo.dashboard.report.plugin.beans.IDashBoardReportBean#getDateGeneration()
     */
    public Date getDateGeneration()
    {
        return dateGeneration;
    }
    /* (non-Javadoc)
     * @see org.codehaus.mojo.dashboard.report.plugin.beans.IDashBoardReportBean#setDateGeneration(java.util.Date)
     */
    public void setDateGeneration( Date dateGeneration )
    {
        this.dateGeneration = dateGeneration;
    }
    /* (non-Javadoc)
     * @see org.codehaus.mojo.dashboard.report.plugin.beans.IDashBoardReportBean#getArtefactId()
     */
    public String getArtefactId()
    {
        return artefactId;
    }
    /* (non-Javadoc)
     * @see org.codehaus.mojo.dashboard.report.plugin.beans.IDashBoardReportBean#setArtefactId(java.lang.String)
     */
    public void setArtefactId( String artefactId )
    {
        this.artefactId = artefactId;
    }
    /**
     * 
     */
    protected Object clone()
    {
        Object clone = null;
        try
        {
            clone = super.clone();
        }
        catch ( CloneNotSupportedException e )
        {
            System.err.println( "AbstractReportBean can't clone" );
        }
        return clone;
    }

}
