/**
 * Copyright 2017 University of Patras 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and limitations under the License.
 */

package centralLog.api;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import healthcheck.model.HCRepository;


public class CentralLoggerRouteBuilder  extends RouteBuilder{
	

	private static String ELASTICLOGGERURL = "";
	private static String ELASTICSIMPLEMONURL = "";
	

	/** 
	 * every 1 hour post Components Status
	 */
	private static final int REFRESH_PERIOD = 60*60*1000;
	//private static final int REFRESH_PERIOD = 60*1000;
	/** */
	private static final transient Log logger = LogFactory.getLog( CentralLoggerRouteBuilder.class.getName());
	
	@Autowired
	HCRepository hcRepository;
	
	
	public void configure() {

		ELASTICLOGGERURL = hcRepository.getProperties().getProperty("ELASTICLOGGERURL");
		ELASTICSIMPLEMONURL = hcRepository.getProperties().getProperty("ELASTICSIMPLEMONURL");
		
		
		if ( ( ELASTICLOGGERURL == null ) || ELASTICLOGGERURL.equals( "" ) ){
			logger.info( "NO ELASTICURL ROUTING. ELASTICURL = " + ELASTICLOGGERURL);
			return; //no routing towards ELASTIC
		}
		
		if ( ( ELASTICSIMPLEMONURL == null ) || ELASTICSIMPLEMONURL.equals( "" ) ){
			logger.info( "NO ELASTICSIMPLEMONURL ROUTING. ELASTICSIMPLEMONURL = " + ELASTICSIMPLEMONURL);
			return; //no routing towards ELASTICSIMPLEMONURL
		}
	
		String url = ELASTICLOGGERURL.replace( "https://", "https4://").replace( "http://", "http4://") + "/_doc";

		from("seda:centralLog")	
        .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.POST))
		.toD(  url  );
		
		
		url = ELASTICSIMPLEMONURL.replace( "https://", "https4://").replace( "http://", "http4://") + "/_doc";

		from("seda:simplemon")	
        .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.POST))
		.toD(  url  );
			
	       
	}




	

}
