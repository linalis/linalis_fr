/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.linalis.pdi.steps;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * This class is the asbstract representation of an import.io step data.
 * 
 * Runtime variables must be found here :
 * - index of the field guid in the input row
 * - a flag to specify the type of step (generating or appender)
 *   
 * The input/output row structure is also stored in this class
 */
abstract public class AbstractImportIOStepData extends BaseStepData implements StepDataInterface {

	/**
	 * The RowMetaInterface for the output row
	 */
	public RowMetaInterface outputRowMeta;
	
	/**
	 * The RowMetaInterface for the input row
	 */
	public RowMetaInterface inputRowMeta;
	
    /**
     * flag set to true if the step is a generating step 
     */
    protected boolean noInputRow;
    
    /**
     * Index for the field GUID in the input row
     */
    public int indexOfGUIDField = -1;
    
    /**
     * Default time out for the import.io request
     */
	public final static String DEFAULT_TIME_OUT = "20";
	
	/**
	 * Default number of times we retry to request import.io if the first request fails
	 */
	public final static String DEFAULT_RETRIES = "1";
	
	/**
	 * Default constructor
	 */
    public AbstractImportIOStepData()
	{
		super();
	}
}
	
