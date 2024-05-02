/**
 * 
 */
package devtests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.tinylog.Logger;

/**
 * 
 */
public class GetQuery {


	public static String getQuery(String query, String SERVER_URL) {
		StringBuilder buf = new StringBuilder();

		BufferedReader rd = null;
		HttpURLConnection con = null;
		PrintWriter out = null;
		try {
			URL urx = new URL(SERVER_URL);
			con = (HttpURLConnection) urx.openConnection();
			con.setReadTimeout(500000); //29 seconds for 1m words - leave lots of time
			con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			con.setRequestMethod("GET");
			con.setDoInput(true);
			con.setDoOutput(true);
			OutputStream os = con.getOutputStream();
			OutputStreamWriter bos = new OutputStreamWriter(os, "UTF-8");
			out = new PrintWriter(bos, true);
			out.print(query);
			out.close();
			rd = new BufferedReader(new InputStreamReader(con.getInputStream()));

			String line;
			while ((line = rd.readLine()) != null) {
				buf.append(line + '\n');
			}

		} catch (Exception var18) {
			var18.printStackTrace();
			Logger.error(var18.getMessage()+"|"+query, var18);
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
				Logger.error(var17.getMessage(), var17);
			}

		}
		return buf.toString();
	}
}
