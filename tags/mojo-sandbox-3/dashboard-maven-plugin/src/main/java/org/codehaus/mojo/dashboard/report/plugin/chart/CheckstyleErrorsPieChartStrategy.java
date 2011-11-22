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

import org.codehaus.mojo.dashboard.report.plugin.beans.CheckstyleError;
import org.codehaus.mojo.dashboard.report.plugin.beans.CheckstyleReportBean;
import org.codehaus.mojo.dashboard.report.plugin.beans.IDashBoardReportBean;
import org.jfree.chart.ChartColor;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;

/**
 * Checkstyle error dataset strategy class.
 * @author <a href="srivollet@objectif-informatique.fr">Sylvain Rivollet</a>
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 */
public class CheckstyleErrorsPieChartStrategy extends AbstractPieDatasetStrategy
{
    /**
     * 
     * @param bundle
     */
    public CheckstyleErrorsPieChartStrategy( ResourceBundle bundle )
    {
        super( bundle );
    }

    /**
     * 
     */
    public void createDatasetElement( Dataset dataset, IDashBoardReportBean dashboardReport )
    {
        if ( dashboardReport instanceof CheckstyleReportBean )
        {
            float percentVal = 0;
            int nbItInfPercent = 0;
            CheckstyleReportBean checkstyleReportBean = (CheckstyleReportBean) dashboardReport;
            CheckstyleError error = new CheckstyleError();
            Iterator iterator = checkstyleReportBean.getErrors().iterator();

            while ( iterator.hasNext() )
            {
                error = (CheckstyleError) iterator.next();
                percentVal = percent( error.getNbIteration(),checkstyleReportBean.getNbTotal() );

                if ( percentVal > 1 )
                {
                    ( (DefaultPieDataset) dataset ).setValue( error.getMessage() + "=" + percentVal,
                                                              error.getNbIteration() );
                }
                else
                {
                    nbItInfPercent += error.getNbIteration();
                }
            }

            if ( nbItInfPercent > 0 )
            {
                percentVal = percent( nbItInfPercent,checkstyleReportBean.getNbTotal() );
                ( (DefaultPieDataset) dataset ).setValue( "Other categories (<1%) = " + percentVal, nbItInfPercent );
            }
        }
    }

    /**
     * 
     */
    public Paint[] getPaintColor()
    {
        return new Paint[] { ChartUtils.BLUE_LIGHT, ChartColor.RED, ChartUtils.YELLOW_LIGHT };
    }

    /**
     * give the percentage of iteration compared to the total error count
     */
    private float percent( int nbIteration, int nbTotal )
    {
        float percent = nbIteration * 100f / nbTotal;
        percent *= 1000;
        percent = (int) ( percent + .5 );
        percent /= 1000;
        return percent;
    }

}
