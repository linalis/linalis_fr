package com.linalis.pdi.steps;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.importio.api.clientlite.ImportIO;
import com.importio.api.clientlite.data.Query;
import com.linalis.importio.MyMessageCallback;

/**
 * This class is the abstract import.io processing step.
 * 
 * It takes a row of data from the input (if any) and appends to it the data obtained from imoort.io,
 * through the use of the processRow method.
 * 
 * 2 methods have to be overridden in the child classes : 
 * - processRowInitialization() which is used to initialize variables before processing the data
 * - setSpecificImportIOParameters() which is used to set input parameters which are specific to the child class
 * 
 * @author nhaquet
 *
 */
public abstract class AbstractImportIOStep extends BaseStep {

	/**
	 *	The PKG member is used when looking up internationalized strings.
	 *	The properties file with localized keys is expected to reside in 
	 *	{the package of the class specified}/messages/messages_{locale}.properties   
	 */
	protected static Class<?> PKG = AbstractImportIOStep.class; 
	
	/**
	 * The meta for the step.
	 * Contains all of the variables that can be set in the step dialog
	 */
	protected AbstractImportIOStepMeta meta;
	
	/**
	 * The data for the step
	 * Contains all of the runtime necessary data
	 */
	protected AbstractImportIOStepData data;
	
	
	/**
	 * The constructor should simply pass on its arguments to the parent class.
	 * 
	 * @param s 				step description
	 * @param stepDataInterface	step data class
	 * @param c					step copy
	 * @param t					transformation description
	 * @param dis				transformation executing
	 */
	public AbstractImportIOStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}
	
	/**
	 * This method is called by PDI during transformation startup. 
	 * 
	 * It should initialize required for step execution. 
	 * 
	 * The meta and data implementations passed in can safely be cast
	 * to the step's respective implementations. 
	 * 
	 * It is mandatory that super.init() is called to ensure correct behavior.
	 * 
	 * Typical tasks executed here are establishing the connection to a database,
	 * as wall as obtaining resources, like file handles.
	 * 
	 * @param smi 	step meta interface implementation, containing the step settings
	 * @param sdi	step data interface implementation, used to store runtime information
	 * 
	 * @return true if initialization completed successfully, false if there was an error preventing the step from working. 
	 *  
	 */
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		// Casting to step-specific implementation classes is safe
		meta = (AbstractImportIOStepMeta) smi;
		data = (AbstractImportIOStepData) sdi;
		
		if(super.init(meta, data))
			return true;
		else
			return false;
	}	

	/**
	 * Once the transformation starts executing, the processRow() method is called repeatedly
	 * by PDI for as long as it returns true. To indicate that a step has finished processing rows
	 * this method must call setOutputDone() and return false;
	 * 
	 * Steps which process incoming rows typically call getRow() to read a single row from the
	 * input stream, change or add row content, call putRow() to pass the changed row on 
	 * and return true. If getRow() returns null, no more rows are expected to come in, 
	 * and the processRow() implementation calls setOutputDone() and returns false to
	 * indicate that it is done too.
	 * 
	 * Steps which generate rows typically construct a new row Object[] using a call to
	 * RowDataUtil.allocateRowData(numberOfFields), add row content, and call putRow() to
	 * pass the new row on. Above process may happen in a loop to generate multiple rows,
	 * at the end of which processRow() would call setOutputDone() and return false;
	 * 
	 * In this case, an import.io connection is opened, and a query is sent. IF the request fails,
	 * the request is resent as many times as defined in the step meta.
	 * 
	 * This step is a generator step as well as an appender step : it accepts  input data rows -as
	 * well as no input data row .
	 * 
	 * @param smi the step meta interface containing the step settings
	 * @param sdi the step data interface that should be used to store
	 * 
	 * @return true to indicate that the function should be called again, false if the step is done
	 */
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		// safely cast the step settings (meta) and runtime info (data) to specific implementations 
		meta = (AbstractImportIOStepMeta) smi;
		data = (AbstractImportIOStepData) sdi;
		
		

		// get incoming row, getRow() potentially blocks waiting for more rows, returns null if no more rows expected
		Object[] readRow = getRow();

		// If this is the first row and no input is received, it means the step is only generating rows, not adding to it.
		if (readRow == null){
			if(first)
				data.noInputRow = true;
			else
			{
				setOutputDone();
				return false;
			}
		}

		// the "first" flag is inherited from the base step implementation
		// it is used to guard some processing tasks, like figuring out field indexes
		// in the row structure that only need to be done once
		if (first) {
			first = false;
			// No input row
			data.inputRowMeta = getInputRowMeta();
			if(data.noInputRow)
			{
				data.outputRowMeta = new RowMeta();
				readRow = new Object[0];
			}
			// We have an input row
			else
			{
			// clone the input row structure and place it in our data object
				data.outputRowMeta = (RowMetaInterface) data.inputRowMeta.clone();
			}
			
			// use meta.getFields() to change it, so it reflects the output row structure 
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			if(meta.isGUIDDefinedInAField())
			{
				if(data.inputRowMeta==null)
					throw new KettleException( BaseMessages.getString(
				              PKG, "ImportIO.Exception.NoInputRow"));
				
		        // cache the position of the guid field in the input row
		        if ( data.indexOfGUIDField < 0 ) {
		          data.indexOfGUIDField = data.inputRowMeta.indexOfValue( meta.getGuidFieldName() );
		          if ( data.indexOfGUIDField < 0 ) {
		            // The field is unreachable !
		            logError( BaseMessages.getString( PKG, "ImportIO.Log.ErrorFindingField", meta
		              .getGuidFieldName() ) );
		            throw new KettleException( BaseMessages.getString(
		              PKG, "ImportIO.Log.ErrorFindingField", meta.getGuidFieldName() ) );
		          }
		        }
			}
	        
	        // Specific initialization goes there by inheritance
	        processRowInitialization();
		}
		
		String guidToUse = meta.getGUID();
		
		if(meta.isGUIDDefinedInAField())
		// We have to get the guid from the input row
		{
			if ( data.indexOfGUIDField >= 0 )
				guidToUse = getInputRowMeta().getString( readRow, data.indexOfGUIDField );
		}
		
		// Sending request to import IO, and blocks until results are received
		MyMessageCallback message = importIOQuery(guidToUse, readRow);
		
		// Trying again if the import IO request did not work the first time.
		// We try meta.getNbRetries() times
		int numberOfRetries = 0;
		int maxRetries = Integer.parseInt(meta.getNbRetries());
		while(message!=null && (message.hasTimedOut()||message.isDisconnected()) && numberOfRetries<maxRetries)
		{
			// Logging
			if(message.hasTimedOut())
			{
				logError(BaseMessages.getString(PKG, "ImportIO.TimedOut.KO"));
			}
			if(message.isDisconnected())
				logError(BaseMessages.getString(PKG, "ImportIO.Disconnected.KO"));
			
			// Sending request to import IO, and blocks until results are received
			message = importIOQuery(guidToUse, readRow);
			
		}
		
		// If no result from mport io and  an input row, we output the input row with the output fields ste to null
		if((message!=null && (message.getDataRows()==null || message.getDataRows().size()==0)) && !data.noInputRow)
		{
			Object[] outputRow = populateOutputFields(readRow, null);
			putRow(data.outputRowMeta, outputRow); 
		}
		
		if(message!=null)
			// generating rows for each line returned from import io
			for(Object o :message.getDataRows())
			{
				// populate the output rowwith the data from import io
				Object[] outputRow = populateOutputFields(readRow, (HashMap<String, Object>)o);
				// put the row to the output row stream
				putRow(data.outputRowMeta, outputRow); 
			}
		
		message = null;
		
		// log progress if it is time to to so
		if (checkFeedback(getLinesRead())) {
			logBasic("Linenr " + getLinesRead()); // Some basic logging
		}

		if(data.noInputRow)
		{
			setOutputDone();
			return false;
		}
		else
		// indicate that processRow() should be called again
			return true;
	}

	/**
	 * This method is called by PDI once the step is done processing. 
	 * 
	 * The dispose() method is the counterpart to init() and should release any resources
	 * acquired for step execution like file handles or database connections.
	 * 
	 * The meta and data implementations passed in can safely be cast
	 * to the step's respective implementations. 
	 * 
	 * It is mandatory that super.dispose() is called to ensure correct behavior.
	 * 
	 * @param smi 	step meta interface implementation, containing the step settings
	 * @param sdi	step data interface implementation, used to store runtime information
	 */
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {

		// Casting to step-specific implementation classes is safe
		meta = (AbstractImportIOStepMeta) smi;
		data = (AbstractImportIOStepData) sdi;
		
		super.dispose(meta, data);
	}

	/**
	 * This method appends the data from import.io with the input row (if any)
	 * 
	 * @param inputRow the input row 
	 * @param importIOData the import.io data
	 * @return the inputrow with the import.io data appended to it.
	 */
	public Object[] populateOutputFields(Object[] inputRow, HashMap<String, Object> importIOData)
	{
		// resizing the row to include the input row and the ouput fields
		Object[] outputRow = RowDataUtil.resizeArray(inputRow, data.outputRowMeta.size());
		
		int inputSize = 0;
		
		if(getInputRowMeta()!=null)
			inputSize=getInputRowMeta().size();
		
		
		for (int i = 0, outi=inputSize;i<meta.getOutputNames().length&&importIOData!=null;outi++, i++){
	         
	        // try to get the value from the cached import io result
	        String value = (String) importIOData.get(meta.getOutputFieldImportIONames()[i]);
	 
	        // if nothing is there, return the default
	        if (value == null){
	            outputRow[outi] = "";  
	        }
	        // else convert the value to desired format
	        else{
	            outputRow[outi] = value;
	        }
	    }
		return outputRow;
	}
	   
	/**
	 * Abstract method. Must be overriden by the child class.
	 * This is the method where all of the initialization has to be done before the row processing starts.
	 * 
	 * @throws KettleException
	 */
	abstract protected void processRowInitialization() throws KettleException;
	
    /**
     * This method sets the input parameters for the import IO query.
     * 
     * This is an abstract method, it must be overridden in the child class.
     * 
     * @param query the query for which the inputs are to be set
     * @param readRow the current input row being processed
     */
	abstract protected void setSpecificImportIOParameters(Query query, Object[] readRow) throws KettleException;
	
	/**
	 * Disconnects from import.io
	 * @param client the client used to disconnect
	 */
	public void disconnect(ImportIO client)
	{
		try {
			client.disconnect();
		} catch (IOException e) {
			logError(BaseMessages.getString(PKG, "ImportIO.Disconnect.KO"));
		}
	}
	
	/**
	 * This is the method which makes the request to import.io, and block until the data has been received.
	 * @param aGUID the import.io GUID being called
	 * @param readRow the input row for the current step. This is used to extract some input parameters (if any) for the import.io request
	 * @return the MyMessageCallBack containing the data from import.io
	 * @throws KettleException
	 */
	public MyMessageCallback importIOQuery(String aGUID, Object[] readRow) throws KettleException
	{
		// Preparing the import io connection
		ImportIO client = new ImportIO(UUID.fromString(environmentSubstitute(meta.getUserID())), environmentSubstitute(meta.getApiKey()));
		try {
			client.connect();
		} catch (IOException e) {
			logError(BaseMessages.getString(PKG, "ImportIO.Connection.KO"));
			return null;
		}
		
		CountDownLatch latch = new CountDownLatch(1);

		MyMessageCallback message = new MyMessageCallback(latch);
		message.setLog(log);

		// Generate a list of the connector GUIDs we are going to query
		List<UUID> connectorGuids = Arrays.asList(
		  UUID.fromString(aGUID)
		);
		
		Query query = new Query();
		query.setConnectorGuids(connectorGuids);
		
		setSpecificImportIOParameters(query, readRow);

		try {
			client.query(query, message);
		} catch (IOException e) {
			logError(BaseMessages.getString(PKG, "ImportIO.Query.KO"));
		}

		boolean hasTimedOut = false;
		
		// Wait on the query to be completed
		int timeOut;
		
		try
		{
			timeOut = Integer.parseInt(meta.getTimeOut());
		}
		catch(NumberFormatException e)
		{
			timeOut = Integer.parseInt(AbstractImportIOStepData.DEFAULT_TIME_OUT);
		}
		
		try {
			hasTimedOut = !latch.await(timeOut, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logError("This should never happen");
		}
		
		message.setHasTimedOut(hasTimedOut);
		
		disconnect(client);
		
		return message;
	}
}