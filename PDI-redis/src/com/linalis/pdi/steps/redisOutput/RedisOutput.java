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

import java.util.ArrayList;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

/**
 * The Redis Output step stores value objects, for the given key names, to
 * memached server(s).
 *
 */
public class RedisOutput extends BaseStep implements StepInterface {
	private static Class<?> PKG = RedisOutputMeta.class; // for i18n
																// purposes,
																// needed by
																// Translator2!!
																// $NON-NLS-1$
	
	static protected JedisPool jedisPool = null;
	static int nbInstance = 0;
	private ArrayList<String[]> redisCache = null;
	private boolean useCache = false;
	private int cacheSize;

	public RedisOutput(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int copyNr,
			TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	@Override
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		// Casting to step-specific implementation classes is safe
		RedisOutputMeta meta = (RedisOutputMeta) smi;
		RedisOutputData data = (RedisOutputData) sdi;
		if (super.init(meta, data)) {
			try {
				// initializing pool
				Integer.parseInt(environmentSubstitute(meta.getPort()));
				initPool(environmentSubstitute(meta.getHostname()), environmentSubstitute(meta.getPort()));
				
				//Initializing the cache, if needed
				if(!meta.getPipelineSize().equals("1"))
				{
					
					redisCache = new ArrayList<String[]>();
					useCache = true;
					cacheSize = Integer.parseInt(meta.getPipelineSize());
				}
				return true;
			} catch (Exception e) {
				logError(BaseMessages.getString(PKG,
						"RedisOutput.Error.ConnectError"), e);
				return false;
			}
		} else {
			return false;
		}
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {

		// Casting to step-specific implementation classes is safe
		RedisOutputMeta meta = (RedisOutputMeta) smi;
		RedisOutputData data = (RedisOutputData) sdi;
		
		nbInstance--;
		
		synchronized(JedisPool.class)
		{
			if(nbInstance==0 && jedisPool!=null)
			{
				jedisPool.destroy();
				jedisPool=null;
			}
		}
		
		redisCache = null;
		useCache = false;
		
		super.dispose(meta, data);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		RedisOutputMeta meta = (RedisOutputMeta) smi;
		RedisOutputData data = (RedisOutputData) sdi;
		Object[] readRow = getRow(); // get row, set busy!
		// If no more input to be expected, stop
		if (readRow == null) {
			if(first)
				data.noInputRow = true;
			else
			{
				// Emptying the cache into redisServer
				if(useCache && redisCache.size()!=0)
				{
					if(!sendCacheToRedis())
					{
						if (log.isError())
							logError("Error connecting to the redis server " + meta.getHostname() + ":" + meta.getPort());
						return false;
					}
				}
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
		
		String value;
		// Get the correct key
		if(!meta.getValueFieldCheck())
		{
			value = meta.getValue();
		}
		else
		// Getting the key from the combo box
		{
			int valueFieldIndex = getInputRowMeta().indexOfValue(
					meta.getValueFieldCombo());
			if (valueFieldIndex < 0) {
				throw new KettleException(BaseMessages.getString(PKG, "RedisInputMeta.Exception.KeyFieldNameNotFound"));
			}
			value = getInputRowMeta().getString( readRow, valueFieldIndex );
		}
		
		if(useCache)
		{
			String keyAndValue[] = new String[2];
			keyAndValue[0] = key;
			keyAndValue[1] = value;
			redisCache.add(keyAndValue);
			
			if(redisCache.size()==cacheSize)
			// Cache is full, le'ts load it into redis
			{
				if(!sendCacheToRedis())
				{
					if (log.isError())
						logError("Error connecting to the redis server " + meta.getHostname() + ":" + meta.getPort());
					return false;
				}
			}
		}
		else
		{
			Jedis jedisClient = jedisPool.getResource();
			if(jedisClient!=null)
			{
				try
				{
					if(meta.getBase()!=null && !meta.getBase().equals(""))
						jedisClient.select(Integer.parseInt(environmentSubstitute(meta.getBase())));
					jedisClient.set(key, value);
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
		}
		
		// No value added to the output line
		Object[] outputRowData = readRow;
		putRow(data.outputRowMeta, outputRowData); // copy row to possible
													// alternate rowset(s).
		if (checkFeedback(getLinesRead())) {
			if (log.isBasic())
				logBasic(BaseMessages.getString(PKG,
						"RedisOutput.Log.LineNumber") + getLinesRead());
		}
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
	
	private boolean sendCacheToRedis()
	{
		Jedis jedisClient = jedisPool.getResource();
		if(jedisClient!=null)
		{
			Pipeline redisPipeline = jedisClient.pipelined();
			for(String[] keyValue : redisCache)
			{
				redisPipeline.set(keyValue[0], keyValue[1]);
			}
			redisPipeline.sync();
			jedisPool.returnResource(jedisClient);
			redisCache.clear();
			return true;
		}
		else
		{
			return false;
		}
	}

}
