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


import java.text.NumberFormat;
import java.util.Iterator;
import java.util.ResourceBundle;

import org.codehaus.mojo.dashboard.report.plugin.beans.CoberturaReportBean;
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
public class CoberturaBarChartStrategy extends AbstractCategoryDatasetStrategy
{
    /**
     * 
     */
    private static final int PERCENT_VALUE = 100;
    /**
     * 
     * @param bundle
     */
    public CoberturaBarChartStrategy( ResourceBundle bundle )
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
            if ( ( (DashBoardReportBean) dashboardReport ).getCoberturaReport() != null )
            {
                CoberturaReportBean coberReportBean = ( (DashBoardReportBean) dashboardReport ).getCoberturaReport();
                String project = coberReportBean.getProjectName();
                String linecover = this.getBundle().getString( "report.cobertura.label.linecover" );
                String branchcover = this.getBundle().getString( "report.cobertura.label.branchcover" );
                //* CoberturaBarChartStrategy.PERCENT_VALUE
                ( (DefaultCategoryDataset) dataset ).addValue( coberReportBean.getLineCoverRate()
                                , linecover, project );
                ( (DefaultCategoryDataset) dataset ).addValue( coberReportBean.getBranchCoverRate()
                                , branchcover, project );
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
        return "Coverage (%)";
    }
}
