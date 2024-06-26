/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.asr.dictionary.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.topicquests.asr.dictionary.DictionaryServerEnvironment;
import org.topicquests.asr.dictionary.server.api.IDictionaryServerModel;
import org.topicquests.support.api.IResult;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

/**
 * @author jackpark
 *
 */
public class DictionaryServletHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;
	/**
	 * 200
	 */
	public static final int RESPONSE_OK = HttpServletResponse.SC_OK;
	/**
	 * 400
	 */
	public static final int RESPONSE_BAD = HttpServletResponse.SC_BAD_REQUEST;
	/**
	 * 401
	 */
	public static final int RESPONSE_UNAUTHORIZED = HttpServletResponse.SC_UNAUTHORIZED;
	/**
	 * 403
	 */
	public static final int RESPONSE_FORBIDDEN = HttpServletResponse.SC_FORBIDDEN;
	/**
	 * 404
	 */
	public static final int RESPONSE_NOT_FOUND = HttpServletResponse.SC_NOT_FOUND;
	/**
	 * 407
	 */
	public static final int RESPONSE_AUTHENTICATION_REQUIRED = HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED;
	/**
	 * 500
	 */
	public static final int RESPONSE_INTERNAL_SERVER_ERROR = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
	
	private DictionaryServerEnvironment environment;
	private IDictionaryServerModel model = null;

	public DictionaryServletHandler(DictionaryServerEnvironment env) {
		environment = env;
		model = environment.getPostgresModel();
		environment.logDebug("BootingServletHandler "+model);
	}
	
	public void executeGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject x = processRequest(request);
		environment.logDebug("DSH-1 "+x);
		IResult r = model.handleRequest(x);
		JSONObject jo = (JSONObject)r.getResultObject();
		//Two cases:
		//environment.logDebug("DictServer.executeGet "+jo);
		if (jo == null) {
			JSONObject msg = new JSONObject();
			if (r.hasError()) {
				msg.put("msg", r.getErrorString());
				sendJSON(msg.toJSONString(), RESPONSE_BAD, response);
			} else {
				msg.put("msg", "ok");
				sendJSON(msg.toJSONString(), RESPONSE_OK, response);
			}
			
		} else {
			sendJSON(jo.toJSONString(), RESPONSE_OK, response);
		} 
	}

	public void executePost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		executeGet(request, response);
	}
	
	void sendJSON(String json, int statusCode, HttpServletResponse response) throws ServletException {
    	System.out.println("SENDJSON "+statusCode+" "+json);
    	try {
	    	response.setContentType("application/json; charset=UTF-8");
	    	response.setStatus(statusCode);
	        PrintWriter out = response.getWriter();
	    	out.write(json);
	    	out.close();
    	} catch (Exception e) {
    		throw new ServletException(e);
    	}
    }
	
	JSONObject processRequest(HttpServletRequest request) throws ServletException {
		JSONObject result = null;
		String pt;
		try {
			InputStream is = request.getInputStream();
			InputStreamReader rdr = new InputStreamReader(is, "UTF-8");
			BufferedReader br = new BufferedReader(rdr);
			StringBuilder buf = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null) {
				buf.append(line);
			}
			pt = buf.toString();
			System.out.println("BOO "+pt);
		} catch (Exception e) {
			environment.logError(e.getMessage(), e);
			throw new ServletException(e);
		}
			
		
		//String pt = getPath(request);
		environment.logDebug("PROCESSREQUEST "+pt);
		//PROCESSREQUEST http://localhost:7878/{"verb":"getDictionary","clientId":"changeme"}
		// edge case {"verb":"addWord","word":""","clientId":"changeme"}
		int where;
		if (!pt.startsWith("{")) {
			where = pt.lastIndexOf('/');
			if (where > -1)  {
				pt = pt.substring(where+1);
			}
		}
		where = pt.indexOf("\"\"\"");
		environment.logDebug("ProcessingRequest-1 "+where);
		if (where > -1) {
			StringBuilder buf = new StringBuilder();
			buf.append(pt.substring(0, where));
			buf.append("\"\\\"\"");
			buf.append(pt.substring(where+3));
			pt = buf.toString();
			environment.logDebug("ProcessingRequest-2 "+where);

		}

		environment.logDebug("PROCESSREQUEST+ "+pt);
		result = jsonFromString(pt);
		
		return result;
	}
	//{"verb":"update","cargo":"["foo","bar","blah"]","clientId":"changeme"}

	JSONObject jsonFromString(String jsonString) throws ServletException {
		environment.logDebug("JSONFROMSTRING "+jsonString);
		//NOTE: there are edge conditions:
		//  jsonString == ""  can happen
		JSONParser p = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
		try {
			return (JSONObject)p.parse(jsonString);
		} catch (Exception e) {
			environment.logError(jsonString, e);
			throw new ServletException(e);
		}
	}
	String getPath(HttpServletRequest request) throws ServletException {
    	String path = notNullString(request.getPathInfo()).trim();
    	Enumeration<String> ex = request.getParameterNames();
    	if (ex != null) {
    		List<String>l = new ArrayList<String>();
    		while (ex.hasMoreElements())
    			l.add(ex.nextElement());
    	}
    	
    	try {
    		InputStream ins = request.getInputStream();
    		if (ins != null) {
    			StringBuilder buf = new StringBuilder();
    			int c;
    			while ((c = ins.read()) > -1)
    				buf.append((char)c);
        		System.out.println("FOO "+buf.toString());
    			//http://localhost:7878/%7B%22verb%22%3A%22getDictionary%22%2C%22clientId%22%3A%22changeme%22%7D
        		path = buf.toString();
    		}
    	} catch (Exception x) {
    		environment.logError("DictionaryServletHandler.getPath booboo "+x.getMessage(), x);
    	}
    	if (path.startsWith("/"))
    		path = path.substring(1);
    	if (path.endsWith("/"))
    		path = path.substring(0,path.length()-1);
    	try {
    		path = URLDecoder.decode(path, "UTF8");
    	} catch (Exception e) {
    		throw new ServletException(e);
    	}
    	if (path != null && path.startsWith("/"))
    		path = path.substring(1);
    	System.out.println(path);
    	return path;
    }
	
	String notNullString(String in) {
    	if (in == null) return "";
    	return in;
    }
	
	public void shutDown() throws Exception {
		System.out.println("DictionaryServletHandler.shutDown");
	}
}
