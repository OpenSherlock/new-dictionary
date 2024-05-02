/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.asr.dictionary.server;

import org.topicquests.asr.dictionary.DictionaryServerEnvironment;
import org.topicquests.asr.dictionary.server.api.IDictionaryServerModel;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;
import org.topicquests.asr.dictionary.server.api.IPostgresDictionary;
import org.topicquests.os.asr.api.IStatisticsClient;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONArray;

import net.minidev.json.parser.JSONParser;

/**
 * @author jackpark
 *
 */
public class DictionaryPostgresModel implements IDictionaryServerModel {
	private DictionaryServerEnvironment environment;
	private IPostgresDictionary dictionary;
	private IStatisticsClient stats;
	//We reserve 0 for the quote character, taken care of at the client
	private long nextNumber = 1;
	private long wordCount = 1;
	private final String clientId;
	private boolean isDirty = false;
	private final String 
		WORDS 		= "words",
		//an index of words, returning their id values
		IDS			= "ids",
		NUMBER		= "_DictionaryNumber",
		SIZE		= "size",
		WORD_COUNT	= "_DictionaryWordCount"; //,

	/**
	 * 
	 */
	public DictionaryPostgresModel(DictionaryServerEnvironment env, IPostgresDictionary d) {
		environment = env;
		dictionary = d;
		stats = environment.getStats();
		clientId = environment.getStringProperty("ClientId");
		environment.logDebug("BootingDictionary");
	}

	@Override
	public IResult handleRequest(JSONObject request) {
		IResult result = new ResultPojo();
		JSONObject jo = new JSONObject(); // empty default
		result.setResultObject(jo);
		environment.logDebug("DictionaryServerModel.handleNewRequest- "+request);
		String verb = request.getAsString(IDictionaryServerModel.VERB);
		String clientIx = request.getAsString(IDictionaryServerModel.CLIENT_ID);
		String x = null;
		if (clientIx.equals(clientId)) {
			if (verb.equals(IDictionaryServerModel.GET_TERM_ID) ||
				verb.equals(IDictionaryServerModel.ADD_TERM)) {
				jo = getTermId(request);
				environment.logDebug("DictionaryServerModel.handleNewRequest-1 "+jo);
			} else if (verb.equals(IDictionaryServerModel.GET_TERM)) {
				x = getTermById(request);
				if (x.startsWith(IDictionaryServerModel.ERROR))
					jo.put(IDictionaryServerModel.ERROR, x);
				else
					jo.put(IDictionaryServerModel.CARGO, x);
			} else if (verb.equals(IDictionaryServerModel.UPDATE)) {
				x = handleUpdate(request);
				jo.put(IDictionaryServerModel.CARGO, x);
			} else if (verb.equals(IDictionaryServerModel.SYNONYMS)) {
				x = handleSynonyms(request);
				jo.put(IDictionaryServerModel.CARGO, x);
			} else if (verb.equals(IDictionaryServerModel.GET_DICTIONARY)) {
				x = getDictionary();
				jo.put(IDictionaryServerModel.CARGO, x);
			} else if (verb.equals(IDictionaryServerModel.TEST))  {
				jo.put(IDictionaryServerModel.CARGO, "Yup");
			} else {
				jo.put(IDictionaryServerModel.ERROR, "BAD VERB: "+verb);
			}
		} else {
			jo.put(IDictionaryServerModel.ERROR, "Invalid Client");
		}
		environment.logDebug("DictionaryServerModel.handleNewRequest+ "+jo);
		result.setResultObject(jo);
		//environment.logDebug("DictionaryServerModel.handleNewRequest++ "+result.getResultObject());;

		return result;
	}
	
	String handleSynonyms(JSONObject jo) {
		String result = "OK"; //default
		String json = jo.getAsString(IDictionaryServerModel.CARGO);
		JSONParser p = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
		JSONArray a = null;
		try {
			a = (JSONArray)p.parse(json);
			result = dictionary.addSynonyms(a);
		} catch (Exception e) {
			environment.logError(e.getMessage(), e);
			return IDictionaryServerModel.ERROR+" "+e.getMessage();
		}

		return result;
	}
	String handleUpdate(JSONObject jo) {
		String result = "OK";
		String json = jo.getAsString(IDictionaryServerModel.CARGO);
		JSONParser p = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
		JSONArray a = null;
		try {
			a = (JSONArray)p.parse(json);
			System.out.println("DPM "+a.toJSONString());
			result = dictionary.update(a);
		} catch (Exception e) {
			environment.logError(e.getMessage(), e);
			return IDictionaryServerModel.ERROR+" "+e.getMessage();
		}

		return result;
	}
	String getDictionary() {
		String result = null;
		JSONObject jo = dictionary.getDictionary();
		if (jo != null) {
			result = jo.toJSONString();
			//result=result.replaceAll("\\\\", ""); // get rid of \" in values
		}
		return result;
	}
	
	String getTermById(JSONObject jo) {
		//theWord is wordId
			String id = jo.getAsString(IDictionaryServerModel.TERM);
			long ix = Long.parseLong(id);
			return dictionary.getTermById(ix);
	}
	JSONObject getTermId(JSONObject jo) {
		String theWord = jo.getAsString(IDictionaryServerModel.TERM);
		JSONObject result = null;
		if (theWord != null && theWord.equals("\""))
			result = addTerm("\"");
		else
			result = addTerm(theWord);
		System.out.println("DictionaryServerModel.getWordId "+theWord+" "+result);
		return result;
	}
	
	JSONObject addSynonym(long masterId, long synId) {
		environment.logDebug("DictServerModel.addSyn- "+masterId+" "+synId);
		JSONObject result = new JSONObject();
		boolean t = dictionary.addSynonym(masterId, synId);
		
		return result;
	}
	
	JSONObject addSynonymW(long masterId, String syn) {
		environment.logDebug("DictServerModel.addSynW- "+masterId+" "+syn);
		JSONObject result = new JSONObject();
		boolean t = false;
		long r = dictionary.getTermId(syn);
		if (r > -1)
			t = dictionary.addSynonym(masterId, r);
		
		return result;
	}
	/////////////////////////
	// given Id, get the whole word
	// given word, find Id with lowercase word
	/////////////////////////
	/**
	* Serves two different verbs:
	* <ul><li>GetWordId: if it tries to get the idea on a particular word
	*  which does not exist, it adds the word and returns the id</li>
	*  <li>AddWord: simply adds the word and returns its id</li>
	* <p>Returns { wordId, boolean }<br/>
	* boolean is true if this is a new word</p>
	* @param term
	* @return
	*/
	private JSONObject addTerm(String term) {
		environment.logDebug("DictServerModel.addWord- "+term);
		JSONObject result = new JSONObject();
		long wID = -1;
		String theWord = term.toLowerCase();
		if (theWord.equals("\""))
			wID = 0; // reserved
		else
			wID =  dictionary.getTermId(term);
		environment.logDebug("DictServerModel.addWord-1 "+wID);
		if (wID > -1)	 {
			result.put(IDictionaryServerModel.CARGO, wID);
			result.put(IDictionaryServerModel.IS_NEW_TERM, false);
		} else {
			wID = dictionary.addTermWord(term);
			result.put(IDictionaryServerModel.CARGO, wID);
			result.put(IDictionaryServerModel.IS_NEW_TERM, true);
			environment.logDebug("DictServerModel.addWord-2 "+result);
			isDirty = true;
		}
		return result;
	}


	@Override
	public void shutDown() {
		dictionary.shutDown();
	}

}
