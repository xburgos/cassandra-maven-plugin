package org.codehaus.mojo.dashboard.report.plugin.chart.time;

/*
 * Copyright 2007 David Vicente
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
import java.util.List;
import java.util.ResourceBundle;

import org.codehaus.mojo.dashboard.report.plugin.chart.AbstractChartStrategy;
import org.codehaus.mojo.dashboard.report.plugin.utils.TimePeriod;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.data.general.Dataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Week;

public abstract class AbstractTimeChartStrategy extends AbstractChartStrategy
{

    public static String xAxisLabel = "Date";
    
    public static String yAxisLabel = "Values";
    
    protected TimeSeriesCollection defaultdataset = new TimeSeriesCollection();
    
    protected ResourceBundle bundle;
    
    protected List mResults;
    
    protected TimePeriod timePeriod;
    
    protected Class periodClass;
    
    private Date startDate;
    
    private Date endDate;
    
    public AbstractTimeChartStrategy(ResourceBundle bundle, String title,List results, String timeUnit, Date startDate, Date endDate )
    {
        this.setTitle( title );
        this.bundle = bundle;
        this.mResults = results;
        this.startDate = startDate;
        this.endDate = endDate;
        retreivePeriodClass(timeUnit);
        
    }
    
    /* (non-Javadoc)
     * @see org.codehaus.mojo.dashboard.report.plugin.chart.IChartStrategy#getDataset()
     */
    public Dataset getDataset()
    {
        fillDataset();
        if ( defaultdataset.getSeriesCount() > 0 )
        {
            this.setDatasetEmpty( false );
        }
        return defaultdataset;
    }

    /*public Paint[] getPaintColor()
    {
        return super.getPaintColor();
    }*/

    public String getXAxisLabel()
    {
        return AbstractTimeChartStrategy.xAxisLabel;
    }
    
    public String getYAxisLabel()
    {
        return AbstractTimeChartStrategy.yAxisLabel;
    }
    
    private void retreivePeriodClass(String timeUnit)
    {
        this.timePeriod = TimePeriod.getPeriod(timeUnit);
        
        periodClass = null;
        if (timePeriod.equals(TimePeriod.MINUTE)) {
            periodClass = Minute.class;
        } else if (timePeriod.equals(TimePeriod.HOUR)) {
            periodClass = Hour.class;
        } else if (timePeriod.equals(TimePeriod.DAY)) {
            periodClass = Day.class;
        } else if (timePeriod.equals(TimePeriod.WEEK)) {
            periodClass = Week.class;
        } else if (timePeriod.equals(TimePeriod.MONTH)) {
            periodClass = Month.class;
        } else {
            periodClass = Day.class;
        }
    }
    
    protected RegularTimePeriod getChartDate(Date keyDate)
    {
        RegularTimePeriod chartDate = null;
        if (timePeriod.equals(TimePeriod.MINUTE)) {
            chartDate = new Minute(keyDate);
        } else if (timePeriod.equals(TimePeriod.HOUR)) {
            chartDate = new Hour(keyDate);
        } else if (timePeriod.equals(TimePeriod.DAY)) {
            chartDate = new Day(keyDate);
        } else if (timePeriod.equals(TimePeriod.WEEK)) {
            chartDate = new Week(keyDate);
        } else if (timePeriod.equals(TimePeriod.MONTH)) {
            chartDate = new Month(keyDate);
        } else {
            chartDate = new Hour(keyDate);
        }
        return chartDate;
    }

    public TimePeriod getTimePeriod()
    {
        return timePeriod;
    }
    
    public NumberAxis getRangeAxis(){
        NumberAxis valueaxis = new NumberAxis();
        valueaxis.setLowerMargin(0.0D);
        valueaxis.setUpperMargin(0.25D);
        valueaxis.setLabel( this.getYAxisLabel() );
       return valueaxis;
    }
    
    public Date getEndDate()
    {
        return endDate;
    }

    public Date getStartDate()
    {
        return startDate;
    }
    
    public XYItemLabelGenerator getLabelGenerator()
    {
        return new StandardXYItemLabelGenerator();
    }
}
