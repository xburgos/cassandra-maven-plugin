package org.codehaus.mojo.dashboard.report.plugin.chart;

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


import java.util.Iterator;
import java.util.ResourceBundle;

import org.codehaus.mojo.dashboard.report.plugin.beans.CpdReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.DashBoardMultiReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.DashBoardReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.IDashBoardReportBean;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;


/**
 * 
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 *
 */
public class CpdBarChartStrategy extends AbstractCategoryDatasetStrategy
{

    public CpdBarChartStrategy( ResourceBundle bundle )
    {
        super( bundle );
    }
    /**
     * 
     */
    public void createDatasetElement( Dataset dataset, IDashBoardReportBean dashboardReport )
    {
        if ( dashboardReport instanceof DashBoardReportBean )
        {
            if ( ( (DashBoardReportBean) dashboardReport ).getCpdReport() != null )
            {
                CpdReportBean cpdReportBean = ( (DashBoardReportBean) dashboardReport ).getCpdReport();
                String project = cpdReportBean.getProjectName();
                String classes = this.getBundle().getString( "report.cpd.label.nbclasses" );
                String duplicate = this.getBundle().getString( "report.cpd.label.nbduplicate" );

                ( (DefaultCategoryDataset) dataset ).addValue( cpdReportBean.getNbClasses(), classes, project );
                ( (DefaultCategoryDataset) dataset ).addValue( cpdReportBean.getNbDuplicate(), duplicate, project );
            }
        }
        else
        {
            Iterator iter = ( (DashBoardMultiReportBean) dashboardReport ).getModules().iterator();
            while ( iter.hasNext() )
            {
                IDashBoardReportBean reportBean = (IDashBoardReportBean) iter.next();
                createDatasetElement( dataset, reportBean );
            }
        }

    }
    /**
     * 
     */
    public String getXAxisLabel()
    {
        return "value";
    }
}
