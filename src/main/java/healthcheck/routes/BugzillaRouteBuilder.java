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


import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.net.ssl.SSLContext;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.PredicateClause;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpClientConfigurer;
import org.apache.camel.component.http4.HttpComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import healthcheck.bugzilla.model.Bug;
import healthcheck.model.Component;
import healthcheck.model.ComponentController;
import healthcheck.model.ComponentStatus;
import healthcheck.model.HCRepository;

/**
 * A simple example router from a file system to an ActiveMQ queue and then to a
 * file system
 *
 * @version
 */
public class BugzillaRouteBuilder extends RouteBuilder {

	private static String BUGZILLAKEY = "";
	private static String BUGZILLAURL = "";

	@Autowired
	HCRepository hcRepository;

	/** */
	private static final transient Log logger = LogFactory.getLog( BugzillaRouteBuilder.class.getName());
	//private static ModelCamelContext actx;

	public static void main(String[] args) throws Exception {
		//new Main().run(args);
		
		CamelContext context = new DefaultCamelContext();
		try {
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false&amp;broker.useJmx=true"); 
			context.addComponent("jms", ActiveMQComponent.jmsComponentAutoAcknowledge(connectionFactory));			

			context.addRoutes( new BugzillaRouteBuilder() );
			context.start();
			
			
			
            Thread.sleep(60000);
		} finally {			
            context.stop();
        }
		
		
	}

	
	public void configure() {

		BUGZILLAURL = hcRepository.getProperties().getProperty("BUGZILLAURL");
		BUGZILLAKEY = hcRepository.getProperties().getProperty("BUGZILLAKEY");
		
		if ( ( BUGZILLAURL == null ) || BUGZILLAURL.equals( "" ) ){
			logger.info( "NO BUGZILLa ROUTING. BUGZILLAURL = " + BUGZILLAURL);
			return; //no routing towards Bugzilla
		}
		if ( ( BUGZILLAKEY == null ) || BUGZILLAKEY.equals( "" ) ){
			logger.info( "NO BUGZILLa ROUTING. BUGZILLAKEY = " + BUGZILLAKEY);
			return;//no routing towards Bugzilla
		}


		HttpComponent httpComponent = getContext().getComponent("https4", HttpComponent.class);
		httpComponent.setHttpClientConfigurer(new MyHttpClientConfigurer());
				

		//message to know that component changed status
		from("seda:componentchangedstatus?multipleConsumers=true")
		.convertBodyTo(Component.class)
		.log( "Component ${body.name} will be examined if need to raise issue." )
		.choice()
			.when( raiseIssue )
				.log( "ISSUE will be raised for component ${body.name} !" )				
				.bean( ComponentController.class, "issueRaised")			
				.bean( BugzillaClient.class, "transformComponent2BugBody")
				.to("log:healthcheck.routes.BugzillaRouteBuilder?level=INFO")
				.to("direct:bugzilla.newIssue")
				.endChoice()
			.otherwise()
				.log( "Component ${body.name} NO issue raised" )
				.endChoice();
		
				
		
		
		/**
		 * Create New Issue in Bugzilla. The body is a {@link Bug}
		 */
		from("direct:bugzilla.newIssue")
		.marshal().json( JsonLibrary.Jackson, true)
		.convertBodyTo( String.class ).to("stream:out")
		.errorHandler(deadLetterChannel("direct:dlq_bugzilla")
				.maximumRedeliveries( 120 ) //let's try for the next 2 hours to send it....
				.redeliveryDelay( 60000 ).useOriginalMessage()
				.deadLetterHandleNewException( false )
				//.logExhaustedMessageHistory(false)
				.logExhausted(true)
				.logHandled(true)
				//.retriesExhaustedLogLevel(LoggingLevel.WARN)
				.retryAttemptedLogLevel( LoggingLevel.WARN) )
		.setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.POST))
		.toD( "https4://" + BUGZILLAURL + "/rest.cgi/bug?api_key="+ BUGZILLAKEY +"&throwExceptionOnFailure=true")
		.to("stream:out");
		
		
		
	}

	Predicate raiseIssue = new Predicate() {
		
		@Override
		public boolean matches(Exchange exchange) {
			Component c = exchange.getIn().getBody( Component.class );
			
			if ( !c.getIssueRaised() && ( c.getStatus().equals( ComponentStatus.DOWN) || c.getStatus().equals( ComponentStatus.FAIL ) ))	
			{
				return true;
			}
			else {
				return false;
			}
			
		}
	};
	
	

	Processor BugHeaderExtractProcessor = new Processor() {
		
		@Override
		public void process(Exchange exchange) throws Exception {

			Map<String, Object> headers = exchange.getIn().getHeaders(); 
			Bug aBug = exchange.getIn().getBody( Bug.class ); 
		    headers.put("uuid", aBug.getAliasFirst()  );
		    exchange.getOut().setHeaders(headers);
		    
		    //copy Description to Comment
		    aBug.setComment( BugzillaClient.createComment( aBug.getDescription() ) );
		    //delete Description
		    aBug.setDescription( null );
		    aBug.setAlias( null ); //dont put any Alias		
		    aBug.setCc( null );
		    
		    exchange.getOut().setBody( aBug  );
		    // copy attachements from IN to OUT to propagate them
		    exchange.getOut().setAttachments(exchange.getIn().getAttachments());
			
		}
	};
	
	
	


	public class MyHttpClientConfigurer implements HttpClientConfigurer {

		@Override
		public void configureHttpClient(HttpClientBuilder hc) {
			try {
				SSLContext sslContext;
				sslContext = new SSLContextBuilder().loadTrustMaterial(null, (certificate, authType) -> true).build();

				//hc.setSSLContext(sslContext).setSSLHostnameVerifier(new NoopHostnameVerifier()).build();

				SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory( sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				hc.setSSLSocketFactory(sslConnectionFactory);
				Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
				        .register("https", sslConnectionFactory)
				        .build();

				HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);

				hc.setConnectionManager(ccm);

			} catch (KeyManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	
}


