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

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * The Redis Input step looks up value objects, from the given key names, from
 * redis server(s).
 *
 */
public class RedisInput extends BaseStep implements StepInterface {
	private static Class<?> PKG = RedisInputMeta.class; // for i18n purposes,
														// needed by
														// Translator2!!
														// $NON-NLS-1$
	static protected JedisPool jedisPool = null;
	static int nbInstance = 0;

	public RedisInput(StepMeta stepMeta, StepDataInterface stepDataInterface,
			int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	@Override
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		RedisInputMeta meta = (RedisInputMeta) smi;
		RedisInputData data = (RedisInputData) sdi;
		if (super.init(meta, data)) {
			try {
				// initializing pool
				// WE make sure that the port is an int before initializing the pool
				Integer.parseInt(environmentSubstitute(meta.getPort()));
				initPool(environmentSubstitute(meta.getHostname()), environmentSubstitute(meta.getPort()));
				return true;
			} catch (Exception e) {
				logError(BaseMessages.getString(PKG,
						"RedisInput.Error.ConnectError"), e);
				return false;
			}
		} else {
			return false;
		}
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {

		// Casting to step-specific implementation classes is safe
		RedisInputMeta meta = (RedisInputMeta) smi;
		RedisInputData data = (RedisInputData) sdi;
		
		nbInstance--;
		
		synchronized(JedisPool.class)
		{
			if(nbInstance==0 && jedisPool!=null)
			{
				jedisPool.destroy();
				jedisPool=null;
			}
		}
		
		super.dispose(meta, data);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		RedisInputMeta meta = (RedisInputMeta) smi;
		RedisInputData data = (RedisInputData) sdi;
		Object[] readRow = getRow(); 
		// If no more input to be expected, stop
		if (readRow == null) {
			if(first)
				data.noInputRow = true;
			else
			{
				setOutputDone();
				return false;
			}
		}
		if (first) {
			first = false;
			if(data.noInputRow)
			{
				data.outputRowMeta = new RowMeta();
				readRow = new Object[0];
			}
			// We have an input row
			else
			{
			// clone the input row structure and place it in our data object
				data.outputRowMeta = getInputRowMeta().clone();
			}
			// Get output field types
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this, repository, metaStore);
		}
		
		String key;
		// Get the correct key
		if(!meta.getKeyFieldCheck())
		{
			key = meta.getKey();
		}
		else
		// Getting the key from the combo box
		{
			int keyFieldIndex = getInputRowMeta().indexOfValue(
					meta.getKeyFieldCombo());
			if (keyFieldIndex < 0) {
				throw new KettleException(BaseMessages.getString(PKG,
						"RedisInputMeta.Exception.KeyFieldNameNotFound"));
			}
			key = getInputRowMeta().getString( readRow, keyFieldIndex );
		}
		
		Jedis jedisClient = jedisPool.getResource();
		Object fetchedValue;
		if(jedisClient!=null)
		{
			try
			{
				if(meta.getBase()!=null && !meta.getBase().equals(""))
					jedisClient.select(Integer.parseInt(environmentSubstitute(meta.getBase())));
				fetchedValue = jedisClient.get((String) (key));
				jedisPool.returnResource(jedisClient);
			}
			catch(Exception e)
			{
				jedisPool.returnResource(jedisClient);
				if (log.isError())
					logError("Base must be an integer or empty");
				return false;
			}
		}
		else
		{
			if (log.isError())
				logError("Error connecting to the redis server " + meta.getHostname() + ":" + meta.getPort());
			return false;
		}
		
		// Add Value data name to output, or set value data if already exists
		Object[] outputRowData = readRow;
		int valueFieldIndex;
		int inputSize;
		
		if(getInputRowMeta()==null)
		// Sets some variables if no input is provided
		{
			valueFieldIndex = -1;
			inputSize=0;
		}
		else
		{
			valueFieldIndex = getInputRowMeta().indexOfValue(meta.getValueField());
			inputSize=getInputRowMeta().size();
		}
		
		if (valueFieldIndex < 0 || valueFieldIndex > outputRowData.length) {
			// Not found so add it
			outputRowData = RowDataUtil.addValueData(readRow, inputSize, fetchedValue);
		} else {
			// Update value in place
			outputRowData[valueFieldIndex] = fetchedValue;
		}
		putRow(data.outputRowMeta, outputRowData); // copy row to possible
													// alternate rowset(s).
		if (checkFeedback(getLinesRead())) {
			if (log.isBasic())
				logBasic(BaseMessages.getString(PKG,
						"RedisInput.Log.LineNumber") + getLinesRead());
		}
		
		if(data.noInputRow)
		{
			setOutputDone();
			return false;
		}
		else
			return true;
	}
	
	synchronized private static void initPool(String host, String port)
	{
		int portInt;
		try
		{
			portInt = Integer.parseInt(port);
			if(jedisPool==null)
				jedisPool = new JedisPool(host, portInt);
			nbInstance++;
		}
		catch(Exception e)
		{
			jedisPool = null;
		}
	}
}