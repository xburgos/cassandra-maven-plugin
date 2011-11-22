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
public class DashBoardReportBean extends AbstractReportBean
{
    /**
     * 
     */
    private CheckstyleReportBean checkStyleReport;
    /**
     * 
     */
    private CoberturaReportBean coberturaReport;
    /**
     * 
     */
    private CpdReportBean cpdReport;
    /**
     * 
     */
    private JDependReportBean jDependReport;
    /**
     * 
     */
    private PmdReportBean pmdReport;
    /**
     * 
     */
    private SurefireReportBean surefireReport;
    
    /**
     * 
     * @param artefactId
     * @param dateGeneration
     */
    public DashBoardReportBean( String artefactId, Date dateGeneration )
    {
        super( artefactId , dateGeneration );
    }
    /**
     * 
     * @param artefactId
     * @param projectName
     * @param dateGeneration
     */
    public DashBoardReportBean( String artefactId, String projectName, Date dateGeneration )
    {
        super( artefactId , projectName , dateGeneration );
    }
    /**
     * 
     * @param artefactId
     * @param projectName
     */
    public DashBoardReportBean( String artefactId, String projectName )
    {
        super( artefactId , projectName );
    }
    /**
     * 
     * @param artefactId
     */
    public DashBoardReportBean( String artefactId )
    {
        super( artefactId ); 
    }
    /**
     * 
     * @return
     */
    public CheckstyleReportBean getCheckStyleReport()
    {
        return checkStyleReport;
    }
    /**
     * 
     * @param checkStyleReport
     */
    public void setCheckStyleReport( CheckstyleReportBean checkStyleReport )
    {
        this.checkStyleReport = checkStyleReport;
    }
    /**
     * 
     * @return
     */
    public CoberturaReportBean getCoberturaReport()
    {
        return coberturaReport;
    }
    /**
     * 
     * @param coberturaReport
     */
    public void setCoberturaReport( CoberturaReportBean coberturaReport )
    {
        this.coberturaReport = coberturaReport;
    }
    /**
     * 
     * @return
     */
    public CpdReportBean getCpdReport()
    {
        return cpdReport;
    }
    /**
     * 
     * @param cpdReport
     */
    public void setCpdReport( CpdReportBean cpdReport )
    {
        this.cpdReport = cpdReport;
    }
    /**
     * 
     * @return
     */
    public JDependReportBean getJDependReport()
    {
        return jDependReport;
    }
    /**
     * 
     * @param dependReport
     */
    public void setJDependReport( JDependReportBean dependReport )
    {
        jDependReport = dependReport;
    }
    /**
     * 
     * @return
     */
    public PmdReportBean getPmdReport()
    {
        return pmdReport;
    }
    /**
     * 
     * @param pmdReport
     */
    public void setPmdReport( PmdReportBean pmdReport )
    {
        this.pmdReport = pmdReport;
    }
    /**
     * 
     * @return
     */
    public SurefireReportBean getSurefireReport()
    {
        return surefireReport;
    }
    /**
     * 
     * @param surefireReport
     */
    public void setSurefireReport( SurefireReportBean surefireReport )
    {
        this.surefireReport = surefireReport;
    }
    /**
     * 
     * @param dashboardReport
     */
    public void merge( IDashBoardReportBean dashboardReport )
    {

        if ( ( (DashBoardReportBean) dashboardReport ).getCheckStyleReport() != null )
        {
            if ( this.checkStyleReport != null )
            {
                this.checkStyleReport.merge( ( (DashBoardReportBean) dashboardReport ).getCheckStyleReport() );
            }
            else
            {
                this.checkStyleReport =
                    (CheckstyleReportBean) ( (DashBoardReportBean) dashboardReport ).getCheckStyleReport().clone();
                this.checkStyleReport.setArtefactId( this.getArtefactId() );
                this.checkStyleReport.setProjectName( this.getProjectName() );
                this.checkStyleReport.setDateGeneration( this.getDateGeneration() );
            }
        }
        if ( ( (DashBoardReportBean) dashboardReport ).getCoberturaReport() != null )
        {
            if ( this.coberturaReport != null )
            {
                this.coberturaReport.merge( ( (DashBoardReportBean) dashboardReport ).getCoberturaReport() );
            }
            else
            {
                this.coberturaReport =
                    (CoberturaReportBean) ( (DashBoardReportBean) dashboardReport ).getCoberturaReport().clone();
                this.coberturaReport.setArtefactId( this.getArtefactId() );
                this.coberturaReport.setProjectName( this.getProjectName() );
                this.coberturaReport.setDateGeneration( this.getDateGeneration() );
            }
        }
        if ( ( (DashBoardReportBean) dashboardReport ).getCpdReport() != null )
        {
            if ( this.cpdReport != null )
            {
                this.cpdReport.merge( ( (DashBoardReportBean) dashboardReport ).getCpdReport() );
            }
            else
            {
                this.cpdReport = (CpdReportBean) ( (DashBoardReportBean) dashboardReport ).getCpdReport().clone();
                this.cpdReport.setArtefactId( this.getArtefactId() );
                this.cpdReport.setProjectName( this.getProjectName() );
                this.cpdReport.setDateGeneration( this.getDateGeneration() );
            }
        }
        if ( ( (DashBoardReportBean) dashboardReport ).getPmdReport() != null )
        {
            if ( this.pmdReport != null )
            {
                this.pmdReport.merge( ( (DashBoardReportBean) dashboardReport ).getPmdReport() );
            }
            else
            {
                this.pmdReport = (PmdReportBean) ( (DashBoardReportBean) dashboardReport ).getPmdReport().clone();
                this.pmdReport.setArtefactId( this.getArtefactId() );
                this.pmdReport.setProjectName( this.getProjectName() );
                this.pmdReport.setDateGeneration( this.getDateGeneration() );
            }
        }
        if ( ( (DashBoardReportBean) dashboardReport ).getSurefireReport() != null )
        {
            if ( this.surefireReport != null )
            {
                this.surefireReport.merge( ( (DashBoardReportBean) dashboardReport ).getSurefireReport() );
            }
            else
            {
                this.surefireReport =
                    (SurefireReportBean) ( (DashBoardReportBean) dashboardReport ).getSurefireReport().clone();
                this.surefireReport.setArtefactId( this.getArtefactId() );
                this.surefireReport.setProjectName( this.getProjectName() );
                this.surefireReport.setDateGeneration( this.getDateGeneration() );
            }
        }
        if ( ( (DashBoardReportBean) dashboardReport ).getJDependReport() != null )
        {
            if ( this.jDependReport != null )
            {
                this.jDependReport.merge( ( (DashBoardReportBean) dashboardReport ).getJDependReport() );
            }
            else
            {
                this.jDependReport =
                    (JDependReportBean) ( (DashBoardReportBean) dashboardReport ).getJDependReport().clone();
                this.jDependReport.setArtefactId( this.getArtefactId() );
                this.jDependReport.setProjectName( this.getProjectName() );
                this.jDependReport.setDateGeneration( this.getDateGeneration() );
            }
        }
    }
    /**
     * 
     */
    public void setProjectName( String projectName )
    {
        super.setProjectName( projectName );
        if ( this.checkStyleReport != null )
        {
            this.checkStyleReport.setProjectName( projectName );
        }
        if ( this.coberturaReport != null )
        {
            this.coberturaReport.setProjectName( projectName );
        }
        if ( this.cpdReport != null )
        {
            this.cpdReport.setProjectName( projectName );
        }
        if ( this.pmdReport != null )
        {
            this.pmdReport.setProjectName( projectName );
        }
        if ( this.surefireReport != null )
        {
            this.surefireReport.setProjectName( projectName );
        }
        if ( this.jDependReport != null )
        {
            this.jDependReport.setProjectName( projectName );
        }

    }
    /**
     * 
     */
    protected Object clone()
    {
        DashBoardReportBean clone = null;
        clone = (DashBoardReportBean) super.clone();
        if ( this.getCheckStyleReport() != null )
        {
            clone.setCheckStyleReport( (CheckstyleReportBean) this.getCheckStyleReport().clone() );
        }
        else
        {
            clone.setCheckStyleReport( null );
        }
        if ( this.getCoberturaReport() != null )
        {
            clone.setCoberturaReport( (CoberturaReportBean) this.getCoberturaReport().clone() );
        }
        else
        {
            clone.setCoberturaReport( null );
        }
        if ( this.getCpdReport() != null )
        {
            clone.setCpdReport( (CpdReportBean) this.getCpdReport().clone() );
        }
        else
        {
            clone.setCpdReport( null );
        }
        if ( this.getPmdReport() != null )
        {
            clone.setPmdReport( (PmdReportBean) this.getPmdReport().clone() );
        }
        else
        {
            clone.setPmdReport( null );
        }
        if ( this.getSurefireReport() != null )
        {
            clone.setSurefireReport( (SurefireReportBean) this.getSurefireReport().clone() );
        }
        else
        {
            clone.setSurefireReport( null );
        }
        if ( this.getJDependReport() != null )
        {
            clone.setJDependReport( (JDependReportBean) this.getJDependReport().clone() );
        }
        else
        {
            clone.setJDependReport( null );
        }
        return clone;
    }

}
