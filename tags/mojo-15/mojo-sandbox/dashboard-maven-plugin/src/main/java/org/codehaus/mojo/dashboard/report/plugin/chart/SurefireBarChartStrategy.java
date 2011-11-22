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

import java.awt.Paint;
import java.util.Iterator;
import java.util.ResourceBundle;

import org.codehaus.mojo.dashboard.report.plugin.beans.DashBoardMultiReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.DashBoardReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.IDashBoardReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.SurefireReportBean;
import org.jfree.chart.ChartColor;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;

/**
 * 
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 * 
 */
public class SurefireBarChartStrategy extends AbstractCategoryDatasetStrategy
{
    /**
     * 
     * @param bundle
     */
    public SurefireBarChartStrategy( ResourceBundle bundle )
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
            if ( ( (DashBoardReportBean) dashboardReport ).getSurefireReport() != null )
            {
                SurefireReportBean fireReportBean = ( (DashBoardReportBean) dashboardReport ).getSurefireReport();

                int total = fireReportBean.getNbTests();
                int error = fireReportBean.getNbErrors();
                int fail = fireReportBean.getNbFailures();
                int skip = fireReportBean.getNbSkipped();
                ( (DefaultCategoryDataset) dataset ).setValue( 
                                                   ( total - error - fail - skip ),
                                                   this.getBundle().getString( "report.surefire.label.success" ),
                                                   fireReportBean.getProjectName() );

                ( (DefaultCategoryDataset) dataset ).setValue(
                                                   error,
                                                   this.getBundle().getString( "report.surefire.label.errors" ),
                                                   fireReportBean.getProjectName() );

                ( (DefaultCategoryDataset) dataset ).setValue(
                                                   fail,
                                                   this.getBundle().getString( "report.surefire.label.failures" ),
                                                   fireReportBean.getProjectName() );

                ( (DefaultCategoryDataset) dataset ).setValue(
                                                   skip,
                                                   this.getBundle().getString( "report.surefire.label.skipped" ),
                                                   fireReportBean.getProjectName() );
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
    public Paint[] getPaintColor()
    {
        return new Paint[] { ChartColor.GREEN, ChartColor.RED, ChartColor.ORANGE, ChartColor.LIGHT_GRAY };
    }
}
