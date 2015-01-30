package com.linalis.pdi.steps.importioextractor;

import java.util.HashMap;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;

import com.importio.api.clientlite.data.Query;
import com.linalis.pdi.steps.AbstractImportIOStep;

public class ImportIOExtractorStep extends AbstractImportIOStep {
	
	protected static final String WEBPAGE_URL_PARAMETER = "webpage/url";
	
	// TODO faire synchro avec objet ci-dessous 
	//static protected HashMap<String, Long> requestsTimeStamps = new HashMap<String, Long>();

	/**
	 * The constructor should simply pass on its arguments to the parent class.
	 * 
	 * @param s 				step description
	 * @param stepDataInterface	step data class
	 * @param c					step copy
	 * @param t					transformation description
	 * @param dis				transformation executing
	 */
	public ImportIOExtractorStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}
	
	/**
	 * This method initializes the necessary variables to process the rows.
	 * It is called only once when the step is processing the first row.
	 * 
	 * In this case, we compute the index of the webpage url field in the input row, and then store it in the step data
	 */
    protected void processRowInitialization() throws KettleException {
    
    	ImportIOExtractorStepMeta myMeta = (ImportIOExtractorStepMeta)meta;
    	ImportIOExtractorStepData myData = (ImportIOExtractorStepData)data;
    	
		if(myMeta.isWebpageURLDefinedInAField())
		{
			if(data.inputRowMeta==null)
				throw new KettleException( BaseMessages.getString(
			              PKG, "ImportIO.Exception.NoInputRow"));
    	
		    // cache the position of the webpage/url field in the input row
		    if ( myData.indexOfWebpageUrlField < 0 )
		    {
		    	myData.indexOfWebpageUrlField = data.inputRowMeta.indexOfValue( myMeta.getWebpageURLFieldName() );
		    	if ( myData.indexOfWebpageUrlField < 0 ) {
		    	// The field is unreachable !
		    		logError( BaseMessages.getString( PKG, "ImportIO.Log.ErrorFindingField", myMeta
		    				.getWebpageURLFieldName()) );
		    		throw new KettleException( BaseMessages.getString(
		    				PKG, "ImportIO.Log.ErrorFindingField", myMeta.getWebpageURLFieldName() ) );
		    	}
		    }
		}
    }
    
    /**
     * This method sets the input parameters for the import IO query.
     * 
     * In this case, we extract the webpage url (either from the input row or from the meta), and set it as a parameter in the import.io 
     * 
     * @param query the query for which the inputs are to be set
     * @param readRow the current input row being processed
     */
    protected void setSpecificImportIOParameters(Query query, Object[] readRow) throws KettleException{
    	HashMap<String, Object> input = null;
    	
    	ImportIOExtractorStepMeta myMeta = (ImportIOExtractorStepMeta)meta;
    	ImportIOExtractorStepData myData = (ImportIOExtractorStepData)data;
    	
		String webpageUrl = myMeta.getWebpageURL();
		
		if(myMeta.isWebpageURLDefinedInAField())
		// We have to get the guid from the input row
		{
			if ( myData.indexOfWebpageUrlField >= 0 )
				webpageUrl = getInputRowMeta().getString( readRow, myData.indexOfWebpageUrlField );
		}
		
    	if(webpageUrl!=null && !webpageUrl.equals(""))
    	{
    		input = new HashMap<String, Object>();
    		input.put(WEBPAGE_URL_PARAMETER, webpageUrl);
    		query.setInput(input);
    	}
    }
	
}
