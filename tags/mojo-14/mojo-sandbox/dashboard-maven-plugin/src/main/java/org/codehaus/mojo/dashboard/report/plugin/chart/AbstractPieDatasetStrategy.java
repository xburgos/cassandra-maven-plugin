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


import java.util.ResourceBundle;

import org.codehaus.mojo.dashboard.report.plugin.beans.IDashBoardReportBean;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;

/**
 * 
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 * 
 */
public abstract class AbstractPieDatasetStrategy extends AbstractDatasetStrategy
{

    /**
     * 
     * @param bundle
     */
    public AbstractPieDatasetStrategy( ResourceBundle bundle )
    {
        super( bundle );
    }
    
    /* (non-Javadoc)
     * @see org.codehaus.mojo.dashboard.report.plugin.chart.IDatasetStrategy#createDataset(org.codehaus.mojo.dashboard.report.plugin.beans.IDashBoardReportBean)
     */
    public Dataset createDataset( IDashBoardReportBean dashboardReport )
    {
        DefaultPieDataset defaultPieDataset = new DefaultPieDataset();
        createDatasetElement( defaultPieDataset, dashboardReport );
        if ( defaultPieDataset.getItemCount() > 0 )
        {
            this.setDatasetEmpty( false );
        }
        return defaultPieDataset;
    }

}
