/**
 * 
 */
package devtests;

import org.topicquests.asr.dictionary.DictionaryServerEnvironment;
import org.topicquests.asr.dictionary.server.api.IPostgresDictionary;
import java.io.*;
import org.topicquests.support.util.TextFileHandler;
/**
 * @author jackpark
 *
 */
public class PGTest {
	private DictionaryServerEnvironment environment;
	private IPostgresDictionary pgDictionary;
	private TextFileHandler h;

	/**
	 * 
	 */
	public PGTest() {
		environment = new DictionaryServerEnvironment();
		pgDictionary = environment.getPostgresDictionary();
		h = new TextFileHandler();
		File dir = new File("data/en/");
		File [] files = dir.listFiles();
		for (File f: files)
			readFile(f);
		
		environment.shutDown();
		System.exit(0);
	}

	void readFile(File f) {
		if (f.getName().endsWith(".txt")) {
			System.out.println("Reading "+f);
			String line = h.readFirstLine(f);
			while (line != null) {
				if (!line.startsWith("#"))
					pgDictionary.addTermWord(line.trim());
				line = h.readNextLine();
			}
			
		}
	}
}
