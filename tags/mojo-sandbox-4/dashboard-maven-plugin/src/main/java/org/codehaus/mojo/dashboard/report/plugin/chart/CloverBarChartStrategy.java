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

import java.awt.Color;
import java.awt.Paint;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

import org.codehaus.mojo.dashboard.report.plugin.beans.CloverReportBean;

/**
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 */
public class CloverBarChartStrategy extends AbstractCategoryChartStrategy
{
    private ResourceBundle bundle;

    private Map datas;

    public CloverBarChartStrategy( ResourceBundle bundle, String title, Map datas )
    {
        this.setTitle( title );
        this.bundle = bundle;
        this.datas = datas;
    }

    public void fillDataset()
    {
        
        if( datas != null && !datas.isEmpty())
        {
            Iterator iter = datas.keySet().iterator();
            String coveredLabel = this.bundle.getString( "report.clover.label.covered" );
            String uncoveredLabel = this.bundle.getString( "report.clover.label.covered" );
            while(iter.hasNext()){
                String key = (String)iter.next();
                CloverReportBean cloverReportBean = (CloverReportBean)datas.get( key );
                
                int total = cloverReportBean.getElements();
                int covered = cloverReportBean.getCoveredElements();
                int uncovered = total - covered;
                this.defaultdataset.setValue( ( covered / (double) total ) * 100, coveredLabel,
                                              this.bundle.getString( "report.clover.label.total" ) );

                this.defaultdataset.setValue( ( uncovered / (double) total ) * 100, uncoveredLabel,
                                              this.bundle.getString( "report.clover.label.total" ) );
                int totalCond = cloverReportBean.getConditionals();
                int coveredCond = cloverReportBean.getCoveredConditionals();
                int uncoveredCond = totalCond - coveredCond;
                this.defaultdataset.setValue( ( coveredCond / (double) totalCond ) * 100, coveredLabel,
                                              this.bundle.getString( "report.clover.label.conditionals" ) );

                this.defaultdataset.setValue( ( uncoveredCond / (double) totalCond ) * 100, uncoveredLabel,
                                              this.bundle.getString( "report.clover.label.conditionals" ) );
                int totalStat = cloverReportBean.getStatements();
                int coveredStat = cloverReportBean.getCoveredStatements();
                int uncoveredStat = totalStat - coveredStat;
                this.defaultdataset.setValue( ( coveredStat / (double) totalStat ) * 100, coveredLabel,
                                              this.bundle.getString( "report.clover.label.statements" ) );

                this.defaultdataset.setValue( ( uncoveredStat / (double) totalStat ) * 100, uncoveredLabel,
                                              this.bundle.getString( "report.clover.label.statements" ) );
                int totalMeth = cloverReportBean.getMethods();
                int coveredMeth = cloverReportBean.getCoveredMethods();
                int uncoveredMeth = totalMeth - coveredMeth;
                this.defaultdataset.setValue( ( coveredMeth / (double) totalMeth ) * 100, coveredLabel,
                                              this.bundle.getString( "report.clover.label.methods" ) );

                this.defaultdataset.setValue( ( uncoveredMeth / (double) totalMeth ) * 100, uncoveredLabel,
                                              this.bundle.getString( "report.clover.label.methods" ) );
            }
        }
    }

    public Paint[] getPaintColor()
    {
        return new Paint[] { Color.GREEN, Color.RED };
    }
}
