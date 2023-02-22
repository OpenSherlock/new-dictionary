/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package devtests;

import org.topicquests.asr.dictionary.DictionaryServerEnvironment;

/**
 * @author jackpark
 *
 */
public class BootTest {
	private DictionaryServerEnvironment environment;

	/**
	 * 
	 */
	public BootTest() {
		environment = new DictionaryServerEnvironment();
		System.out.println("AAA "+environment.getProperties());
		environment.shutDown();
		System.exit(0);
	}

}
