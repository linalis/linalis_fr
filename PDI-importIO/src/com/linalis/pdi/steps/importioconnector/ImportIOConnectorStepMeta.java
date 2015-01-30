package com.linalis.pdi.steps.importioconnector;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

import com.linalis.pdi.steps.AbstractImportIOStepMeta;

@Step(
		id = "ImportIOConnector",
		image = "img/connector.png",
		description ="ImportIO.plugin.connector.description",
		name = "ImportIO.plugin.connector.name",
		categoryDescription="ImportIO.plugin.connector.category",
		i18nPackageName="com.linalis.pdi.steps" 
	)

/**
 * This class is the representation of an import.io step meta.
 * All of the specific settings for the step (usually defined in the dialog) are found here :
 *  - startPage
 *  - maxPage
 *  - the input fields
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
 * @see AbstractImportIOStepMeta
 */
public class ImportIOConnectorStepMeta extends AbstractImportIOStepMeta {
	
	/**
	 * Import IO start page
	 */
	protected String startPage;
	
	/**
	 * Import IO max page
	 */
	protected String maxPage;
	
	/** input: Name of the import IO input fields */
	protected String[] importIOInputFields;
	
	/** output: Name of the import io field */
	protected String[] importIOInputFieldsValues;
	
	
	/**
	 * Constructor should call super() to make sure the base class has a chance to initialize properly.
	 */
	public ImportIOConnectorStepMeta() {
		super();
	}

	@Override
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta,
			TransMeta transMeta, String name) {
		return new ImportIOConnectorStepDialog(shell, meta, transMeta, name);
	}

	@Override
	public StepInterface getStep(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
			Trans disp) {
		return new ImportIOConnectorStep(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	@Override
	public StepDataInterface getStepData() {
		return new ImportIOConnectorStepData();
	}

	/**
	 * getter for the startPage parameter
	 * @return the startPage parameter
	 */
	public String getStartPage() {
		return startPage;
	}

	/**
	 * Setter for the startPage parameter
	 * @param startPage the startPage to set
	 */
	public void setStartPage(String startPage) {
		this.startPage = startPage;
	}

	/**
	 * getter for the maxPage parameter
	 * @return the maxPage parameter
	 */
	public String getMaxPage() {
		return maxPage;
	}

	/**
	 * Setter for the maxPage parameter
	 * @param maxPage the maxPage to set
	 */
	public void setMaxPage(String maxPage) {
		this.maxPage = maxPage;
	}
	
	/**
	 * Getter for the output fields names
	 * @return the output fields names
	 */
	public String[] getImportIOInputFields() {
		return importIOInputFields;
	}

	/**
	 * Setter for the output fields names
	 * @param importIOInputFields the output fields names
	 */
	public void setImportIOInputFields(String[] importIOInputFields) {
		this.importIOInputFields = importIOInputFields;
	}
	
	/**
	 * Getter for the names of the import io output fields
	 * @return the names of the import io output fields
	 */
	public String[] getImportIOInputFieldsValues() {
		return importIOInputFieldsValues;
	}

	/**
	 * Setter for the names of the import io output fields
	 * @param importIOInputFieldsValues the names of the import io output fields
	 */
	public void setImportIOInputFieldsValues(String[] importIOInputFieldsValues) {
		this.importIOInputFieldsValues = importIOInputFieldsValues;
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
		StringBuilder xml = new StringBuilder("").append(XMLHandler.addTagValue("startPage", startPage))
												 .append(XMLHandler.addTagValue("maxPage", maxPage));
		
		
		// Ouput Fields
	    xml.append( "<inputFields>" );
	    for ( int i = 0; i < importIOInputFields.length; i++ ) {
	      xml.append( "<inputField>" );
	      xml.append(XMLHandler.addTagValue( "name", importIOInputFields[i] ) );
	      xml.append(XMLHandler.addTagValue( "value", importIOInputFieldsValues[i] ) );
	      xml.append( "</inputField>" );
	    }
	    xml.append( "</inputFields>" );
	    xml.append(super.getXML());
	    
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
			setStartPage(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "startPage")));
			setMaxPage(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "maxPage")));
			
			
			Node inputFields = XMLHandler.getSubNode( stepnode, "inputFields" );
	        int nbInputFields = XMLHandler.countNodes( inputFields, "inputField" );
	        
	        allocateInputs(nbInputFields);

	        for ( int i = 0; i < nbInputFields; i++ ) {
	          Node line = XMLHandler.getSubNodeByNr( inputFields, "inputField", i );
	          importIOInputFields[i] = XMLHandler.getTagValue( line, "name" );
	          importIOInputFieldsValues[i] = XMLHandler.getTagValue( line, "value" );
	        }
			
			super.loadXML(stepnode, databases, counters);
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
			rep.saveStepAttribute(id_transformation, id_step, "startPage", startPage); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "maxPage", maxPage); //$NON-NLS-1$
			//Saving the input fields
			for ( int i = 0; i < importIOInputFields.length; i++ ) {
	          rep.saveStepAttribute( id_transformation, id_step, i, "inputField.name", importIOInputFields[i] );
	          rep.saveStepAttribute( id_transformation, id_step, i, "inputField.value", importIOInputFieldsValues[i] );
	        }
			super.saveRep(rep, id_transformation, id_step);
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
	 * @param id_step	the id of the step being read
	 * @param databases	the databases available in the transformation
	 * @param counters	the counters available in the transformation
	 */
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
		try{
			startPage = rep.getStepAttributeString(id_step, "startPage"); //$NON-NLS-1$
			maxPage   = rep.getStepAttributeString(id_step, "maxPage"); //$NON-NLS-1$
		    int nbInputFields     = rep.countNrStepAttributes( id_step, getRepCode( "inputField.name" ) );
		    allocateInputs(nbInputFields);
		    
		    for ( int i = 0; i < nbInputFields; i++ ) {
		    	importIOInputFields[i] = rep.getStepAttributeString( id_step, i, getRepCode( "inputField.name" ) );
		    	importIOInputFieldsValues[i] = rep.getStepAttributeString( id_step, i, getRepCode( "inputField.value" ) );
	        }
			super.readRep(rep, id_step, databases, counters);
		}
		catch(Exception e){
			throw new KettleException("Unable to load step from repository", e);
		}
	}
	
	/**
	 * This method is called every time a new step is created and should allocate/set the step configuration
	 * to sensible defaults. The values set here will be used by Spoon when a new step is created.    
	 */
	public void setDefault() {
		allocateInputs(0);
		super.setDefault();
	}
	

	/**
	 * Allocate the array of parameters for the pdi step
	 * @param nbInputs number of input parameters
	 */
	public void allocateInputs(int nbInputs)
	{
		importIOInputFields       = new String[nbInputs];
		importIOInputFieldsValues = new String[nbInputs];
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
		ImportIOConnectorStepMeta retval = (ImportIOConnectorStepMeta)super.clone();
		
	    int nrfields = importIOInputFields.length;

	    retval.allocateInputs( nrfields );

	    for ( int i = 0; i < nrfields; i++ ) {
	      retval.importIOInputFields[i] = importIOInputFields[i];
	      retval.importIOInputFieldsValues[i] = importIOInputFieldsValues[i];
	    }
		
		return retval;
	}

}
