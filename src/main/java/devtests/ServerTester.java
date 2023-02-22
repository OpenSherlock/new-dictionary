/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package devtests;


/**
 * @author jackpark
 *
 */
public class ServerTester {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//new BootTest(); // simple boot test
		//new Stringolizer(); // used to make some query strings
		//new DictionaryFillerTest(); // pumps 1 million words into it
		//new BigDictionaryGetTest(); // asks for a huge dictionary over http
		//new PersistentDictionaryTest(); Deprecated
		new PGTest();
	}

}
