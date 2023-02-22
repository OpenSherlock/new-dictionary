/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.asr.dictionary;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.EnumSet;
import java.util.List;
//import java.util.logging.Handler;


import javax.servlet.DispatcherType;
import javax.servlet.Servlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import org.topicquests.support.util.LoggingPlatform;
/**
 * @author jackpark
 * A class which boots the servlet
 */
public class Main {
	private DictionaryServerEnvironment environment;
	/**
	 * 
	 */
	public Main() {
		environment = new DictionaryServerEnvironment();		
		//////////////////////
		// Create the server
		// ideas from http://stackoverflow.com/questions/28190198/cross-origin-filter-with-embedded-jetty
		// and http://www.becodemonkey.com/?p=179
		//////////////////////
		int port = Integer.valueOf(environment.getStringProperty("ServerPort")).intValue();
		Server server = new Server(port);
		environment.logDebug("MAIN.SERVER");
		ServletHandler handler = new ServletHandler();
        FilterHolder holder = new FilterHolder(new CrossOriginFilter());
        holder.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        holder.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        holder.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,HEAD");
        holder.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin");
        holder.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");
        holder.setName("cross-origin");
        FilterMapping fm = new FilterMapping();
        fm.setFilterName("cross-origin");
        fm.setPathSpec("*");
        handler.addFilter(holder, fm );
        environment.logDebug("MAIN.HANDLER");
      
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.addFilter(holder, "/*", EnumSet.of(DispatcherType.REQUEST)); 
        context.setContextPath("/");
        DictionaryServlet svlt = new DictionaryServlet(environment);
        context.addServlet(new ServletHolder(svlt),"/*");
        environment.logDebug("MAIN.SVLT "+svlt);
              HandlerList handlers = new HandlerList();
        handlers.addHandler(handler);
        handlers.addHandler(context);
        server.setHandler(context);
		//////////////////////
		// Start the server 
		//////////////////////
        try {
        	environment.logDebug("MAIN.START");

        	server.start();
        	server.join();

        } catch (Exception e) {
        	environment.logError(e.getMessage(), e);
        	e.printStackTrace();
        }

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Main();
	}

}
