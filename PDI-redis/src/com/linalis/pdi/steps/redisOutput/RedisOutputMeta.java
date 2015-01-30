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
package com.linalis.pdi.steps.redisOutput;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
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
 * The Redis Output step writes value objects, for the given key names, to
 * Redis server(s).
 *
 */
@Step(id = "RedisOutput", image = "img/redis-input.png", name = "Redis Output", description = "Writes to a Redis instance", categoryDescription = "Output")
public class RedisOutputMeta extends BaseStepMeta implements
		StepMetaInterface {
	private static Class<?> PKG = RedisOutputMeta.class; // for i18n
																// purposes,
																// needed by
																// Translator2!!
																// $NON-NLS-1$
	private String hostname;
	private String port;
	private String base;
	private String pipelineSize  ="1";
	private String key;
	private boolean keyFieldCheck = false;
	private String keyFieldCombo;
	private String value;
	private boolean valueFieldCheck = false;
	private String valueFieldCombo;

	public RedisOutputMeta() {
		super(); // allocate BaseStepMeta
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases,
			IMetaStore metaStore) throws KettleXMLException {
		readData(stepnode);
	}

	public Object clone() {
		RedisOutputMeta retval = (RedisOutputMeta) super.clone();
		retval.setHostname(this.hostname);
		retval.setPort(this.port);
		retval.setBase(this.base);
		retval.setKey(this.key);
		retval.setKeyFieldCheck(this.keyFieldCheck);
		retval.setKeyFieldCombo(this.keyFieldCombo);
		retval.setValue(this.value);
		retval.setValueFieldCheck(this.valueFieldCheck);
		retval.setValueFieldCombo(this.valueFieldCombo);
		return retval;
	}

	public void setDefault() {
		this.hostname = null;
		this.port = null;
		this.base = null;
		this.pipelineSize = "1";
		this.key = null;
		this.keyFieldCheck = false;
		this.keyFieldCombo = null;
		this.value = null;
		this.valueFieldCheck = false;
		this.valueFieldCombo = null;
		
	}

	public void getFields(RowMetaInterface inputRowMeta, String origin,
			RowMetaInterface[] info, StepMeta nextStep, VariableSpace space,
			Repository repository, IMetaStore metaStore)
			throws KettleStepException {
		super.getFields(inputRowMeta, origin, info, nextStep, space,
				repository, metaStore);
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
			StepMeta stepMeta, RowMetaInterface prev, String input[],
			String output[], RowMetaInterface info, VariableSpace space,
			Repository repository, IMetaStore metaStore) {
		CheckResult cr;
		if (prev == null || prev.size() == 0) {
			cr = new CheckResult(
					CheckResultInterface.TYPE_RESULT_WARNING,
					BaseMessages
							.getString(PKG,
									"MemcachedOutputMeta.CheckResult.NotReceivingFields"),
					stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK,
					BaseMessages.getString(PKG,
							"MemcachedOutputMeta.CheckResult.StepRecevingData",
							prev.size() + ""), stepMeta);
			remarks.add(cr);
		}
		// See if we have input streams leading to this step!
		if (input.length > 0) {
			cr = new CheckResult(
					CheckResultInterface.TYPE_RESULT_OK,
					BaseMessages
							.getString(PKG,
									"MemcachedOutputMeta.CheckResult.StepRecevingData2"),
					stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(
					CheckResultInterface.TYPE_RESULT_ERROR,
					BaseMessages
							.getString(PKG,
									"MemcachedOutputMeta.CheckResult.NoInputReceivedFromOtherSteps"),
					stepMeta);
			remarks.add(cr);
		}
	}

	public StepInterface getStep(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int cnr, TransMeta tr,
			Trans trans) {
		return new RedisOutput(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData() {
		return new RedisOutputData();
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

	public String getPipelineSize()
	{
		return pipelineSize;
	}
	
	public void setPipelineSize(String pipelineSize)
	{
		this.pipelineSize=pipelineSize;
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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public boolean getValueFieldCheck() {
		return valueFieldCheck;
	}

	public void setValueFieldCheck(boolean valueFieldCheck) {
		this.valueFieldCheck = valueFieldCheck;
	}
	
	public String getValueFieldCombo() {
		return valueFieldCombo;
	}

	public void setValueFieldCombo(String valueFieldCombo) {
		this.valueFieldCombo = valueFieldCombo;
	}


	@Override
	public String getXML() throws KettleException {
		StringBuffer retval = new StringBuffer();
		retval.append(XMLHandler.addTagValue("hostname", this.getHostname()));
		retval.append(XMLHandler.addTagValue("port", this.getPort()));
		retval.append(XMLHandler.addTagValue("base", this.getBase()));
		retval.append(XMLHandler.addTagValue("pipelineSize", this.getPipelineSize()));
		retval.append(XMLHandler.addTagValue("key", this.getKey()));
		retval.append(XMLHandler.addTagValue("keyFieldCheck", this.getKeyFieldCheck()==true?"true":"false"));
		retval.append(XMLHandler.addTagValue("keyFieldCombo", this.getKeyFieldCombo()));
		retval.append(XMLHandler.addTagValue("value", this.getValue()));
		retval.append(XMLHandler.addTagValue("valueFieldCheck", this.getValueFieldCheck()==true?"true":"false"));
		retval.append(XMLHandler.addTagValue("valueFieldCombo", this.getValueFieldCombo()));
		return retval.toString();
	}

	private void readData(Node stepnode) throws KettleXMLException {
		try {
			this.hostname = XMLHandler.getTagValue(stepnode, "hostname");
			this.port = XMLHandler.getTagValue(stepnode, "port");
			this.base = XMLHandler.getTagValue(stepnode, "base");
			this.pipelineSize = XMLHandler.getTagValue(stepnode, "pipelineSize");
			this.key = XMLHandler.getTagValue(stepnode, "key");
			this.keyFieldCheck = (XMLHandler.getTagValue(stepnode, "keyFieldCheck")!=null && XMLHandler.getTagValue(stepnode, "keyFieldCheck").equals("true")?true:false);
			this.keyFieldCombo = XMLHandler.getTagValue(stepnode, "keyFieldCombo");
			this.value = XMLHandler.getTagValue(stepnode, "value");
			this.valueFieldCheck = (XMLHandler.getTagValue(stepnode, "valueFieldCheck")!=null && XMLHandler.getTagValue(stepnode, "valueFieldCheck").equals("true")?true:false);
			this.valueFieldCombo = XMLHandler.getTagValue(stepnode, "valueFieldCombo");
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
			this.pipelineSize = rep.getStepAttributeString(id_step, "pipelineSize");
			this.key = rep.getStepAttributeString(id_step, "key");
			this.keyFieldCheck = rep.getStepAttributeBoolean(id_step, "keyFieldCheck");
			this.keyFieldCombo = rep.getStepAttributeString(id_step, "keyFieldCombo");
			this.value = rep.getStepAttributeString(id_step, "value");
			this.valueFieldCheck = rep.getStepAttributeBoolean(id_step, "valueFieldCheck");
			this.valueFieldCombo = rep.getStepAttributeString(id_step, "valueFieldCombo");
		} catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG,
					"RedisInputMeta.Exception.UnexpectedErrorReadingStepInfo"),
					e);
		}
	}

	public void saveRep(Repository rep, IMetaStore metaStore,
			ObjectId id_transformation, ObjectId id_step)
			throws KettleException {
		try {
			rep.saveStepAttribute(id_transformation, id_step, "hostname", this.hostname);
			rep.saveStepAttribute(id_transformation, id_step, "port", this.port);
			rep.saveStepAttribute(id_transformation, id_step, "base", this.base);
			rep.saveStepAttribute(id_transformation, id_step, "pipelineSize", this.pipelineSize);
			rep.saveStepAttribute(id_transformation, id_step, "key", this.key);
			rep.saveStepAttribute(id_transformation, id_step, "keyFieldCheck", this.keyFieldCheck);
			rep.saveStepAttribute(id_transformation, id_step, "keyFieldCombo", this.keyFieldCombo);
			rep.saveStepAttribute(id_transformation, id_step, "value", this.value);
			rep.saveStepAttribute(id_transformation, id_step, "valueFieldCheck", this.valueFieldCheck);
			rep.saveStepAttribute(id_transformation, id_step, "valueFieldCombo", this.valueFieldCombo);
		} catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG,
					"RedisInputMeta.Exception.UnexpectedErrorSavingStepInfo"),
					e);
		}
	}
}