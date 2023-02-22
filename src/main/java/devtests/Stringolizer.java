/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package devtests;

import java.net.URLEncoder;

/**
 * @author jackpark
 *
 */
public class Stringolizer {

	/**
	 * 
	 */
	public Stringolizer() {
		String request = "{ \"verb\":\"getDictionary\", \"clientId\":\"changeme\"  }";
		try {
			request = URLEncoder.encode(request, "UTF-8");
			System.out.println("GetDictionary "+request);
			request = "{ \"verb\":\"addWord\", \"word\":\"foo\", \"clientId\":\"changeme\"  }";
			request = URLEncoder.encode(request, "UTF-8");
			System.out.println("ADD1 "+request);
			request = "{ \"verb\":\"addWord\", \"word\":\"bar\", \"clientId\":\"changeme\"  }";
			request = URLEncoder.encode(request, "UTF-8");
			System.out.println("ADD2 "+request);
			request = "{ \"verb\":\"getWordId\", \"word\":\"foo\", \"clientId\":\"changeme\"  }";
			request = URLEncoder.encode(request, "UTF-8");
			System.out.println("GET1 "+request);
			request = "{ \"verb\":\"getWord\", \"word\":\"2\", \"clientId\":\"changeme\"  }";
			request = URLEncoder.encode(request, "UTF-8");
			System.out.println("GET2 "+request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
				
		System.exit(0);
	}

}
