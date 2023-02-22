/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.asr.dictionary;

import org.topicquests.os.asr.StatisticsHttpClient;
import org.topicquests.os.asr.api.IStatisticsClient;
import org.topicquests.pg.PostgresConnectionFactory;
import org.topicquests.asr.dictionary.server.DictionaryPostgresModel;
import org.topicquests.asr.dictionary.server.api.IDictionaryServerModel;
import org.topicquests.asr.dictionary.server.api.IPostgresDictionary;
import org.topicquests.support.RootEnvironment;

/**
 * @author jackpark
 *
 */
public class DictionaryServerEnvironment extends RootEnvironment {
	private boolean isShutDown = false;
	private PostgresConnectionFactory database = null;
	private IPostgresDictionary pgDictionary;
	private IDictionaryServerModel pgModel;

	private IStatisticsClient stats;
	/**
	 * 
	 */
	public DictionaryServerEnvironment() {
		super("config-props.xml", "logger.properties");
		stats = new StatisticsHttpClient(this);
		String schemaName = getStringProperty("DatabaseSchema");
		database = new PostgresConnectionFactory(getStringProperty("DatabaseName"),
                schemaName);
		isShutDown = false;
		pgDictionary = new PostgresDictionary(this);
		pgModel = new DictionaryPostgresModel(this, pgDictionary);
       // System.out.println("ENV-1 "+model);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			
			@Override
			public void run() {
				shutDown();
			}
		});

	}
	
	public IDictionaryServerModel getPostgresModel() {
		return pgModel;
	}
	public IPostgresDictionary getPostgresDictionary() {
		return pgDictionary;
	}
	
	public PostgresConnectionFactory getPostgresFactory() {
		return database;
	}
	
	/*public IPersistentDictionary getDictionary() {
		return dictionary;
	}*/
	
	public IStatisticsClient getStats() {
		return stats;
	}
	
	/*public IDictionaryServerModel getModel() {
		logDebug("GetModel");
		return model;
	}*/
		
	public void shutDown() {
		logDebug("DictionaryServerEnvironment.shutDown "+isShutDown);
		if (!isShutDown) {
			isShutDown = true;
			System.out.println("DictionaryServerEnvironment.shutDown");
			pgModel.shutDown();
			//if (model != null)
			//	model.shutDown();
			//stopper.shutDown();
		}
	}


}
