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

import javax.net.ssl.SSLContext;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpClientConfigurer;
import org.apache.camel.component.http4.HttpComponent;
import org.apache.commons.lang.math.RandomUtils;
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

import healthcheck.model.Component;
import healthcheck.model.ComponentController;
import healthcheck.model.HCRepository;
import healthcheck.model.HealthCheckMode;

/**
 * 
 * Creates healtcheck Routes
 * @author ctranoris
 *
 */
public class HCRouteBuilder extends RouteBuilder {


	private static String BUGZILLAURL = "portal.5ginfire.eu:443/bugzilla";
	
	/** 
	 * every 1 minute check Components Status
	 */
	private static final int REFRESH_PERIOD = 1*60*1000;
	/** 
	 * every 2 minutes check Components Status
	 */
	private static final int ACTIVE_COMPONENTS_POLLING_PERIOD = 2*60*1000;

	/** */
	private static final transient Log logger = LogFactory.getLog( HCRouteBuilder.class.getName());

	@Autowired
	HCRepository hcRepository;
	
	/* (non-Javadoc)
	 * @see org.apache.camel.builder.RouteBuilder#configure()
	 */
	public void configure() {

		HttpComponent httpComponent = getContext().getComponent("https4", HttpComponent.class);
		httpComponent.setHttpClientConfigurer(new MyHttpClientConfigurer());

		//handle message when a component seen. possibly triggered by ACTIVE or PASSIVE components
		from("seda:componentseen?multipleConsumers=true")
		.bean( ComponentController.class, "componentSeen");
		
		//create a timer to check status
		from("timer://myTimer?period=" + REFRESH_PERIOD)
		.bean(ComponentController.class, "refreshComponentsStatus")
		.log( "End refresh route. Next in " + REFRESH_PERIOD + " msecs");
		
		//message to know that component changed status
		from("seda:componentchangedstatus?multipleConsumers=true")
		.convertBodyTo(Component.class)		
		.log( "Component ${body.name} changed state to ${body.status} at ${date:now:yyyyMMddHHmmss} " );
		
		
		for (Component comp : hcRepository.getComponents()) {
			if ( comp.getMode().equals( HealthCheckMode.ACTIVE ) 
					&& ( comp.getCheckURL() != null )
					&& ( !comp.getCheckURL().equals("") )){

				String url = comp.getCheckURL().replace( "https://", "https4://").replace( "http://", "http4://") ;
				//create a timer to check status. Randomize period.
				int period = ACTIVE_COMPONENTS_POLLING_PERIOD + RandomUtils.nextInt( 30000 );
				
				from("timer://" + comp.getName().replace(" ", "_") + "Timer?period=" + period  )
				.log( "Will check component: " + comp.getName() + " (every " + period + " msecs) by GET from URL: " + url )
				.setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.GET))
				.toD(  url  )
				.log( "Component " + comp.getName() + " seems alive!" )
				//.to("stream:out")
				.setBody().constant(comp)
				.bean( ComponentController.class, "componentSeen");
			}
			
		}
				
	}


	/**
	 * 
	 * configure https
	 * @author ctranoris
	 *
	 */
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


