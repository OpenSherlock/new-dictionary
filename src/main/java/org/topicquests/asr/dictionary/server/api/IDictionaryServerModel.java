/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.asr.dictionary.server.api;


import org.topicquests.support.api.IResult;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 *
 */
public interface IDictionaryServerModel {
	// JSON Keys
	public static final String
		VERB			= "verb",
		CLIENT_ID		= "clientId",
		TERM			= "term",
		GET_TERM		= "getWord",
		IS_NEW_TERM		= "isNewTerm",	// boolean <code>true</code> if is new word
		TEST			= "test",
		ERROR			= "error",
		CARGO			= "cargo"; //return object - wordId or word
	
	// Verbs
	public static final String
		GET_TERM_ID		= "getTermId",
		ADD_TERM		= "addTerm",
		ADD_SYNONYM		= "addSyn", // long, long
		ADD_SYNONYM_W	= "addSynW",// long, word
		LIST_SYNONYMS	= "listSyns",
		GET_DICTIONARY	= "getDictionary";

	/**
	 * A request takes one form:<br>
	 * {verb:"add", word:<word>, clientId:<clientId>}<br/>
	 * It responds with {resp:"ok", value:<value>, isNewWord:<true/false>}<br/>
	 * @param request
	 * @return
	 */
	IResult handleRequest(JSONObject request);

	
	void shutDown();
}
