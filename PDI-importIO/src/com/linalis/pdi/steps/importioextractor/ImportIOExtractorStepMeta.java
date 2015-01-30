package com.linalis.pdi.steps.importioextractor;

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
		id = "ImportIOExtractor",
		image = "img/extractor.png",
		description ="ImportIO.plugin.extractor.description",
		name = "ImportIO.plugin.extractor.name",
		categoryDescription="ImportIO.plugin.extractor.category",
		i18nPackageName="com.linalis.pdi.steps" 
	)

/**
 * This class is the representation of an import.io step meta.
 * All of the specific settings for the step (usually defined in the dialog) are found here :
 *  - webpageUrl
 *  - webpageUrl is defined in a field
 *  - webpageUrl field name
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
public class ImportIOExtractorStepMeta extends AbstractImportIOStepMeta {
	
	/**
	 * Import IO webpage/url
	 */
	protected String webpageURL;
	
	/**
	 * webpage/url is defined in a field?
	 */
	protected boolean webpageURLDefinedInAField;
	
	/**
	 * Field name for webpage/url
	 */
	protected String webpageURLFieldName;
	
	
	/**
	 * Constructor should call super() to make sure the base class has a chance to initialize properly.
	 */
	public ImportIOExtractorStepMeta() {
		super();
	}

	@Override
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta,
			TransMeta transMeta, String name) {
		return new ImportIOExtractorStepDialog(shell, meta, transMeta, name);
	}

	@Override
	public StepInterface getStep(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
			Trans disp) {
		return new ImportIOExtractorStep(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	@Override
	public StepDataInterface getStepData() {
		return new ImportIOExtractorStepData();
	}

	/**
	 * Getter for the webpageUrl parameter
	 * @return the webpageUrl parameter
	 */
	public String getWebpageURL() {
		return webpageURL;
	}

	/**
	 * Setter for the webpageUrl parameter
	 * @param webpageURL the webpageUrl parameter
	 */
	public void setWebpageURL(String webpageURL) {
		this.webpageURL = webpageURL;
	}

	/**
	 * Getter for the "webpageUrl is defined in a field?" parameter
	 * @return the webpageUrl the "webpageUrl is defined in a field?" parameter
	 */
	public boolean isWebpageURLDefinedInAField() {
		return webpageURLDefinedInAField;
	}

	/**
	 * Setter for the "webpageUrl is defined in a field?" parameter
	 * @param webpageURLDefinedInAField the "webpageUrl is defined in a field?" parameter
	 */
	public void setWebpageURLDefinedInAField(boolean webpageURLDefinedInAField) {
		this.webpageURLDefinedInAField = webpageURLDefinedInAField;
	}

	/**
	 * Getter for the webpageUrl field name parameter
	 * @return the webpageUrl field name parameter
	 */
	public String getWebpageURLFieldName() {
		return webpageURLFieldName;
	}

	/**
	 * Setter the webpageUrl field name parameter
	 * @param webpageURLFieldName the webpageUrl field name parameter
	 */
	public void setWebpageURLFieldName(String webpageURLFieldName) {
		this.webpageURLFieldName = webpageURLFieldName;
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
		StringBuilder xml = new StringBuilder("").append(XMLHandler.addTagValue("webpageURL", webpageURL))
												 .append(XMLHandler.addTagValue("webpageURLDefinedInAField", webpageURLDefinedInAField))
												 .append(XMLHandler.addTagValue("webpageURLField", webpageURLFieldName));
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
			setWebpageURL(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "webpageURL")));
			setWebpageURLDefinedInAField("Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "webpageURLDefinedInAField" )));
			setWebpageURLFieldName(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "webpageURLField")));
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
			rep.saveStepAttribute(id_transformation, id_step, "webpageURL", webpageURL); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "webpageURLDefinedInAField", webpageURLDefinedInAField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "webpageURLField", webpageURLFieldName); //$NON-NLS-1$
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
			webpageURL                = rep.getStepAttributeString(id_step, "webpageURL"); //$NON-NLS-1$
			webpageURLDefinedInAField = rep.getStepAttributeBoolean(id_step, "webpageURLDefinedInAField"); //$NON-NLS-1$
			webpageURLFieldName       = rep.getStepAttributeString(id_step, "webpageURLFieldName"); //$NON-NLS-1$
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
		webpageURLDefinedInAField = false;
		super.setDefault();
	}
}
