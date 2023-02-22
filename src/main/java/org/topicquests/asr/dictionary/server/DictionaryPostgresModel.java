/**
 * 
 */
package org.topicquests.asr.dictionary.server;

import org.topicquests.asr.dictionary.DictionaryServerEnvironment;
import org.topicquests.asr.dictionary.server.api.IDictionaryServerModel;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;
import org.topicquests.asr.dictionary.server.api.IPostgresDictionary;
import org.topicquests.os.asr.api.IStatisticsClient;

import net.minidev.json.JSONObject;

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
		String x;
		if (clientIx.equals(clientId)) {
			if (verb.equals(IDictionaryServerModel.GET_TERM_ID) ||
				verb.equals(IDictionaryServerModel.ADD_TERM)) {
				jo = getTermId(request);
				environment.logDebug("DictionaryServerModel.handleNewRequest-1 "+jo);
			} else if (verb.equals(IDictionaryServerModel.GET_TERM)) {
				x = getTermById(request);
				jo.put(IDictionaryServerModel.CARGO, x);
			} else if (verb.equals(IDictionaryServerModel.GET_DICTIONARY)) {
				//WE DON'T DO THIS
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
		environment.logDebug("DictionaryServerModel.handleNewRequest++ "+result.getResultObject());;

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
		if (theWord.equals("\""))
			result = addTerm("\"");
		else
			result = addTerm(theWord);
		System.out.println("DictionaryServerModel.getWordId "+theWord+" "+result);
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
