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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package com.linalis.pdi.steps.redisInput;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class RedisInputDialog extends BaseStepDialog implements
		StepDialogInterface {
	private static Class<?> PKG = RedisInputMeta.class; // for i18n purposes,
														// needed by
														// Translator2!!
														// $NON-NLS-1$
	private RedisInputMeta input;
	private boolean gotPreviousFields = false;
	private RowMetaInterface previousFields;
	private Label wlHostname;
	private TextVar  wHostname;
	private FormData fdlHostname, fdHostname;
	private Label wlPort;
	private TextVar  wPort;
	private FormData fdlPort, fdPort;
	private Label wlBase;
	private TextVar  wBase;
	private FormData fdlBase, fdBase;
	private Label wlKey;
	private Text wKey;
	private FormData fdlKey, fdKey;
	private Label wlKeyFieldCheck;
	private Button wKeyFieldCheck;
	private FormData fdlKeyFieldCheck, fdKeyFieldCheck;
	private Label wlKeyFieldCombo;
	private CCombo wKeyFieldCombo;
	private FormData fdlKeyFieldCombo, fdKeyFieldCombo;
	private Label wlValueField;
	private Text wValueField;
	private FormData fdlValueField, fdValueField;

	public RedisInputDialog(Shell parent, Object in, TransMeta tr, String sname) {
		super(parent, (BaseStepMeta) in, tr, sname);
		input = (RedisInputMeta) in;
	}

	public String open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN
				| SWT.MAX);
		props.setLook(shell);
		setShellImage(shell, input);
		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				input.setChanged();
			}
		};
		changed = input.hasChanged();
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG,
				"RedisInputDialog.Shell.Title"));
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;
		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG,
				"RedisInputDialog.Stepname.Label"));
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
		// Hostname
		wlHostname = new Label(shell, SWT.RIGHT);
		wlHostname.setText(BaseMessages.getString(PKG,
				"RedisInputDialog.Hostname.Label"));
		props.setLook(wlHostname);
		fdlHostname = new FormData();
		fdlHostname.left = new FormAttachment(0, 0);
		fdlHostname.right = new FormAttachment(middle, -margin);
		fdlHostname.top = new FormAttachment(wStepname, margin);
		wlHostname.setLayoutData(fdlHostname);
		wHostname = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
				| SWT.BORDER);
		props.setLook(wHostname);
		wHostname.addModifyListener(lsMod);
		fdHostname = new FormData();
		fdHostname.left = new FormAttachment(middle, 0);
		fdHostname.top = new FormAttachment(wStepname, margin);
		fdHostname.right = new FormAttachment(100, 0);
		wHostname.setLayoutData(fdHostname);
		// Port
		wlPort = new Label(shell, SWT.RIGHT);
		wlPort.setText(BaseMessages.getString(PKG,
				"RedisInputDialog.Port.Label"));
		props.setLook(wlPort);
		fdlPort = new FormData();
		fdlPort.left = new FormAttachment(0, 0);
		fdlPort.right = new FormAttachment(middle, -margin);
		fdlPort.top = new FormAttachment(wHostname, margin);
		wlPort.setLayoutData(fdlPort);
		wPort = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
				| SWT.BORDER);
		props.setLook(wPort);
		wPort.addModifyListener(lsMod);
		fdPort = new FormData();
		fdPort.left = new FormAttachment(middle, 0);
		fdPort.top = new FormAttachment(wHostname, margin);
		fdPort.right = new FormAttachment(100, 0);
		wPort.setLayoutData(fdPort);
		// Base
		wlBase = new Label(shell, SWT.RIGHT);
		wlBase.setText(BaseMessages.getString(PKG,
				"RedisInputDialog.Base.Label"));
		props.setLook(wlBase);
		fdlBase = new FormData();
		fdlBase.left = new FormAttachment(0, 0);
		fdlBase.right = new FormAttachment(middle, -margin);
		fdlBase.top = new FormAttachment(wPort, margin);
		wlBase.setLayoutData(fdlBase);
		wBase = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
				| SWT.BORDER);
		props.setLook(wBase);
		wBase.addModifyListener(lsMod);
		fdBase = new FormData();
		fdBase.left = new FormAttachment(middle, 0);
		fdBase.top = new FormAttachment(wPort, margin);
		fdBase.right = new FormAttachment(100, 0);
		wBase.setLayoutData(fdBase);
		// Key
		wlKey = new Label(shell, SWT.RIGHT);
		wlKey.setText(BaseMessages.getString(PKG,
				"RedisInputDialog.Key.Label"));
		props.setLook(wlKey);
		fdlKey = new FormData();
		fdlKey.left = new FormAttachment(0, 0);
		fdlKey.right = new FormAttachment(middle, -margin);
		fdlKey.top = new FormAttachment(wBase, margin);
		wlKey.setLayoutData(fdlKey);
		wKey = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wKey);
		wKey.addModifyListener(lsMod);
		fdKey = new FormData();
		fdKey.left = new FormAttachment(middle, 0);
		fdKey.top = new FormAttachment(wBase, margin);
		fdKey.right = new FormAttachment(100, 0);
		wKey.setLayoutData(fdKey);
		// Key field check
		wlKeyFieldCheck = new Label(shell, SWT.RIGHT);
		wlKeyFieldCheck.setText(BaseMessages.getString(PKG,
				"RedisInputDialog.KeyFieldCheck.Label"));
		props.setLook(wlKeyFieldCheck);
		fdlKeyFieldCheck = new FormData();
		fdlKeyFieldCheck.left = new FormAttachment(0, 0);
		fdlKeyFieldCheck.right = new FormAttachment(middle, -margin);
		fdlKeyFieldCheck.top = new FormAttachment(wKey, margin);
		wlKeyFieldCheck.setLayoutData(fdlKeyFieldCheck);
		wKeyFieldCheck = new Button(shell, SWT.CHECK);
		props.setLook(wKeyFieldCheck);
		fdKeyFieldCheck = new FormData();
		fdKeyFieldCheck.left = new FormAttachment(middle, 0);
		fdKeyFieldCheck.top = new FormAttachment(wKey, margin);
		fdKeyFieldCheck.right = new FormAttachment(100, 0);
		wKeyFieldCheck.setLayoutData(fdKeyFieldCheck);
		SelectionAdapter lsxKeyFieldCheck = new SelectionAdapter() {
		      public void widgetSelected( SelectionEvent arg0 ) {
			    	if(wKeyFieldCheck.getSelection())
			    	{
			    		wKey.setEnabled(false);
			    		wKey.setText("");
			    		wKeyFieldCombo.setEnabled(true);
			    		if (input.getKeyFieldCombo()!=null)
			    			wKeyFieldCombo.setText(input.getKeyFieldCombo());
			    	}
			    	else
			    	{
			    		wKey.setEnabled(true);
			    		if(input.getKey()!=null)
			    			wKey.setText(input.getKey());
			    		wKeyFieldCombo.setEnabled(false);
			    		wKeyFieldCombo.setText("");
			    	}
			    	getFieldsInto(wKeyFieldCombo);
			    	input.setChanged();
		      }
		    };
		wKeyFieldCheck.addSelectionListener( lsxKeyFieldCheck );
		// Key field combo
		wlKeyFieldCombo = new Label(shell, SWT.RIGHT);
		wlKeyFieldCombo.setText(BaseMessages.getString(PKG,
				"RedisInputDialog.KeyField.Label"));
		props.setLook(wlKeyFieldCombo);
		fdlKeyFieldCombo = new FormData();
		fdlKeyFieldCombo.left = new FormAttachment(0, 0);
		fdlKeyFieldCombo.right = new FormAttachment(middle, -margin);
		fdlKeyFieldCombo.top = new FormAttachment(wKeyFieldCheck, margin);
		wlKeyFieldCombo.setLayoutData(fdlKeyFieldCombo);
		wKeyFieldCombo = new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
		props.setLook(wKeyFieldCombo);
		wKeyFieldCombo.addModifyListener(lsMod);
		fdKeyFieldCombo = new FormData();
		fdKeyFieldCombo.left = new FormAttachment(middle, 0);
		fdKeyFieldCombo.top = new FormAttachment(wKeyFieldCheck, margin);
		fdKeyFieldCombo.right = new FormAttachment(100, 0);
		wKeyFieldCombo.setLayoutData(fdKeyFieldCombo);
		wKeyFieldCombo.addFocusListener(new FocusListener() {
			public void focusLost(org.eclipse.swt.events.FocusEvent e) {
			}

			public void focusGained(org.eclipse.swt.events.FocusEvent e) {
				Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
				shell.setCursor(busy);
				getFieldsInto(wKeyFieldCombo);
				shell.setCursor(null);
				busy.dispose();
			}
		});
		// Value field
		wlValueField = new Label(shell, SWT.RIGHT);
		wlValueField.setText(BaseMessages.getString(PKG,
				"RedisInputDialog.ValueField.Label"));
		props.setLook(wlValueField);
		fdlValueField = new FormData();
		fdlValueField.left = new FormAttachment(0, 0);
		fdlValueField.right = new FormAttachment(middle, -margin);
		fdlValueField.top = new FormAttachment(wKeyFieldCombo, margin);
		wlValueField.setLayoutData(fdlValueField);
		wValueField = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wValueField);
		wValueField.addModifyListener(lsMod);
		fdValueField = new FormData();
		fdValueField.left = new FormAttachment(middle, 0);
		fdValueField.top = new FormAttachment(wKeyFieldCombo, margin);
		fdValueField.right = new FormAttachment(100, 0);
		wValueField.setLayoutData(fdValueField);

		// Some buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
		setButtonPositions(new Button[] { wOK, wCancel }, margin, wValueField);
		// Add listeners
		lsCancel = new Listener() {
			public void handleEvent(Event e) {
				cancel();
			}
		};
		lsOK = new Listener() {
			public void handleEvent(Event e) {
				ok();
			}
		};
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);
		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};
		wStepname.addSelectionListener(lsDef);
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				cancel();
			}
		});
		// Set the shell size, based upon previous time...
		setSize();
		fillData();
		input.setChanged(changed);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void fillData() {
		
		if (!Const.isEmpty(input.getHostname())) {
			wHostname.setText(input.getHostname());
		}
		
		if (!Const.isEmpty(input.getPort())) {
			wPort.setText(input.getPort());
		}
		
		if (!Const.isEmpty(input.getBase())) {
			wBase.setText(input.getBase());
		}
		
		if (!Const.isEmpty(input.getKey())) {
			wKey.setText(input.getKey());
		}

		wKeyFieldCheck.setSelection(input.getKeyFieldCheck());
		
		if (!Const.isEmpty(input.getKeyFieldCombo())) {
			wKeyFieldCombo.setText(input.getKeyFieldCombo());
		}
		
		if (!Const.isEmpty(input.getValueField())) {
			wValueField.setText(input.getValueField());
		}

		if(input.getKeyFieldCheck())
			wKey.setEnabled(false);
		else
			wKeyFieldCombo.setEnabled(false);
		
		wStepname.selectAll();
		wStepname.setFocus();
	}

	private void cancel() {
		stepname = null;
		input.setChanged(changed);
		dispose();
	}

	private void ok() {
		if (Const.isEmpty(wStepname.getText()))
			return;
		stepname = wStepname.getText(); // return value
		input.setHostname(wHostname.getText());
		input.setPort(wPort.getText());
		input.setBase(wBase.getText());
		input.setKey(wKey.getText());
		input.setKeyFieldCheck(wKeyFieldCheck.getSelection());
		input.setKeyFieldCombo(wKeyFieldCombo.getText());
		input.setValueField(wValueField.getText());
		dispose();
	}

	private void getFieldsInto(CCombo fieldCombo) {
		try {
			if (!gotPreviousFields) {
				previousFields = transMeta.getPrevStepFields(stepname);
			}
			String field = fieldCombo.getText();
			if (previousFields != null) {
				fieldCombo.setItems(previousFields.getFieldNames());
			}
			if (field != null)
				fieldCombo.setText(field);
			gotPreviousFields = true;
		} catch (KettleException ke) {
			new ErrorDialog(
					shell,
					BaseMessages.getString(PKG,
							"RedisInputDialog.FailedToGetFields.DialogTitle"),
					BaseMessages.getString(PKG,
							"RedisInputDialog.FailedToGetFields.DialogMessage"),
					ke);
		}
	}
}