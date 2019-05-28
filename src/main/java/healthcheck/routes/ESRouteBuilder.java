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

package healthcheck.routes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import healthcheck.model.Component;
import healthcheck.model.ComponentStatus;
import healthcheck.model.HCRepository;


public class ESRouteBuilder  extends RouteBuilder{
	

	private static String ELASTICURL = "";
	

	/** 
	 * every 1 hour post Components Status
	 */
	private static final int REFRESH_PERIOD = 60*60*1000;
	//private static final int REFRESH_PERIOD = 60*1000;
	/** */
	private static final transient Log logger = LogFactory.getLog( ESRouteBuilder.class.getName());
	
	@Autowired
	HCRepository hcRepository;
	
	
	public void configure() {
		
		ELASTICURL = hcRepository.getProperties().getProperty("ELASTICURL");
		
		if ( ( ELASTICURL == null ) || ELASTICURL.equals( "" ) ){
			logger.info( "NO ELASTICURL ROUTING. ELASTICURL = " + ELASTICURL);
			return; //no routing towards ELASTIC
		}
	
		for (Component comp : hcRepository.getComponents()) {
			String url = ELASTICURL.replace( "https://", "https4://").replace( "http://", "http4://") ;
			url = url + "/" + comp.getName().toLowerCase() + "/_doc";
			int period = REFRESH_PERIOD + RandomUtils.nextInt( 30000 );
			//create a timer to post status
			from("timer://" + comp.getName().replace(" ", "_") + "Timer?delay=10m&period=" + period  )
			.log( "Will post component: " + comp.getName() + " (every " + period + " msecs) to Elastic" )
			.setBody().constant(comp)
			.bean(ESh.class, "amap")
			.setHeader(Exchange.CONTENT_TYPE, constant( "application/json" ))
            .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.POST))
			.toD(  url  )
			.log( "End refresh route. Next in " + REFRESH_PERIOD + " msecs");
			
			
		}
		
	       
	}




	static public class ESh{
		
		public static String amap( Component comp ){
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("time", Instant.now().toString());
			map.put("Element", comp.getName() );
			
			if ( comp.getStatus().equals( ComponentStatus.UP ) || comp.getStatus().equals( ComponentStatus.PASS )  ){
				map.put("StatusN", 1);
			}else {
				map.put("StatusN", 0);
			}
			String json;
			try {
				json = new ObjectMapper().writeValueAsString(map);
				System.out.println(json);
				return json;
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
	}
	
	
//	
//	public static void main(String[] args) throws Exception {
//		//new Main().run(args);
//		
//		
//		CamelContext tempcontext = new DefaultCamelContext();
//		try {
//		RouteBuilder rb = new RouteBuilder() {
//            @Override
//            public void configure() throws Exception {
//                from( "timer://mytimer?period=10000&repeatCount=100&daemon=true"  )
//                .bean( ESh.class, "amap")
//                .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.POST))
//                //.to("stream:out");
//        		.to("http4://150.140.184.249:9200/5ginfire/status");
//            }
//        };
//        tempcontext.addRoutes( rb);
//        tempcontext.start();
//        Thread.sleep(300000);
//		} finally {			
//			tempcontext.stop();
//        }
//		
//	}
	

}
