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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.tableoutput.TableOutput;

import com.importio.api.clientlite.ImportIO;
import com.importio.api.clientlite.Session;

/**
* This class represents the dialog for the pdi step.
*   
* This class is the implementation of StepDialogInterface.
* Classes implementing this interface need to:
* 
* - build and open a SWT dialog displaying the step's settings (stored in the step's meta object)
* - write back any changes the user makes to the step's meta object
* - report whether the user changed any settings when confirming the dialog 
* 
*/
public class ImportIOExtractorStepDialog extends BaseStepDialog implements StepDialogInterface {

	/**
	 *	The PKG member is used when looking up internationalized strings.
	 *	The properties file with localized keys is expected to reside in 
	 *	{the package of the class specified}/messages/messages_{locale}.properties   
	 */
	private static Class<?> PKG = ImportIOExtractorStepDialog.class; // for i18n purposes

	/**
	 *  this is the object the stores the step's settings
	 */
	private ImportIOExtractorStepMeta meta;

	/**
	 *  text field for the user ID field
	 */
	private TextVar textUserID;
	
	/**
	 *  text field for the api key
	 */
	private TextVar textApiKey;
	
	/**
	 *  text field for the GUID
	 */
	private Text textGUID;
	
	/**
	 *  Check box, checked if the guid is defined in an input field
	 */
	private Button checkGUIDInAField;
	
	/**
	 *  Name of the field holding the GUID
	 */
	private CCombo textGUIDField;
	
	/**
	 *  text field for the time out
	 */
	private Text textTimeOut;
	
	/**
	 *  text field for the number of retries
	 */
	private Text textNbRetries;
	
	/**
	 *  text field for minimum time between queries
	 */
	private Text textMinimumTime;
	
	/**
	 *  text field the webpage/url parameter
	 */
	private Text textWebPageURL;
	
	/**
	 *  Check box, checked if the webpage/url is defined in an input field
	 */
	private Button checkWebPageURLInAField;
	
	/**
	 *  Name of the field holding the GUID
	 */
	private CCombo textWebpageURLField;

	private Composite compOutput;
	private FormData fdCompOutput;
	private TableView tableOuput;
	private FormData fdTableOuput;
	
	private Button buttonGetOutputs;
	private FormData fdButtonGetOutputs;
	
	private boolean getpreviousFields = false;

	/**
	 * The constructor should simply invoke super() and save the incoming meta
	 * object to a local variable, so it can conveniently read and write settings
	 * from/to it.
	 * 
	 * @param parent 	the SWT shell to open the dialog in
	 * @param in		the meta object holding the step's settings
	 * @param transMeta	transformation description
	 * @param sname		the step name
	 */
	public ImportIOExtractorStepDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		meta = (ImportIOExtractorStepMeta) in;
	}

	/**
	 * This method is called by Spoon when the user opens the settings dialog of the step.
	 * It should open the dialog and return only once the dialog has been closed by the user.
	 * 
	 * If the user confirms the dialog, the meta object (passed in the constructor) must
	 * be updated to reflect the new step settings. The changed flag of the meta object must 
	 * reflect whether the step configuration was changed by the dialog.
	 * 
	 * If the user cancels the dialog, the meta object must not be updated, and its changed flag
	 * must remain unaltered.
	 * 
	 * The open() method must return the name of the step after the user has confirmed the dialog,
	 * or null if the user cancelled the dialog.
	 */
	public String open() {

		// store some convenient SWT variables 
		Shell parent = getParent();
		final Display display = parent.getDisplay();

		// SWT code for preparing the dialog
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		props.setLook(shell);
		setShellImage(shell, meta);
		
		// Save the value of the changed flag on the meta object. If the user cancels
		// the dialog, it will be restored to this saved value.
		// The "changed" variable is inherited from BaseStepDialog
		changed = meta.hasChanged();
		
		// The ModifyListener used on all controls. It will update the meta object to 
		// indicate that changes are being made.
		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				meta.setChanged();
			}
		};
		
		// ------------------------------------------------------- //
		// SWT code for building the actual settings dialog        //
		// ------------------------------------------------------- //
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "ImportIO.Shell.Title")); 

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName")); 
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		
		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		// user id field value
		Label labelUserID = new Label(shell, SWT.RIGHT);
		labelUserID.setText(BaseMessages.getString(PKG, "ImportIO.UserID.Label")); 
		props.setLook(labelUserID);
		FormData fdLabelUserID = new FormData();
		fdLabelUserID.left = new FormAttachment(0, 0);
		fdLabelUserID.right = new FormAttachment(middle, -margin);
		fdLabelUserID.top = new FormAttachment(wStepname, margin);
		labelUserID.setLayoutData(fdLabelUserID);

		textUserID = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(textUserID);
		textUserID.addModifyListener(lsMod);
		FormData fdUserID = new FormData();
		fdUserID.left = new FormAttachment(middle, 0);
		fdUserID.right = new FormAttachment(100, 0);
		fdUserID.top = new FormAttachment(wStepname, margin);
		textUserID.setLayoutData(fdUserID);
		
		// api key field value
		Label labelApiKey = new Label(shell, SWT.RIGHT);
		labelApiKey.setText(BaseMessages.getString(PKG, "ImportIO.ApiKey.Label")); 
		props.setLook(labelApiKey);
		FormData fdLabelApiKey = new FormData();
		fdLabelApiKey.left = new FormAttachment(0, 0);
		fdLabelApiKey.right = new FormAttachment(middle, -margin);
		fdLabelApiKey.top = new FormAttachment(textUserID, margin);
		labelApiKey.setLayoutData(fdLabelApiKey);

		textApiKey = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(textApiKey);
		textApiKey.addModifyListener(lsMod);
		FormData fdApiKey = new FormData();
		fdApiKey.left = new FormAttachment(middle, 0);
		fdApiKey.right = new FormAttachment(100, 0);
		fdApiKey.top = new FormAttachment(textUserID, margin);
		textApiKey.setLayoutData(fdApiKey);
		
		// GUID field value
		Label labelGUID = new Label(shell, SWT.RIGHT);
		labelGUID.setText(BaseMessages.getString(PKG, "ImportIO.GUID.Label")); 
		props.setLook(labelGUID);
		FormData fdLabelGUID = new FormData();
		fdLabelGUID.left = new FormAttachment(0, 0);
		fdLabelGUID.right = new FormAttachment(middle, -margin);
		fdLabelGUID.top = new FormAttachment(textApiKey, margin);
		labelGUID.setLayoutData(fdLabelGUID);

		textGUID = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(textGUID);
		textGUID.addModifyListener(lsMod);
		FormData fdGUID = new FormData();
		fdGUID.left = new FormAttachment(middle, 0);
		fdGUID.right = new FormAttachment(100, 0);
		fdGUID.top = new FormAttachment(textApiKey, margin);
		textGUID.setLayoutData(fdGUID);
		
	    // Is GUID string defined in a Field
	    Label labelCheckGUIDInAField = new Label( shell, SWT.RIGHT );
	    labelCheckGUIDInAField.setText( BaseMessages.getString( PKG, "ImportIO.GUIDDefinedInAField.Label" ) );
	    props.setLook( labelCheckGUIDInAField );
	    FormData fdLabelCheckGUIDInAField = new FormData();
	    fdLabelCheckGUIDInAField.left = new FormAttachment( 0, -margin );
	    fdLabelCheckGUIDInAField.top = new FormAttachment( textGUID, margin );
	    fdLabelCheckGUIDInAField.right = new FormAttachment( middle, -2 * margin );
	    labelCheckGUIDInAField.setLayoutData( fdLabelCheckGUIDInAField );
	    
		checkGUIDInAField = new Button(shell, SWT.CHECK);
		checkGUIDInAField.setToolTipText(BaseMessages.getString(PKG, "ImportIO.GUIDDefinedInAField.Label"));
		FormData fdCheckGUIDInAField = new FormData();
		fdCheckGUIDInAField.left = new FormAttachment(middle, 0);
		fdCheckGUIDInAField.right = new FormAttachment(100, 0);
		fdCheckGUIDInAField.top = new FormAttachment(textGUID, margin);
		checkGUIDInAField.setLayoutData(fdCheckGUIDInAField);
		SelectionAdapter lsxGUIDField = new SelectionAdapter() {
	      public void widgetSelected( SelectionEvent arg0 ) {
		    	if(checkGUIDInAField.getSelection())
		    	{
		    		textGUID.setEnabled(false);
		    		textGUID.setText("");
		    		textGUIDField.setEnabled(true);
		    		if(meta.getGuidFieldName()!=null)
		    			textGUIDField.setText(meta.getGuidFieldName());
		    	}
		    	else
		    	{
		    		textGUID.setEnabled(true);
		    		if(meta.getGUID()!=null)
		    			textGUID.setText(meta.getGUID());
		    		textGUIDField.setEnabled(false);
		    		textGUIDField.setText("");
		    	}
		    	setPreviousFieldsCombos();
		    	meta.setChanged();
	      }
	    };
	    checkGUIDInAField.addSelectionListener( lsxGUIDField );
	    
		// GUIDField field value
		Label labelGUIDField = new Label(shell, SWT.RIGHT);
		labelGUIDField.setText(BaseMessages.getString(PKG, "ImportIO.GUIDField.Label")); 
		props.setLook(labelGUIDField);
		FormData fdLabelGUIDField = new FormData();
		fdLabelGUIDField.left = new FormAttachment(0, 0);
		fdLabelGUIDField.right = new FormAttachment(middle, -margin);
		fdLabelGUIDField.top = new FormAttachment(checkGUIDInAField, margin);
		labelGUIDField.setLayoutData(fdLabelGUIDField);
		
		textGUIDField = new CCombo( shell, SWT.BORDER | SWT.READ_ONLY );
		textGUIDField.setEditable( true );
		props.setLook(textGUIDField);
		textGUIDField.addModifyListener(lsMod);
		FormData fdGUIDField = new FormData();
		fdGUIDField.left = new FormAttachment(middle, 0);
		fdGUIDField.right = new FormAttachment(100, 0);
		fdGUIDField.top = new FormAttachment(checkGUIDInAField, margin);
		textGUIDField.setLayoutData(fdGUIDField);
		
		// TimeOut field value
		Label labelTimeOut = new Label(shell, SWT.RIGHT);
		labelTimeOut.setText(BaseMessages.getString(PKG, "ImportIO.TimeOut.Label")); 
		props.setLook(labelTimeOut);
		FormData fdLabelTimeOut = new FormData();
		fdLabelTimeOut.left = new FormAttachment(0, 0);
		fdLabelTimeOut.right = new FormAttachment(middle, -margin);
		fdLabelTimeOut.top = new FormAttachment(textGUIDField, margin);
		labelTimeOut.setLayoutData(fdLabelTimeOut);

		textTimeOut = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(textTimeOut);
		textTimeOut.addModifyListener(lsMod);
		FormData fdTimeOut = new FormData();
		fdTimeOut.left = new FormAttachment(middle, 0);
		fdTimeOut.right = new FormAttachment(100, 0);
		fdTimeOut.top = new FormAttachment(textGUIDField, margin);
		textTimeOut.setLayoutData(fdTimeOut);
		
		// NbRetries field value
		Label labelNbRetries = new Label(shell, SWT.RIGHT);
		labelNbRetries.setText(BaseMessages.getString(PKG, "ImportIO.NbRetries.Label")); 
		props.setLook(labelNbRetries);
		FormData fdLabelNbRetries = new FormData();
		fdLabelNbRetries.left = new FormAttachment(0, 0);
		fdLabelNbRetries.right = new FormAttachment(middle, -margin);
		fdLabelNbRetries.top = new FormAttachment(textTimeOut, margin);
		labelNbRetries.setLayoutData(fdLabelNbRetries);

		textNbRetries = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(textNbRetries);
		textNbRetries.addModifyListener(lsMod);
		FormData fdNbRetries = new FormData();
		fdNbRetries.left = new FormAttachment(middle, 0);
		fdNbRetries.right = new FormAttachment(100, 0);
		fdNbRetries.top = new FormAttachment(textTimeOut, margin);
		textNbRetries.setLayoutData(fdNbRetries);
		
		// Minimum time between queries
		Label labelMinimumTime = new Label(shell, SWT.RIGHT);
		labelMinimumTime.setText(BaseMessages.getString(PKG, "ImportIO.MinimumTime.Label")); 
		props.setLook(labelMinimumTime);
		FormData fdLabelMinimumTime = new FormData();
		fdLabelMinimumTime.left = new FormAttachment(0, 0);
		fdLabelMinimumTime.right = new FormAttachment(middle, -margin);
		fdLabelMinimumTime.top = new FormAttachment(textNbRetries, margin);
		labelMinimumTime.setLayoutData(fdLabelMinimumTime);

		textMinimumTime = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(textMinimumTime);
		textMinimumTime.addModifyListener(lsMod);
		FormData fdTextMinimumTile = new FormData();
		fdTextMinimumTile.left = new FormAttachment(middle, 0);
		fdTextMinimumTile.right = new FormAttachment(100, 0);
		fdTextMinimumTile.top = new FormAttachment(textNbRetries, margin);
		textMinimumTime.setLayoutData(fdTextMinimumTile);
		
		// webpage/url value
		Label labelWebpageURL = new Label(shell, SWT.RIGHT);
		labelWebpageURL.setText(BaseMessages.getString(PKG, "ImportIO.WebpageURL.Label")); 
		props.setLook(labelWebpageURL);
		FormData fdLabelWebpageURL = new FormData();
		fdLabelWebpageURL.left = new FormAttachment(0, 0);
		fdLabelWebpageURL.right = new FormAttachment(middle, -margin);
		fdLabelWebpageURL.top = new FormAttachment(textMinimumTime, margin);
		labelWebpageURL.setLayoutData(fdLabelWebpageURL);

		textWebPageURL = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(textWebPageURL);
		textWebPageURL.addModifyListener(lsMod);
		FormData fdWebpageURL = new FormData();
		fdWebpageURL.left = new FormAttachment(middle, 0);
		fdWebpageURL.right = new FormAttachment(100, 0);
		fdWebpageURL.top = new FormAttachment(textMinimumTime, margin);
		textWebPageURL.setLayoutData(fdWebpageURL);
		
	    // Is webpage/url defined in a Field
	    Label labelCheckWebpageURLInAField = new Label( shell, SWT.RIGHT );
	    labelCheckWebpageURLInAField.setText( BaseMessages.getString( PKG, "ImportIO.WebpageURLDefinedInAField.Label" ) );
	    props.setLook( labelCheckWebpageURLInAField );
	    FormData fdLabelWebpageURLGUIDInAField = new FormData();
	    fdLabelWebpageURLGUIDInAField.left = new FormAttachment( 0, -margin );
	    fdLabelWebpageURLGUIDInAField.top = new FormAttachment( textWebPageURL, margin );
	    fdLabelWebpageURLGUIDInAField.right = new FormAttachment( middle, -2 * margin );
	    labelCheckWebpageURLInAField.setLayoutData( fdLabelWebpageURLGUIDInAField );
	    
		checkWebPageURLInAField = new Button(shell, SWT.CHECK);
		checkWebPageURLInAField.setToolTipText(BaseMessages.getString(PKG, "ImportIO.GUIDDefinedInAField.Label"));
		FormData fdCheckWebpageURLInAField = new FormData();
		fdCheckWebpageURLInAField.left = new FormAttachment(middle, 0);
		fdCheckWebpageURLInAField.right = new FormAttachment(100, 0);
		fdCheckWebpageURLInAField.top = new FormAttachment(textWebPageURL, margin);
		checkWebPageURLInAField.setLayoutData(fdCheckWebpageURLInAField);
		SelectionAdapter lsxWebpageURLField = new SelectionAdapter() {
	      public void widgetSelected( SelectionEvent arg0 ) {
	    	if(checkWebPageURLInAField.getSelection())
	    	{
	    		textWebPageURL.setEnabled(false);
	    		textWebPageURL.setText("");
	    		textWebpageURLField.setEnabled(true);
	    		if(meta.getWebpageURLFieldName()!=null)
	    			textWebpageURLField.setText(meta.getWebpageURLFieldName());
	    	}
	    	else
	    	{
	    		textWebPageURL.setEnabled(true);
	    		if(meta.getWebpageURL()!=null)
	    			textWebPageURL.setText(meta.getWebpageURL());
	    		textWebpageURLField.setEnabled(false);
	    		textWebpageURLField.setText("");
	    	}
	    	setPreviousFieldsCombos();
	        meta.setChanged();
	      }
	    };
	    checkWebPageURLInAField.addSelectionListener( lsxWebpageURLField );
	    
		// GUIDField field value
		Label labelWebpageURLField = new Label(shell, SWT.RIGHT);
		labelWebpageURLField.setText(BaseMessages.getString(PKG, "ImportIO.WebpageURLField.Label")); 
		props.setLook(labelWebpageURLField);
		FormData fdLabelWebpageURLField = new FormData();
		fdLabelWebpageURLField.left = new FormAttachment(0, 0);
		fdLabelWebpageURLField.right = new FormAttachment(middle, -margin);
		fdLabelWebpageURLField.top = new FormAttachment(checkWebPageURLInAField, margin);
		labelWebpageURLField.setLayoutData(fdLabelWebpageURLField);

		textWebpageURLField = new CCombo( shell, SWT.BORDER | SWT.READ_ONLY );
		textWebpageURLField.setEditable( true );
		props.setLook(textWebpageURLField);
		textGUIDField.addModifyListener(lsMod);
		FormData fdWebpageURLField = new FormData();
		fdWebpageURLField.left = new FormAttachment(middle, 0);
		fdWebpageURLField.right = new FormAttachment(100, 0);
		fdWebpageURLField.top = new FormAttachment(checkWebPageURLInAField, margin);
		textWebpageURLField.setLayoutData(fdWebpageURLField);
		    
	      //////////////////////////////
	     // Start of ouput composite //
	    //////////////////////////////

	    compOutput = new Composite( shell, SWT.NONE );
	    props.setLook( compOutput );

	    FormLayout outputLayout = new FormLayout();
	    outputLayout.marginWidth = margin;
	    outputLayout.marginHeight = margin;
	    
	    compOutput.setLayout( outputLayout );
	    
		// "Output fields" label
		Label labelOutputFields = new Label(compOutput, SWT.LEFT);
		labelOutputFields.setText(BaseMessages.getString(PKG, "ImportIO.OutputFields.Label")); 
		props.setLook(labelOutputFields);
		FormData fdLabelOutputFields = new FormData();
		fdLabelOutputFields.left = new FormAttachment(0, margin);
		fdLabelOutputFields.top = new FormAttachment(textWebpageURLField, margin);
		labelOutputFields.setLayoutData(fdLabelOutputFields);
	    
	    final int outputCols = 2;
	    final int outputRows = meta.getOutputNames().length;

	    ColumnInfo[] colInfOutput = new ColumnInfo[outputCols];
	    colInfOutput[0] =
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "ImportIO.ColumnInfo.outputImportIOField" ), ColumnInfo.COLUMN_TYPE_TEXT,
	        false );
	    colInfOutput[1] =
	      new ColumnInfo(
	        BaseMessages.getString( PKG, "ImportIO.ColumnInfo.outputName" ), ColumnInfo.COLUMN_TYPE_TEXT,
	        false );
	    
	    //outputColumns.add( colInfOutput[0] );
	    tableOuput =
	      new TableView(
	        transMeta, compOutput, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colInfOutput, outputRows, lsMod, props );
	    
	    buttonGetOutputs = new Button( compOutput, SWT.PUSH );
	    buttonGetOutputs.setText( BaseMessages.getString( PKG, "ImportIO.GetOutputs.Button" ) );
	    fdButtonGetOutputs = new FormData();
	    fdButtonGetOutputs.right = new FormAttachment( 100, 0 );
	    fdButtonGetOutputs.top = new FormAttachment( middle, margin );
	    buttonGetOutputs.setLayoutData( fdButtonGetOutputs );
	    
	    fdTableOuput = new FormData();
	    fdTableOuput.left = new FormAttachment( 0, 0 );
	    fdTableOuput.top = new FormAttachment( labelOutputFields, margin );
	    fdTableOuput.right = new FormAttachment( buttonGetOutputs, -margin );
	    fdTableOuput.bottom = new FormAttachment( 100, -margin );
	    tableOuput.setLayoutData( fdTableOuput );
	    
	    fdCompOutput = new FormData();
	    fdCompOutput.left = new FormAttachment( 0, 0 );
	    fdCompOutput.top = new FormAttachment( textWebpageURLField, 0 );
	    fdCompOutput.right = new FormAttachment( 100, 0 );
	    fdCompOutput.bottom = new FormAttachment( 100, -50 );
	    compOutput.setLayoutData( fdCompOutput );

	    compOutput.layout();
	    
	    
		// OK and cancel buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); 
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); 

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, compOutput);

		// Add listeners for cancel and OK
		lsCancel = new Listener() {
			public void handleEvent(Event e) {cancel();}
		};
		lsOK = new Listener() {
			public void handleEvent(Event e) {ok();}
		};
	    lsGet = new Listener() {
	        public void handleEvent( Event e ) {getFields();}
	    };

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);
		buttonGetOutputs.addListener( SWT.Selection, lsGet );

		// default listener (for hitting "enter")
		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {ok();}
		};
		wStepname.addSelectionListener(lsDef);
		textUserID.addSelectionListener(lsDef);
		textApiKey.addSelectionListener(lsDef);
		textGUID.addSelectionListener(lsDef);
		textTimeOut.addSelectionListener(lsDef);
		textNbRetries.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window and cancel the dialog properly
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {cancel();}
		});
		
		// Set/Restore the dialog size based on last position on screen
		// The setSize() method is inherited from BaseStepDialog
		setSize();

		// populate the dialog with the values from the meta object
		populateDialog();
		
		// restore the changed flag to original value, as the modify listeners fire during dialog population 
		meta.setChanged(changed);

		// open dialog and enter event loop 
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		// at this point the dialog has closed, so either ok() or cancel() have been executed
		// The "stepname" variable is inherited from BaseStepDialog
		return stepname;
	}
	
	/**
	 * This helper method puts the step configuration stored in the meta object
	 * and puts it into the dialog controls.
	 */
	private void populateDialog() {
		wStepname.selectAll();
		if(meta.getUserID()!=null)
			textUserID.setText(meta.getUserID());
		if(meta.getApiKey()!=null)
			textApiKey.setText(meta.getApiKey());
		
		if(meta.getGUID()!=null)
			textGUID.setText(meta.getGUID());
		textGUID.setEnabled(!meta.isGUIDDefinedInAField());
		checkGUIDInAField.setSelection(meta.isGUIDDefinedInAField());
		if(meta.getGuidFieldName()!=null)
			textGUIDField.setText(meta.getGuidFieldName());
		textGUIDField.setEnabled(meta.isGUIDDefinedInAField());
		
		if(meta.getTimeOut()!=null)
			textTimeOut.setText(meta.getTimeOut());
		if(meta.getNbRetries()!=null)
			textNbRetries.setText(meta.getNbRetries());
		if(meta.getMinimumTime()!=null)
			textMinimumTime.setText(meta.getMinimumTime());
		
		if(meta.getWebpageURL()!=null)
			textWebPageURL.setText(meta.getWebpageURL());
		textWebPageURL.setEnabled(!meta.isWebpageURLDefinedInAField());
		checkWebPageURLInAField.setSelection(meta.isWebpageURLDefinedInAField());
		if(meta.getWebpageURLFieldName()!=null)
			textWebpageURLField.setText(meta.getWebpageURLFieldName());
		textWebpageURLField.setEnabled(meta.isWebpageURLDefinedInAField());
	    
		// Outputs
	    if ( meta.getOutputFieldImportIONames() != null && meta.getOutputFieldImportIONames().length > 0 )
	    {
	        for ( int i = 0; i < meta.getOutputFieldImportIONames().length; i++ ) {
	          TableItem item = tableOuput.table.getItem( i );
	          if ( meta.getOutputFieldImportIONames()[i] != null ) {
		            item.setText( 1, meta.getOutputFieldImportIONames()[i] );
		          }
	          if ( meta.getOutputNames()[i] != null ) {
	            item.setText( 2, meta.getOutputNames()[i] );
	          }
	        }
	        tableOuput.removeEmptyRows();
	        tableOuput.setRowNums();
	        tableOuput.optWidth( true );
	        tableOuput.setSelection(new int[]{0});
	        tableOuput.table.getVerticalBar().setVisible(true);
	    }
	    
	}

	/**
	 * Called when the user cancels the dialog.  
	 */
	private void cancel() {
		// The "stepname" variable will be the return value for the open() method. 
		// Setting to null to indicate that dialog was cancelled.
		stepname = null;
		// Restoring original "changed" flag on the met aobject
		meta.setChanged(changed);
		// close the SWT dialog window
		dispose();
	}
	
	/**
	 * Called when the user confirms the dialog
	 */
	private void ok() {
		// The "stepname" variable will be the return value for the open() method. 
		// Setting to step name from the dialog control
		stepname = wStepname.getText(); 
		// Setting the  settings to the meta object
		meta.setUserID(textUserID.getText());
		meta.setApiKey(textApiKey.getText());
		meta.setGUID(textGUID.getText());
		meta.setGUIDDefinedInAField(checkGUIDInAField.getSelection());
		meta.setGuidFieldName(textGUIDField.getText());
		meta.setTimeOut(textTimeOut.getText());
		meta.setNbRetries(textNbRetries.getText());
		meta.setMinimumTime(textMinimumTime.getText());
		meta.setWebpageURL(textWebPageURL.getText());
		meta.setWebpageURLDefinedInAField(checkWebPageURLInAField.getSelection());
		meta.setWebpageURLFieldName(textWebpageURLField.getText());
		
		// Output fields
	    int nbOutputFields     = tableOuput.nrNonEmpty();
	    meta.allocate(nbOutputFields);

	    for ( int i = 0; i < nbOutputFields; i++ ) {
	        TableItem item = tableOuput.getNonEmpty( i );
	        meta.getOutputFieldImportIONames()[i] = item.getText( 1 );
	        meta.getOutputNames()[i] = item.getText( 2 );
	    }
	    
		// close the SWT dialog window
		dispose();
	}
	
	private void getFields()
	{
		ImportIO client = new ImportIO(UUID.fromString(transMeta.environmentSubstitute(textUserID.getText())), transMeta.environmentSubstitute(textApiKey.getText()));
		try {
			System.out.println("userid : " + transMeta.environmentSubstitute(textUserID.getText()) +" apikey : " +  transMeta.environmentSubstitute(textApiKey.getText()));
			client.connect();
		} catch (IOException e) {
			logError(BaseMessages.getString(PKG, "ImportIO.Connection.KO"));
			MessageBox box = new MessageBox(shell, SWT.ERROR|SWT.OK);
			box.setMessage(BaseMessages.getString(PKG, "ImportIO.Connection.KO"));
			box.open();
			return;
		}
		
		ArrayList<String> fieldNames;
		
		try {
			fieldNames = client.schemaRequest(textGUID.getText(), Session.TYPE_OUTPUT_FIELDS);
		} catch (Exception e) {
			logError(BaseMessages.getString(PKG, "ImportIO.SchemaRequest.KO") + " " + e.getMessage());
			MessageBox box = new MessageBox(shell, SWT.ERROR|SWT.OK);
			box.setMessage(BaseMessages.getString(PKG, "ImportIO.SchemaRequest.KO") + " " + e.getMessage());
			box.open();
			return;
		}
		
		// Outputs
		//tableOuput.table.clearAll();
		
		List<String> keys = new ArrayList<String>();
	    for ( int i = 0; i < tableOuput.getItemCount(); i++ ) {
	      TableItem tableItem = tableOuput.table.getItem( i );
	      String key = tableItem.getText( 1 );
	      if ( !Const.isEmpty( key ) && keys.indexOf( key ) < 0 ) {
	        keys.add( key );
	      }
	    }
	    
	    
        for (String fieldName: fieldNames) {
        	if ( keys.indexOf( fieldName ) >= 0 ) 
        		continue;
        	
        	TableItem item = new TableItem(tableOuput.table, SWT.NONE);
        
        	if ( fieldName != null ) {
	            item.setText( 1, fieldName);
	            item.setText( 2, fieldName);
        	}
        }
        tableOuput.removeEmptyRows();
        tableOuput.setRowNums();
        tableOuput.optWidth( true );
        tableOuput.table.setTopIndex(0);
	}
	
	  private void setPreviousFieldsCombos() {
		    try {
		      if ( !getpreviousFields ) {
		        getpreviousFields = true;
		        String guid = textGUIDField.getText();
		        String webpageurl = textWebpageURLField.getText();

		        textGUIDField.removeAll();
		        textWebpageURLField.removeAll();

		        RowMetaInterface r = transMeta.getPrevStepFields( stepname );
		        if ( r != null ) {
		          textGUIDField.setItems( r.getFieldNames() );
		          textWebpageURLField.setItems( r.getFieldNames() );
		        }
		        if ( guid != null )
		        	textGUIDField.setText( guid );
		        if(webpageurl!=null)
		        	textWebpageURLField.setText( webpageurl );
		        	
		      }
		    } catch ( KettleException ke ) {
		      new ErrorDialog(
		        shell, BaseMessages.getString( PKG, "ImportIO.GetPreviousFields.KO.Title" ), BaseMessages
		          .getString( PKG, "ImportIO.GetPreviousFields.KO.Message" ), ke );
		    }
		  }
}
