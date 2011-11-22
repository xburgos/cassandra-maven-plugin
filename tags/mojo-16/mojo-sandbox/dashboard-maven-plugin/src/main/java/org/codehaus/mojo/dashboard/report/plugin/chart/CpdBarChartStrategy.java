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
import java.util.Map;
import java.util.ResourceBundle;

import org.codehaus.mojo.dashboard.report.plugin.beans.CpdReportBean;
import org.jfree.data.category.DefaultCategoryDataset;


/**
 * 
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 *
 */
public class CpdBarChartStrategy extends AbstractCategoryChartStrategy
{
    private ResourceBundle bundle;

    private Map datas;
    
    public CpdBarChartStrategy( ResourceBundle bundle, String title, Map datas )
    {
        this.setTitle( title );
        this.bundle = bundle;
        this.datas = datas;
    }
    /**
     * 
     */
    public void fillDataset()
    {
        
        if( datas != null && !datas.isEmpty())
        {
            Iterator iter = datas.keySet().iterator();
            
            while(iter.hasNext()){
                String key = (String)iter.next();
                CpdReportBean cpdReportBean = (CpdReportBean)datas.get( key );
                //String project = this.getTitle();
                String classes = this.bundle.getString( "report.cpd.label.nbclasses" );
                String duplicate = this.bundle.getString( "report.cpd.label.nbduplicate" );

                ( (DefaultCategoryDataset) defaultdataset ).addValue( cpdReportBean.getNbClasses(), classes, key );
                ( (DefaultCategoryDataset) defaultdataset ).addValue( cpdReportBean.getNbDuplicate(), duplicate, key );
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
