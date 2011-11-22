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

/**
 * 
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 * 
 */
public class CheckstyleReportBean extends AbstractReportBean
{
    /**
     * 
     */
    private int nbClasses;
    /**
     * 
     */
    private int nbInfos;
    /**
     * 
     */
    private int nbWarnings;
    /**
     * 
     */
    private int nbErrors;
    /**
     * 
     */
    private int nbTotal;
    /**
     * 
     * @param projectName
     */
    public CheckstyleReportBean( String projectName )
    {
        this.setProjectName( projectName );
    }
    /**
     * 
     * @return int
     */
    public int getNbClasses()
    {
        return nbClasses;
    }
    /**
     * 
     * @param nbClasses
     */
    public void setNbClasses( int nbClasses )
    {
        this.nbClasses = nbClasses;
    }
    /**
     * 
     * @return
     */
    public int getNbErrors()
    {
        return nbErrors;
    }
    /**
     * 
     * @param nbErrors
     */
    public void setNbErrors( int nbErrors )
    {
        this.nbErrors = nbErrors;
    }
    /**
     * 
     * @return
     */
    public int getNbInfos()
    {
        return nbInfos;
    }
    /**
     * 
     * @param nbInfos
     */
    public void setNbInfos( int nbInfos )
    {
        this.nbInfos = nbInfos;
    }
    /**
     * 
     * @return
     */
    public int getNbTotal()
    {
        return nbTotal;
    }
    /**
     * 
     * @param nbTotal
     */
    public void setNbTotal( int nbTotal )
    {
        this.nbTotal = nbTotal;
    }
    /**
     * 
     * @return
     */
    public int getNbWarnings()
    {
        return nbWarnings;
    }
    /**
     * 
     * @param nbWarnings
     */
    public void setNbWarnings( int nbWarnings )
    {
        this.nbWarnings = nbWarnings;
    }
    /**
     * 
     * @param dashboardReport
     */
    public void merge( CheckstyleReportBean dashboardReport )
    {
        if ( dashboardReport != null )
        {
            this.nbClasses = this.nbClasses + dashboardReport.getNbClasses();

            this.nbInfos = this.nbInfos + dashboardReport.getNbInfos();

            this.nbWarnings = this.nbWarnings + dashboardReport.getNbWarnings();

            this.nbErrors = this.nbErrors + dashboardReport.getNbErrors();

            this.nbTotal = this.nbTotal + dashboardReport.getNbTotal();
        }
    }

}
