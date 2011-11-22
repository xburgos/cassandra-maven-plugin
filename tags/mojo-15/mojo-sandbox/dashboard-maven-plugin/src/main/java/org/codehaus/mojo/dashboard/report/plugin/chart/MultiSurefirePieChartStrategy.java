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
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;

/**
 * 
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 * 
 */
public class MultiSurefirePieChartStrategy extends AbstractPieDatasetStrategy
{
    /**
     * 
     */
    private static final int SKIP_INDEX = 3;
    /**
     * 
     */
    private static final int FAIL_INDEX = 2;
    /**
     * 
     */
    private static final int ERROR_INDEX = 1;
    /**
     * 
     */
    private static final int SUCCESS_INDEX = 0;
    /**
     * 
     */
    private int[] indicators = new int[] { 0, 0, 0, 0 };
    /**
     * 
     * @param bundle
     */
    public MultiSurefirePieChartStrategy( ResourceBundle bundle )
    {
        super( bundle );
    }
    /**
     * 
     */
    public void createDatasetElement( Dataset dataset, IDashBoardReportBean dashboardReport )
    {
        fillIndicators( dashboardReport );

        ( (DefaultPieDataset) dataset ).setValue( this.getBundle().getString( "report.surefire.label.success" ) + " = "
                        + ( indicators[MultiSurefirePieChartStrategy.SUCCESS_INDEX] ),
                                                  indicators[MultiSurefirePieChartStrategy.SUCCESS_INDEX] );

        ( (DefaultPieDataset) dataset ).setValue( this.getBundle().getString( "report.surefire.label.errors" ) + " = "
                        + indicators[MultiSurefirePieChartStrategy.ERROR_INDEX],
                                                  indicators[MultiSurefirePieChartStrategy.ERROR_INDEX] );

        ( (DefaultPieDataset) dataset ).setValue( this.getBundle().getString( "report.surefire.label.failures" ) + " = "
                        + indicators[MultiSurefirePieChartStrategy.FAIL_INDEX],
                                                  indicators[MultiSurefirePieChartStrategy.FAIL_INDEX] );

        ( (DefaultPieDataset) dataset ).setValue( this.getBundle().getString( "report.surefire.label.skipped" ) + " = "
                        + indicators[MultiSurefirePieChartStrategy.SKIP_INDEX],
                                                  indicators[MultiSurefirePieChartStrategy.SKIP_INDEX] );

    }
    /**
     * 
     * @param dashboardReport
     */
    private void fillIndicators( IDashBoardReportBean dashboardReport )
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
                int success = total - error - fail - skip;
                indicators[MultiSurefirePieChartStrategy.SUCCESS_INDEX] =
                    indicators[MultiSurefirePieChartStrategy.SUCCESS_INDEX] + success;
                indicators[MultiSurefirePieChartStrategy.ERROR_INDEX] =
                    indicators[MultiSurefirePieChartStrategy.ERROR_INDEX] + error;
                indicators[MultiSurefirePieChartStrategy.FAIL_INDEX] =
                    indicators[MultiSurefirePieChartStrategy.FAIL_INDEX] + fail;
                indicators[MultiSurefirePieChartStrategy.SKIP_INDEX] =
                    indicators[MultiSurefirePieChartStrategy.SKIP_INDEX] + skip;
            }

        }
        else
        {
            Iterator iter = ( (DashBoardMultiReportBean) dashboardReport ).getModules().iterator();
            while ( iter.hasNext() )
            {
                IDashBoardReportBean reportBean = (IDashBoardReportBean) iter.next();
                fillIndicators( reportBean );
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
