/*
 * Copyright 2023 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.asr.dictionary;

import java.sql.ResultSet;
import java.util.*;

import org.topicquests.asr.dictionary.server.api.IPostgresDictionary;
import org.topicquests.pg.PostgresConnectionFactory;
import org.topicquests.pg.api.IPostgresConnection;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

import net.minidev.json.JSONObject;

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
		String sql = "INSERT INTO public.dictionary (word, lc_word) VALUES(?, ?) RETURNING id";
		environment.logDebug("AddTerm: "+word);
		IResult r = null;
		long result = -1;
	    try {
	      r = conn.beginTransaction();
	      Object [] obj = new Object[2];
	      obj[0] = word;
	      obj[1] = word.toLowerCase();
	      r = conn.executeSelect(sql, r, obj);
	      ResultSet rs = (ResultSet)r.getResultObject();
	      if (rs != null && rs.next())
			result = rs.getLong("id");
	      
	    } catch (Exception e) {
	    	environment.logError(e.getMessage(), e);
	    } finally {
	    	conn.endTransaction(r);
	    }
	    environment.logDebug("PGD "+word+" "+result+" | "+r.getErrorString());
	    return result;
	}	

	@Override
	public JSONObject getDictionary() {
		JSONObject result = new JSONObject();
		String sql = "SELECT * FROM public.dictionary";
		IResult r = null;
	    try {
	      r = conn.beginTransaction();
	      conn.executeSelect(sql, r);
	      ResultSet rs = (ResultSet)r.getResultObject();
	      if (rs != null) {
	    	  String foo;
	    	  String id;
	    	  while (rs.next()) {
	    		  // key: word:lcword
	    		  id = rs.getString("id");
	    		  foo = rs.getString("word")+":";
	    		  foo += rs.getString("lc_word");
	    		  result.put(id, foo);
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
	public String getTermById(long id) {
		String result = null;
		String sql = "SELECT word FROM public.dictionary WHERE id = ?";
		IResult r = null;
	    try {
	      r = conn.beginTransaction();
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

	@Override
	public void addSynonym(long masterId, long synonymId) {
		String sql = "INSERT INTO public.synonyms (id, syn_id) VALUES(?, ?)";
		environment.logDebug("AddSyn: "+masterId+" "+synonymId);
		IResult r = null;
		long result = -1;
	    try {
	      r = conn.beginTransaction();
	      Object [] obj = new Object[2];
	      obj[0] = Long.valueOf(masterId);
	      obj[1] = Long.valueOf(synonymId);
	      r = conn.executeSelect(sql, r, obj);
	      
	    } catch (Exception e) {
	    	environment.logError(e.getMessage(), e);
	    } finally {
	    	conn.endTransaction(r);
	    }
	}

	@Override
	public List<Long> listSynonymIds(long masterId) {
		List<Long> result = new ArrayList<Long>();
		String sql = "SELECT word FROM public.dictionary LIMIT ? OFFSET ?";
		IResult r = null;
	    try {
	      r = conn.beginTransaction();
	      Object [] obj = new Object[1];
	      obj[0] = Long.valueOf(masterId);
	      conn.executeSelect(sql, r, obj);
	      ResultSet rs = (ResultSet)r.getResultObject();
	      if (rs != null) {
	    	  while(rs.next()) {
	    		  result.add(Long.valueOf(rs.getLong("syn_id")));
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
	public List<String> listSynonyms(long masterId) {
		List<String> result = new ArrayList<String>();
		String sql = "SELECT syn_id FROM public.synonyms where id= ?";
		IResult r = null;
	    try {
	      r = conn.beginTransaction();
	      Object [] obj = new Object[1];
	      obj[0] = Long.valueOf(masterId);
	      conn.executeSelect(sql, r, obj);
	      ResultSet rs = (ResultSet)r.getResultObject();
	      if (rs != null) {
	  		List<Long> rxx = new ArrayList<Long>();

	    	  while(rs.next()) {
	    		  rxx.add(Long.valueOf(rs.getLong("syn_id")));
	    	  }
	    	  if (!rxx.isEmpty()) {
	    		  sql= "SELECT word FROM public.dictionary WHERE id="; 
	    		  Iterator<Long> litr = rxx.iterator();
	    		  while (litr.hasNext()) {
	    			  obj[0] = Long.valueOf(litr.next());
	    			  conn.executeSelect(sql, r, obj);
	    			  rs = (ResultSet)r.getResultObject();
	    			  if (rs != null) {
	    				  result.add(rs.getString("word"));
	    			  }
	    		  }
	    	  }
	      }
	    } catch (Exception e) {
	    	environment.logError(e.getMessage(), e);
	    } finally {
	    	conn.endTransaction(r);
	    }
	    return result;
	}

}
