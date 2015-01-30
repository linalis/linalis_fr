package com.linalis.pdi.steps.importioconnector;

import java.util.HashMap;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;

import com.importio.api.clientlite.data.Query;
import com.linalis.pdi.steps.AbstractImportIOStep;

/**
 * This class allows to request an import.io connector in pdi.
 * It processes the input row (if any), makes the query to import.io, and outputs the results of the connector
 * 
 * @author nhaquet
 * @see AbstractImportIOStep
 *
 */
public class ImportIOConnectorStep extends AbstractImportIOStep {

	/**
	 * Constructor for the step.
	 * The constructor simply passes on its arguments to the parent class.
	 * 
	 * @param s 				step description
	 * @param stepDataInterface	step data class
	 * @param c					step copy
	 * @param t					transformation description
	 * @param dis				transformation executing
	 */
	public ImportIOConnectorStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}
	
	/**
	 * This method initializes the necessary variables to process the rows.
	 * It is called only once when the step is processing the first row.
	 * 
	 * In this case, nothing is needed.
	 */
    protected void processRowInitialization() throws KettleException {}
    
    /**
     * This method sets the input parameters for the import IO query.
     * 
     * In this case, the parameters startPage and maxPages are set, as well as the input parameters for the query.
     * @param query the query for which the inputs are to be set
     * @param readRow the current input row being processed
     */
    protected void setSpecificImportIOParameters(Query query, Object[] readRow) throws KettleException{
    	HashMap<String, Object> input = new HashMap<String, Object>();
    	
    	ImportIOConnectorStepMeta myMeta = (ImportIOConnectorStepMeta)meta;
    	
    	// Start page parameter
		String startPage = myMeta.getStartPage();
		try{
			if(startPage!=null && !startPage.equals(""))
				query.setStartPage(Integer.parseInt(startPage));
		}catch(NumberFormatException e){}
		
		// Max Page parameter
		String maxPage = myMeta.getMaxPage();
		try{
			if(maxPage!=null && !maxPage.equals(""))
				query.setMaxPages(Integer.parseInt(maxPage));
		}catch(NumberFormatException e){}
		
		// input parameters
		for(int i=0;i<myMeta.getImportIOInputFields().length;i++)
		{
			String inputName  = myMeta.getImportIOInputFields()[i];
			String inputValue = myMeta.getImportIOInputFieldsValues()[i];
			if(inputName!=null&&!inputName.equals("")&&inputValue!=null&&!inputValue.equals(""))
				input.put(inputName, inputValue);
		}
		if(!input.isEmpty())
			query.setInput(input);
    }
	
}
