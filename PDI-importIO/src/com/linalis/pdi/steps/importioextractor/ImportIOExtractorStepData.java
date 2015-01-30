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

package com.linalis.pdi.steps.importioextractor;

import com.linalis.pdi.steps.AbstractImportIOStepData;

/**
 * This class is the representation of an import.io step data.
 * 
 * Runtime variables must be found here
 * 
 * In the case of an extractor, the only runtime data needed is the index of the webpage/url field in the input row
 *   
 */
public class ImportIOExtractorStepData extends AbstractImportIOStepData {
    // Indexes to look up fields in input row
    public int indexOfWebpageUrlField = -1;

	
    public ImportIOExtractorStepData()
	{
		super();
	}
}
	
