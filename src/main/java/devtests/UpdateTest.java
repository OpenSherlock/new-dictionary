/**
 * 
 */
package devtests;

import org.topicquests.support.api.IResult;

import net.minidev.json.JSONArray;

/**
 * 
 */
public class UpdateTest {
	private final String URL = "http://localhost:7878/";
	private StringBuilder buf;

	/**
	 * 
	 */
	public UpdateTest() {
		buf = new StringBuilder();
		String query, word;
		IResult r;
		String x;
		long st = System.currentTimeMillis();
		JSONArray ja = new JSONArray();
		ja.add("foo");
		ja.add("bar");
		ja.add("blah");
		try {
					buf.append("{\"verb\":\"update\",\"cargo\":");
					buf.append(ja.toJSONString());
					buf.append(",\"clientId\":\"changeme\"}");
					System.out.println("SENDING:\n"+buf.toString());
					//query = URLEncoder.encode(buf.toString(), "UTF-8");
					x = GetQuery.getQuery(buf.toString(), URL);
					System.out.println("GOT "+x);
					buf = new StringBuilder();
		} catch (Exception e) {
			e.printStackTrace();
		}
		long delta = (System.currentTimeMillis() - st)/1000;
		System.out.println("Did "+delta);
		System.exit(0);	}

}
