package com.linalis.importio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;

import com.importio.api.clientlite.MessageCallback;
import com.importio.api.clientlite.data.Progress;
import com.importio.api.clientlite.data.Query;
import com.importio.api.clientlite.data.QueryMessage;
import com.importio.api.clientlite.data.QueryMessage.MessageType;

/**
 * Class used for callback calls when import.io has to retrieve import.io data
 * 
 * @author nhaquet
 * @see MessageCallback
 */
public class MyMessageCallback implements MessageCallback {

	/**
	 *	The PKG member is used when looking up internationalized strings.
	 *	The properties file with localized keys is expected to reside in 
	 *	{the package of the class specified}/messages/messages_{locale}.properties   
	 */
	protected static Class<?> PKG = MyMessageCallback.class;
	
	/**
	 * pentaho logger
	 */
	protected LogChannelInterface log;

	/**
	 * The @seeCountDownLatch used to wait for data retrieval to be finished
	 */
	protected CountDownLatch latch;
	
	/**
	 * Tells if we have been disconnected from import.io
	 */
	protected boolean disconnected=false;
	
    /**
     *  the data rows received from import io
     */
    protected List<Object> dataRows;
    
    /**
     * Tells if we have timed out from import.io
     */
    protected boolean hasTimedOut = false; 

    /**
     * Constructor for MyMessageCallback
     * 
     * @param latch the latch to be used
     */
	public MyMessageCallback(CountDownLatch latch) {
		this.latch = latch;
	}
	
	/**
	 * Method which processes messages from import.io
	 * When data is received, it is stored in dataRows
	 * When all data is received, countdown is called on the latch to signal that no more messages are expected
	 * @see CountDownLatch
	 */
	public void onMessage(Query query, QueryMessage message, Progress progress){
		if (message.getType() == MessageType.MESSAGE)
	    {
	      if(dataRows==null)
	    	  dataRows = new ArrayList<Object>();
	      HashMap<String, Object> resultMessage = (HashMap<String, Object>) message.getData();
	      if (resultMessage.containsKey("errorType"))
	      {
	        // In this case, we received a message, but it was an error from the external service
	        log.logError(BaseMessages.getString(PKG, "ImportIO.ErrorMessage"));
	        log.logError(message.toString());
	      }
	      else if(message.getType() == MessageType.DISCONNECT)
	      {
	    	  disconnected = true;
	      }
	      else
	      {
	        // We got a message and it was not an error, so we can process the data
	        // Save the data we got in our dataRows variable for later
	        List<Object> results = (List<Object>) resultMessage.get("results");
	        dataRows.addAll(results);
	      }
	    }
	    // When the query is finished, countdown the latch so the program can continue when everything is done
	    if ( progress.isFinished() )
	    {
	      latch.countDown();
	    }
	}
	
	/**
	 * Returns the latch
	 * @return the latch
	 */
	public CountDownLatch getMyLatch() {
		return latch;
	}

	/**
	 * Sets the latch
	 * @param myLatch the latch to set
	 */
	public void setMyLatch(CountDownLatch myLatch) {
		this.latch = myLatch;
	}

	/**
	 * Returns the disconnected flag
	 * @return the disconnected flag
	 */
	public boolean isDisconnected() {
		return disconnected;
	}

	/**
	 * Sets  the disconnected flag
	 * @param disconnected  the disconnected flag
	 */
	public void setDisconnected(boolean disconnected) {
		this.disconnected = disconnected;
	}
	
	/**
	 * Returns the data rows
	 * @return the dataRows object
	 */
	public List<Object> getDataRows() {
		return dataRows;
	}

	/**
	 * Sets the data rows
	 * @param dataRows the data rows object to set
	 */
	public void setDataRows(List<Object> dataRows) {
		this.dataRows = dataRows;
	}
	
	/**
	 * Adds some data rows to the dataRows
	 * @param someData the data rows to add
	 */
	public void addDataRows(List<Object> someData){
		dataRows.addAll(someData);
	}
	
	/**
	 * Returns the timed out boolean
	 * @return the timed out boolean
	 */
	public boolean hasTimedOut() {
		return hasTimedOut;
	}

	/**
	 * Sets the timed out boolean
	 * @param hasTimedOut the timed out boolean
	 */
	public void setHasTimedOut(boolean hasTimedOut) {
		this.hasTimedOut = hasTimedOut;
	}
	
	/**
	 * Sets the pentaho logger
	 * @param log
	 */
	public void setLog(LogChannelInterface log) {
		this.log = log;
	}
}
