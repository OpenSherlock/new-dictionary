/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package devtests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

/**
 * @author jackpark
 *
 */
public class BigDictionaryGetTest {
	private final String query =
			"http://localhost:7878/%7B+%22verb%22%3A%22getDictionary%22%2C+%22clientId%22%3A%22changeme%22++%7D";
	/**
	 * 
	 */
	public BigDictionaryGetTest() {
		long st = System.currentTimeMillis();
		IResult r = client();
		long delta = (System.currentTimeMillis() - st)/1000;
		System.out.println("A "+" | "+r.getErrorString()+" | "+delta);
		try {
			String json = (String)r.getResultObject();
			if (json != null) {
				System.out.println("B "+json.length());
				JSONParser p = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
				JSONObject jo = (JSONObject)p.parse(json);
				System.out.println("C "+jo.size());

			} 
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
//A  |  | 29 seconds to load a million words
//B 43337991
//C 4	
	IResult client() {
		IResult result = new ResultPojo();
		BufferedReader rd = null;
		HttpURLConnection con = null;

		try {
			URL urx = new URL(query);
			con = (HttpURLConnection) urx.openConnection();
			con.setReadTimeout(500000); //29 seconds for 1m words - leave lots of time
			con.setRequestMethod("GET");
			con.setDoInput(true);
			con.setDoOutput(true);
			con.connect();
			rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuilder buf = new StringBuilder();

			String line;
			while ((line = rd.readLine()) != null) {
				buf.append(line + '\n');
			}

			result.setResultObject(buf.toString());
		} catch (Exception var18) {
			var18.printStackTrace();
			result.addErrorString(var18.getMessage());
		} finally {
			try {
				if (rd != null) {
					rd.close();
				}

				if (con != null) {
					con.disconnect();
				}
			} catch (Exception var17) {
				var17.printStackTrace();
				result.addErrorString(var17.getMessage());
			}

		}
		return result;
	}

}
