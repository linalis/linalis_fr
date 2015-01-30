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

import java.util.List;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * The Redis Input step looks up value objects, from the given key names, from
 * memached server(s).
 *
 */
@Step(id = "RedisInput", image = "img/redis-input.png", name = "Redis Input", description = "Reads from a Redis instance", categoryDescription = "Input")
public class RedisInputMeta extends BaseStepMeta implements StepMetaInterface {
	private static Class<?> PKG = RedisInputMeta.class; // for i18n purposes,
														// needed by
														// Translator2!!
														// $NON-NLS-1$
	private String hostname;
	private String port;
	private String base;
	private String key;
	private boolean keyFieldCheck = false;
	private String keyFieldCombo;
	private String valueField;

	public RedisInputMeta() {
		super(); // allocate BaseStepMeta
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases,
			IMetaStore metaStore) throws KettleXMLException {
		readData(stepnode);
	}

	public Object clone() {
		RedisInputMeta retval = (RedisInputMeta) super.clone();
		retval.setHostname(this.hostname);
		retval.setPort(this.port);
		retval.setBase(this.base);
		retval.setKey(key);
		retval.setKeyFieldCheck(this.keyFieldCheck);
		retval.setKeyFieldCombo(this.keyFieldCombo);
		retval.setValueField(valueField);
		return retval;
	}

	public void setDefault() {
		this.hostname=null;
		this.port=null;
		this.base=null;
		this.key=null;
		this.keyFieldCheck=false;
		this.keyFieldCombo=null;
		this.valueField=null;
	}

	public void getFields(RowMetaInterface inputRowMeta, String origin,
			RowMetaInterface[] info, StepMeta nextStep, VariableSpace space,
			Repository repository, IMetaStore metaStore)
			throws KettleStepException {
		if (!Const.isEmpty(this.valueField)) {
			// Add value field meta if not found, else set it
			ValueMetaInterface v;
			try {
				v = ValueMetaFactory.createValueMeta(this.valueField, ValueMeta.TYPE_STRING);
			} catch (KettlePluginException e) {
				throw new KettleStepException(BaseMessages.getString(PKG,
						"RedisInputMeta.Exception.ValueTypeNameNotFound"), e);
			}
			v.setOrigin(origin);
			int valueFieldIndex = inputRowMeta
					.indexOfValue(this.valueField);
			if (valueFieldIndex < 0) {
				inputRowMeta.addValueMeta(v);
			} else {
				inputRowMeta.setValueMeta(valueFieldIndex, v);
			}
		} else {
			throw new KettleStepException(BaseMessages.getString(PKG,
					"RedisInputMeta.Exception.ValueFieldNameNotFound"));
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
			StepMeta stepMeta, RowMetaInterface prev, String input[],
			String output[], RowMetaInterface info, VariableSpace space,
			Repository repository, IMetaStore metaStore) {
		CheckResult cr;
		if (prev == null || prev.size() == 0) {
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING,
					BaseMessages.getString(PKG,
							"RedisInputMeta.CheckResult.NotReceivingFields"),
					stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK,
					BaseMessages.getString(PKG,
							"RedisInputMeta.CheckResult.StepRecevingData",
							prev.size() + ""), stepMeta);
			remarks.add(cr);
		}
		// See if we have input streams leading to this step!
		if (input.length > 0) {
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK,
					BaseMessages.getString(PKG,
							"RedisInputMeta.CheckResult.StepRecevingData2"),
					stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(
					CheckResultInterface.TYPE_RESULT_ERROR,
					BaseMessages
							.getString(PKG,
									"RedisInputMeta.CheckResult.NoInputReceivedFromOtherSteps"),
					stepMeta);
			remarks.add(cr);
		}
	}

	public StepInterface getStep(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int cnr, TransMeta tr,
			Trans trans) {
		return new RedisInput(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData() {
		return new RedisInputData();
	}

	public String getHostname()
	{
		return hostname;
	}
	
	public void setHostname(String hostname)
	{
		this.hostname=hostname;
	}
	
	public String getPort()
	{
		return port;
	}
	
	public void setPort(String port)
	{
		this.port=port;
	}
	
	public String getBase()
	{
		return base;
	}
	
	public void setBase(String base)
	{
		this.base=base;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public boolean getKeyFieldCheck() {
		return keyFieldCheck;
	}

	public void setKeyFieldCheck(boolean keyFieldCheck) {
		this.keyFieldCheck = keyFieldCheck;
	}
	
	public String getKeyFieldCombo() {
		return keyFieldCombo;
	}

	public void setKeyFieldCombo(String keyFieldCombo) {
		this.keyFieldCombo = keyFieldCombo;
	}

	public String getValueField() {
		return valueField;
	}

	public void setValueField(String valueField) {
		this.valueField = valueField;
	}
	

	@Override
	public String getXML() throws KettleException {
		StringBuffer retval = new StringBuffer();
		retval.append(XMLHandler.addTagValue("hostname", this.getHostname()));
		retval.append(XMLHandler.addTagValue("port", this.getPort()));
		retval.append(XMLHandler.addTagValue("base", this.getBase()));
		retval.append(XMLHandler.addTagValue("key", this.getKey()));
		retval.append(XMLHandler.addTagValue("keyFieldCheck", this.getKeyFieldCheck()==true?"true":"false"));
		retval.append(XMLHandler.addTagValue("keyFieldCombo", this.getKeyFieldCombo()));
		retval.append(XMLHandler.addTagValue("valueField", this.getValueField()));
		return retval.toString();
	}

	private void readData(Node stepnode) throws KettleXMLException {
		try {
			this.hostname = XMLHandler.getTagValue(stepnode, "hostname");
			this.port = XMLHandler.getTagValue(stepnode, "port");
			this.base = XMLHandler.getTagValue(stepnode, "base");
			this.key = XMLHandler.getTagValue(stepnode, "key");
			this.keyFieldCheck = (XMLHandler.getTagValue(stepnode, "keyFieldCheck")!=null && XMLHandler.getTagValue(stepnode, "keyFieldCheck").equals("true")?true:false);
			this.keyFieldCombo = XMLHandler.getTagValue(stepnode, "keyFieldCombo");
			this.valueField = XMLHandler.getTagValue(stepnode, "valueField");
		} catch (Exception e) {
			throw new KettleXMLException(BaseMessages.getString(PKG,
					"RedisInputMeta.Exception.UnableToReadStepInfo"), e);
		}
	}

	public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases) throws KettleException {
		try {
			this.hostname = rep.getStepAttributeString(id_step, "hostname");
			this.port = rep.getStepAttributeString(id_step, "port");
			this.base = rep.getStepAttributeString(id_step, "base");
			this.key = rep.getStepAttributeString(id_step, "key");
			this.keyFieldCheck = rep.getStepAttributeBoolean(id_step, "keyFieldCheck");
			this.keyFieldCombo = rep.getStepAttributeString(id_step, "keyFieldCombo");
			this.valueField = rep.getStepAttributeString(id_step, "valueField");
		} catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG,
					"RedisInputMeta.Exception.UnexpectedErrorReadingStepInfo"),
					e);
		}
	}

	public void saveRep(Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step)
			throws KettleException {
		try {
			rep.saveStepAttribute(id_transformation, id_step, "hostname", this.hostname);
			rep.saveStepAttribute(id_transformation, id_step, "port", this.port);
			rep.saveStepAttribute(id_transformation, id_step, "base", this.base);
			rep.saveStepAttribute(id_transformation, id_step, "key", this.key);
			rep.saveStepAttribute(id_transformation, id_step, "keyFieldCheck", this.keyFieldCheck);
			rep.saveStepAttribute(id_transformation, id_step, "keyFieldCombo", this.keyFieldCombo);
			rep.saveStepAttribute(id_transformation, id_step, "valuefield", this.valueField);
		} catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG,
					"RedisInputMeta.Exception.UnexpectedErrorSavingStepInfo"),
					e);
		}
	}
}