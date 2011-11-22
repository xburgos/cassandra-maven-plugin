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
import java.util.ResourceBundle;

import org.jfree.chart.ChartColor;

/**
 * @author <a href="dvicente72@gmail.com">David Vicente</a>
 *
 */
public abstract class AbstractDatasetStrategy implements IDatasetStrategy
{
    /**
     * 
     */
    private ResourceBundle bundle = null;
    /**
     * 
     */
    private boolean isDatasetEmpty = true;
    
    /**
     * 
     * @param bundle
     */
    public AbstractDatasetStrategy( ResourceBundle bundle )
    {
        this.bundle = bundle;
    }
    /**
     * 
     */
    public String getXAxisLabel()
    {
        return "";
    }
    /**
     * 
     */
    public String getYAxisLabel()
    {
        return "";
    }
    /**
     * 
     */
    public Paint[] getPaintColor()
    {
        return ChartColor.createDefaultPaintArray();
    }
    /**
     * 
     */
    public boolean isDatasetEmpty()
    {
        return this.isDatasetEmpty;
    }
    /**
     * 
     */
    public void setDatasetEmpty( boolean isDatasetEmpty )
    {
        this.isDatasetEmpty = isDatasetEmpty;
    }
    /**
     * 
     */
    public ResourceBundle getBundle()
    {
        return this.bundle;
    }
}
