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

import org.codehaus.mojo.dashboard.report.plugin.beans.IDashBoardReportBean;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.PieDataset;
import org.jfree.util.Rotation;

/**
 * Pie Chart 3D renderer.
 * 
 * @author <a href="srivollet@objectif-informatique.fr">Sylvain Rivollet</a>
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 */
public class PieChart3DRenderer extends AbstractChartRenderer
{

    private static final double START_ANGLE = 45D;
    
    private static final float FOREGROUND_ALPHA = 0.5f;

    /**
     * 
     * @param dashboardReport
     * @param strategy
     */
    public PieChart3DRenderer( IDashBoardReportBean dashboardReport, IDatasetStrategy strategy )
    {
        super( dashboardReport, strategy );
    }

    /**
     * 
     * @param dashboardReport
     * @param strategy
     * @param width
     * @param height
     */
    public PieChart3DRenderer( IDashBoardReportBean dashboardReport, IDatasetStrategy strategy, int width, int height )
    {
        super( dashboardReport, strategy, width, height );
    }

    protected void createChart( IDashBoardReportBean dashboardReport )
    {
        PieDataset dataset = (PieDataset) this.datasetStrategy.createDataset( dashboardReport );
        report =
            ChartFactory.createPieChart3D( dashboardReport.getProjectName() + " "
                            + this.datasetStrategy.getBundle().getString( "chart.checkstyle.violations.title" ),
                                           dataset, false, true, true );

        PiePlot3D plot3D = (PiePlot3D) report.getPlot();
        plot3D.setDirection( Rotation.ANTICLOCKWISE );
        plot3D.setStartAngle( PieChart3DRenderer.START_ANGLE );
        plot3D.setForegroundAlpha( PieChart3DRenderer.FOREGROUND_ALPHA );

    }
}
