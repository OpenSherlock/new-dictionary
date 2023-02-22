/**
 * 
 */
package org.topicquests.asr.dictionary;

import java.sql.ResultSet;
import java.util.*;

import org.topicquests.asr.dictionary.server.api.IPostgresDictionary;
import org.topicquests.pg.PostgresConnectionFactory;
import org.topicquests.pg.api.IPostgresConnection;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public class PostgresDictionary implements IPostgresDictionary {
	private DictionaryServerEnvironment environment;
	private PostgresConnectionFactory database = null;
	private IPostgresConnection conn = null;
	private IResult BIG_RESULT;
	private long latestId = 0;

	/**
	 * 
	 */
	public PostgresDictionary(DictionaryServerEnvironment env) {
		environment = env;
		BIG_RESULT = new ResultPojo();
		database = environment.getPostgresFactory();
		try {
			//We use one connection and close it at the end
			conn = database.getConnection();
		} catch (Exception e) {
			environment.logError(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isEmpty() {
		String sql = "SELECT count(id) FROM public.dictionary";
		IResult r = null;
	    try {
	      r = conn.beginTransaction();
	      conn.setConvRole(r);
	      Object [] obj = new Object[0];
	      conn.executeSelect(sql, r, obj);
	      ResultSet rs = (ResultSet)r.getResultObject();
	      if (rs != null && rs.next()) {
	    	  long cx = rs.getLong(1);
	    	  return cx > 0;
	      }
	    } catch (Exception e) {
	    	environment.logError(e.getMessage(), e);
	    } finally {
	    	conn.endTransaction(r);
	    }		
	    return false;
	}

	@Override
	public long addTermWord(String word) {
		String sql = "INSERT INTO public.dictionary (id, word, lc_word) VALUES(?, ?, ?)";
		IResult r = null;
	    try {
	      r = conn.beginTransaction();
	      conn.setProxyRole(r);
	      Object [] obj = new Object[3];
	      obj[0] = latestId;
	      obj[1] = word;
	      obj[2] = word.toLowerCase();
	      conn.executeSQL(sql, r, obj);
	    } catch (Exception e) {
	    	environment.logError(e.getMessage(), e);
	    } finally {
	    	conn.endTransaction(r);
	    }
	    updateLatestId();
	    return this.getTermId(word);
	}

	void updateLatestId() {
		String sql = "UPDATE public.latest SET latest = ? WHERE latest = ?";
		IResult r = null;
	    try {
	      r = conn.beginTransaction();
	      conn.setProxyRole(r);
	      Object [] obj = new Object[2];
	      obj[0] = latestId+1;
	      obj[1] = latestId++;
	      conn.executeSQL(sql, r, obj);
	    } catch (Exception e) {
	    	environment.logError(e.getMessage(), e);
	    } finally {
	    	conn.endTransaction(r);
	    }		
	}
	

	
	@Override
	public String getTermById(long id) {
		String result = null;
		String sql = "SELECT word FROM public.dictionary WHERE id = ?";
		IResult r = null;
	    try {
	      r = conn.beginTransaction();
	      conn.setProxyRole(r);
	      Object [] obj = new Object[1];
	      obj[0] = id;
	      conn.executeSelect(sql, r, obj);
	      ResultSet rs = (ResultSet)r.getResultObject();
	      if (rs != null && rs.next()) {
	    	  result = rs.getString("word");
	      }
	    } catch (Exception e) {
	    	environment.logError(e.getMessage(), e);
	    } finally {
	    	conn.endTransaction(r);
	    }
	    return result;	
	}

	@Override
	public List<String> listWords(int offset, int count) {
		List<String> result = new ArrayList<String>();
		String sql = "SELECT word FROM public.dictionary LIMIT ? OFFSET ?";
		IResult r = null;
	    try {
	      r = conn.beginTransaction();
	      conn.setProxyRole(r);
	      Object [] obj = new Object[2];
	      obj[0] = count;
	      obj[1] = offset;
	      conn.executeSelect(sql, r, obj);
	      ResultSet rs = (ResultSet)r.getResultObject();
	      if (rs != null) {
	    	  while(rs.next()) {
	    		  result.add(rs.getString("word"));
	    	  }
	      }
	    } catch (Exception e) {
	    	environment.logError(e.getMessage(), e);
	    } finally {
	    	conn.endTransaction(r);
	    }
	    return result;
	}

	@Override
	public long getTermId(String word) {
		long result = -1;
		String sql = "SELECT id FROM public.dictionary WHERE lc_word = ?";
		IResult r = null;
	    try {
	      r = conn.beginTransaction();
	      conn.setProxyRole(r);
	      Object [] obj = new Object[1];
	      obj[0] = word.toLowerCase();
	      conn.executeSelect(sql, r, obj);
	      ResultSet rs = (ResultSet)r.getResultObject();
	      if (rs != null && rs.next())
	    	  result = rs.getLong("id");
	    } catch (Exception e) {
	    	environment.logError(e.getMessage(), e);
	    } finally {
	    	conn.endTransaction(r);
	    }
	    return result;
	}


	@Override
	public void shutDown() {
		if (conn != null) {
			conn.closeConnection(BIG_RESULT);
			conn = null;
		}
	}

}
