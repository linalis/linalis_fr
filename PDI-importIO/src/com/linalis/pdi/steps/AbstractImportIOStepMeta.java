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

import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/**
 * This class is the abstract representation of an import.io step meta.
 * All of the common settings for the step (usually defined in the dialog) are found here :
 *  - api key
 *  - user id
 *  - guid 
 *  - time out
 *  - output fields
 *  - etc...
 *   
 * This class is the implementation of StepMetaInterface.
 * Classes implementing this interface need to:
 * 
 * - keep track of the step settings
 * - serialize step settings both to xml and a repository
 * - provide new instances of objects implementing StepDialogInterface, StepInterface and StepDataInterface
 * - report on how the step modifies the meta-data of the row-stream (row structure and field types)
 * - perform a sanity-check on the settings provided by the user 
 * 
 */
public abstract class AbstractImportIOStepMeta extends BaseStepMeta implements StepMetaInterface {

	/**
	 *	The PKG member is used when looking up internationalized strings.
	 *	The properties file with localized keys is expected to reside in 
	 *	{the package of the class specified}/messages/messages_{locale}.properties   
	 */
	protected static Class<?> PKG = AbstractImportIOStepMeta.class; // for i18n purposes
	
	/**
	 * Import IO user id
	 */
	protected String userID;

	/**
	 * Import IO api key
	 */
	protected String apiKey;
	
	/**
	 * Import IO GUID
	 */
	protected String guid;
	
	/**
	 * GUID is defined in a field?
	 */
	protected boolean guidDefinedInAField;
	
	/**
	 * Field name for guid
	 */
	protected String guidFieldName;
	
	/**
	 * Import IO time out
	 */
	protected String timeOut;

	/**
	 * Number of retries
	 */
	protected String nbRetries;
	
	/**
	 * Minimum time between 2 queries in milliseconds
	 */
	protected String minimumTime;
	
	/** output: Name of the output fields */
	protected String[] outputNames;
	
	/** output: Name of the import io field */
	protected String[] outputFieldImportIONames;
	
	/**
	 * Constructor should call super() to make sure the base class has a chance to initialize properly.
	 */
	public AbstractImportIOStepMeta() {
		super(); 
	}
	
	/**
	 * Called by Spoon to get a new instance of the SWT dialog for the step.
	 * A standard implementation passing the arguments to the constructor of the step dialog is recommended.
	 * 
	 * @param shell		an SWT Shell
	 * @param meta 		description of the step 
	 * @param transMeta	description of the the transformation 
	 * @param name		the name of the step
	 * @return 			new instance of a dialog for this step 
	 */
	public abstract StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name);

	/**
	 * Called by PDI to get a new instance of the step implementation. 
	 * A standard implementation passing the arguments to the constructor of the step class is recommended.
	 * 
	 * @param stepMeta				description of the step
	 * @param stepDataInterface		instance of a step data class
	 * @param cnr					copy number
	 * @param transMeta				description of the transformation
	 * @param disp					runtime implementation of the transformation
	 * @return						the new instance of a step implementation 
	 */
	abstract public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp);

	/**
	 * Called by PDI to get a new instance of the step data class.
	 */
	abstract public StepDataInterface getStepData();

	/**
	 * This method is called every time a new step is created and should allocate/set the step configuration
	 * to sensible defaults. The values set here will be used by Spoon when a new step is created.    
	 */
	public void setDefault() {
		timeOut = AbstractImportIOStepData.DEFAULT_TIME_OUT;
		nbRetries = AbstractImportIOStepData.DEFAULT_RETRIES;
		guidDefinedInAField = false;
		allocate(0);
	}
	
	/**
	 * Getter for the Import IO user ID
	 * @return the Import IO user ID
	 */
	public String getUserID() {
		return userID;
	}

	/**
	 * Setter for the Import IO user ID
	 * @param userID the Import IO user ID
	 */
	public void setUserID(String userID) {
		this.userID = userID;
	}
	
	/**
	 * Getter for the Import IO api key
	 * @return the Import IO api key
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * Setter for the Import IO api key
	 * @param apiKey the Import IO api key
	 */
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	/**
	 * Getter for the Import IO api key
	 * @return the Import IO api key
	 */
	public String getTimeOut() {
		return timeOut;
	}

	/**
	 * Setter for the Import IO user ID
	 * @param timeOut the Import IO user ID
	 */
	public void setTimeOut(String timeOut) {
		this.timeOut = timeOut;
	}

	/**
	 * Getter for the number of retries if the request fails
	 * @return the number of retries if the request fails
	 */
	public String getNbRetries() {
		return nbRetries;
	}

	/**
	 * Setter for the number of retries if the request fails
	 * @param nbRetries the number of retries if the request fails
	 */
	public void setNbRetries(String nbRetries) {
		this.nbRetries = nbRetries;
	}
	
	/**
	 * Getter for the Import IO GUID
	 * @return the Import IO GUID
	 */
	public String getGUID() {
		return guid;
	}

	/**
	 * Setter for the Import IO GUID
	 * @param guid the Import IO GUID
	 */
	public void setGUID(String guid) {
		this.guid = guid;
	}

	/**
	 * Getter for the output fields names
	 * @return the output fields names
	 */
	public String[] getOutputNames() {
		return outputNames;
	}

	/**
	 * Setter for the output fields names
	 * @param outputNames the output fields names
	 */
	public void setOutputNames(String[] outputNames) {
		this.outputNames = outputNames;
	}
	
	/**
	 * Getter for the names of the import io output fields
	 * @return the names of the import io output fields
	 */
	public String[] getOutputFieldImportIONames() {
		return outputFieldImportIONames;
	}

	/**
	 * Setter for the names of the import io output fields
	 * @param outputFieldImportIONames the names of the import io output fields
	 */
	public void setOutputFieldImportIONames(String[] outputFieldImportIONames) {
		this.outputFieldImportIONames = outputFieldImportIONames;
	}
	
	
	/**
	 * Getter for the boolean stating if the GUID is defined in an input field
	 * @return true if the GUID is defined in an input field, false if not.
	 */
	public boolean isGUIDDefinedInAField() {
		return guidDefinedInAField;
	}

	/**
	 * Setter for the boolean stating if the GUID is defined in an input field
	 * @param guidDefinedInAField
	 */
	public void setGUIDDefinedInAField(boolean guidDefinedInAField) {
		this.guidDefinedInAField = guidDefinedInAField;
	}

	/**
	 * Getter for the field name for the GUID
	 * @return the field name for the GUID
	 */
	public String getGuidFieldName() {
		return guidFieldName;
	}

	/**
	 * Setter for the field name for the GUID
	 * @param guidFieldName
	 */
	public void setGuidFieldName(String guidFieldName) {
		this.guidFieldName = guidFieldName;
	}

	/**
	 * Getter for the minimum time between queries
	 * @return the minimum time between queries
	 */
	public String getMinimumTime() {
		return minimumTime;
	}

	/**
	 * Setter for the minimum time between queries
	 * @param minimumTime
	 */
	public void setMinimumTime(String minimumTime) {
		this.minimumTime = minimumTime;
	}
	
	/**
	 * Allocate the array of parameters for the pdi step
	 * @param nbOutputs number of output parameters
	 */
	public void allocate(int nbOutputs)
	{
		outputNames              = new String[nbOutputs];
		outputFieldImportIONames = new String[nbOutputs];
	}
	
	
	/**
	 * This method is used when a step is duplicated in Spoon. It needs to return a deep copy of this
	 * step meta object. Be sure to create proper deep copies if the step configuration is stored in
	 * modifiable objects.
	 * 
	 * See org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta.clone() for an example on creating
	 * a deep copy.
	 * 
	 * @return a deep copy of this
	 */
	public Object clone() {
		AbstractImportIOStepMeta retval = (AbstractImportIOStepMeta)super.clone();
		
	    int nrfields = outputNames.length;

	    retval.allocate( nrfields );

	    for ( int i = 0; i < nrfields; i++ ) {
	      retval.outputNames[i] = outputNames[i];
	      retval.outputFieldImportIONames[i] = outputFieldImportIONames[i];
	    }
		
		
		return retval;
	}
	
	/**
	 * This method is called by Spoon when a step needs to serialize its configuration to XML. The expected
	 * return value is an XML fragment consisting of one or more XML tags.  
	 * 
	 * Please use org.pentaho.di.core.xml.XMLHandler to conveniently generate the XML.
	 * 
	 * @return a string containing the XML serialization of this step
	 */
	public String getXML() throws KettleValueException {
		StringBuilder xml = new StringBuilder("").append(XMLHandler.addTagValue("userID", userID))
												 .append(XMLHandler.addTagValue("apiKey", apiKey))
												 .append(XMLHandler.addTagValue("GUID", guid))
												 .append(XMLHandler.addTagValue("GUIDDefinedInAField", guidDefinedInAField))
												 .append(XMLHandler.addTagValue("GUIDField", guidFieldName))
												 .append(XMLHandler.addTagValue("timeOut", timeOut))
												 .append(XMLHandler.addTagValue("nbRetries", nbRetries))
												 .append(XMLHandler.addTagValue("minimumTime", minimumTime));
	    
		// Ouput Fields
	    xml.append( "<outputFields>" );
	    for ( int i = 0; i < outputNames.length; i++ ) {
	      xml.append( "<outputField>" );
	      xml.append(XMLHandler.addTagValue( "name", outputNames[i] ) );
	      xml.append(XMLHandler.addTagValue( "fieldImportIO", outputFieldImportIONames[i] ) );
	      xml.append( "</outputField>" );
	    }
	    xml.append( "</outputFields>" );
	    
		return xml.toString();
	}

	/**
	 * This method is called by PDI when a step needs to load its configuration from XML.
	 * 
	 * Please use org.pentaho.di.core.xml.XMLHandler to conveniently read from the
	 * XML node passed in.
	 * 
	 * @param stepnode	the XML node containing the configuration
	 * @param databases	the databases available in the transformation
	 * @param counters	the counters available in the transformation
	 */
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {

		try {
			setUserID(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "userID")));
			setApiKey(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "apiKey")));
			setGUID(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "GUID")));
			setGUIDDefinedInAField("Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "GUIDDefinedInAField" )));
			setGuidFieldName(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "GUIDField")));
			setTimeOut(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "timeOut")));
			setNbRetries(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "nbRetries")));
			setMinimumTime(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "minimumTime")));
			
	        Node outputFields = XMLHandler.getSubNode( stepnode, "outputFields" );
	        int nbOutputFields = XMLHandler.countNodes( outputFields, "outputField" );
	        
	        allocate(nbOutputFields);

	        for ( int i = 0; i < nbOutputFields; i++ ) {
	          Node line = XMLHandler.getSubNodeByNr( outputFields, "outputField", i );
	          outputNames[i] = XMLHandler.getTagValue( line, "name" );
	          outputFieldImportIONames[i] = XMLHandler.getTagValue( line, "fieldImportIO" );
	        }
		} catch (Exception e) {
			throw new KettleXMLException("Import IO Query plugin unable to read step info from XML node", e);
		}

	}	

	/**
	 * This method is called by Spoon when a step needs to serialize its configuration to a repository.
	 * The repository implementation provides the necessary methods to save the step attributes.
	 *
	 * @param rep					the repository to save to
	 * @param id_transformation		the id to use for the transformation when saving
	 * @param id_step				the id to use for the step  when saving
	 */
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try{
			//Saving the text inputs
			rep.saveStepAttribute(id_transformation, id_step, "userID", userID); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "apiKey", apiKey); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "GUID", guid); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "GUIDDefinedInAField", guidDefinedInAField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "GUIDField", guidFieldName); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "timeOut", timeOut); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "nbRetries", nbRetries); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "minimumTime", minimumTime); //$NON-NLS-1$
			//Saving the ouput fields
			for ( int i = 0; i < outputNames.length; i++ ) {
	          rep.saveStepAttribute( id_transformation, id_step, i, "outputField.name", outputNames[i] );
	          rep.saveStepAttribute( id_transformation, id_step, i, "outputField.fieldImportIO", outputFieldImportIONames[i] );
	        }
		}
		catch(Exception e){
			throw new KettleException("Unable to save step into repository: "+id_step, e); 
		}
	}		
	
	/**
	 * This method is called by PDI when a step needs to read its configuration from a repository.
	 * The repository implementation provides the necessary methods to read the step attributes.
	 * 
	 * @param rep		the repository to read from
	 * @param id_step	the id of the step bein
	 * @param databases	the databases available in the transformation
	 * @param counters	the counters available in the transformation
	 */
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
		try{
			userID              = rep.getStepAttributeString(id_step, "userID"); //$NON-NLS-1$
			apiKey              = rep.getStepAttributeString(id_step, "apiKey"); //$NON-NLS-1$
			guid                = rep.getStepAttributeString(id_step, "GUID"); //$NON-NLS-1$
			guidDefinedInAField = rep.getStepAttributeBoolean(id_step, "GUIDDefinedInAField"); //$NON-NLS-1$
			guidFieldName       = rep.getStepAttributeString(id_step, "GUIDField"); //$NON-NLS-1$
			timeOut             = rep.getStepAttributeString(id_step, "timeOut"); //$NON-NLS-1$
			nbRetries           = rep.getStepAttributeString(id_step, "nbRetries"); //$NON-NLS-1$
			minimumTime         = rep.getStepAttributeString(id_step, "minimumTime"); //$NON-NLS-1$
			
		    int nbOutputFields     = rep.countNrStepAttributes( id_step, getRepCode( "outputField.name" ) );
		    allocate(nbOutputFields);
		    
		    for ( int i = 0; i < nbOutputFields; i++ ) {
		    	outputNames[i] = rep.getStepAttributeString( id_step, i, getRepCode( "outputField.name" ) );
		    	outputFieldImportIONames[i] = rep.getStepAttributeString( id_step, i, getRepCode( "outputField.fieldImportIO" ) );
	        }
		}
		catch(Exception e){
			throw new KettleException("Unable to load step from repository", e);
		}
	}

	/**
	 * This method is called to determine the changes the step is making to the row-stream.
	 * To that end a RowMetaInterface object is passed in, containing the row-stream structure as it is when entering
	 * the step. This method must apply any changes the step makes to the row stream. Usually a step adds fields to the
	 * row-stream.
	 * 
	 * @param r			the row structure coming in to the step
	 * @param origin	the name of the step making the changes
	 * @param info		row structures of any info steps coming in
	 * @param nextStep	the description of a step this step is passing rows to
	 * @param space		the variable space for resolving variables
	 */
	public void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) {
		for(int i=0;i<outputFieldImportIONames.length;i++)
		{
			// a value meta object contains the meta data for a field
			ValueMetaInterface v = new ValueMeta();
			// set the name of the new field 
			v.setName(outputNames[i]);
			// type is going to be string
			v.setType(ValueMeta.TYPE_STRING);
			// setting trim type to "both"
			v.setTrimType(ValueMeta.TRIM_TYPE_BOTH);
			// the name of the step that adds this field
			v.setOrigin(origin);
			// modify the row structure and add the field this step generates  
			r.addValueMeta(v);
		}
	}

	/**
	 * This method is called when the user selects the "Verify Transformation" option in Spoon. 
	 * A list of remarks is passed in that this method should add to. Each remark is a comment, warning, error, or ok.
	 * The method should perform as many checks as necessary to catch design-time errors.
	 * 
	 * Typical checks include:
	 * - verify that all mandatory configuration is given
	 * - verify that the step receives any input, unless it's a row generating step
	 * - verify that the step does not receive any input if it does not take them into account
	 * - verify that the step finds fields it relies on in the row-stream
	 * 
	 *   @param remarks		the list of remarks to append to
	 *   @param transmeta	the description of the transformation
	 *   @param stepMeta	the description of the step
	 *   @param prev		the structure of the incoming row-stream
	 *   @param input		names of steps sending input to the step
	 *   @param output		names of steps this step is sending output to
	 *   @param info		fields coming in from info steps 
	 */
	public void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info) {
		
		CheckResult cr;

		//Verifying that the step parameters are set
		if (userID!=null && !userID.equals("")) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ImportIO.CheckResult.UserID.OK"), stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ImportIO.CheckResult.UserID.ERROR"), stepMeta);
			remarks.add(cr);
		}
		if (apiKey!=null && !apiKey.equals("")) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ImportIO.CheckResult.ApiKey.OK"), stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ImportIO.CheckResult.ApiKey.ERROR"), stepMeta);
			remarks.add(cr);
		}
		if (guid!=null && !guid.equals("")) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ImportIO.CheckResult.GUID.OK"), stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ImportIO.CheckResult.GUID.ERROR"), stepMeta);
			remarks.add(cr);
		}	
	}
}
