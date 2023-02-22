/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.asr.dictionary;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.topicquests.asr.dictionary.server.DictionaryServletHandler;

import java.io.IOException;

/**
 * @author jackpark
 *
 */
public class DictionaryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DictionaryServerEnvironment environment;
	private DictionaryServletHandler handler;
	
	/**
	 * 
	 */
	public DictionaryServlet(DictionaryServerEnvironment env) {
		environment = env;
		handler = new DictionaryServletHandler(environment);
		environment.logDebug("BootingServlet "+handler);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handler.executePost(request, response);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handler.executeGet(request, response);
	}

	public void destroy() {
		environment.shutDown();
	}

}
